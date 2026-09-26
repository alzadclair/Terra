package com.terraforge.rpg.client.render.mesh;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads, parses, and caches runtime weighted skeletal meshes (.skin.json) for TerraForge RPG bosses.
 */
public final class TerraSkinnedMeshLoader {

    private static final Map<ResourceLocation, TerraSkinnedMesh> CACHE = new ConcurrentHashMap<>();

    private TerraSkinnedMeshLoader() {}

    public static TerraSkinnedMesh getOrLoad(ResourceLocation location) {
        return CACHE.computeIfAbsent(location, TerraSkinnedMeshLoader::load);
    }

    public static void clearCache() {
        CACHE.clear();
    }

    private static TerraSkinnedMesh load(ResourceLocation location) {
        try (InputStream stream = Minecraft.getInstance().getResourceManager().open(location);
             BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {

            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            String name = root.has("name") ? root.get("name").getAsString() : location.getPath();

            List<String> bones = new ArrayList<>();
            if (root.has("bones")) {
                for (JsonElement elem : root.getAsJsonArray("bones")) {
                    bones.add(elem.getAsString());
                }
            }

            List<TerraSkinnedMesh.SkinnedPart> parts = new ArrayList<>();
            if (root.has("parts")) {
                for (JsonElement partElem : root.getAsJsonArray("parts")) {
                    JsonObject pObj = partElem.getAsJsonObject();
                    String partName = pObj.get("name").getAsString();
                    int vertexCount = pObj.get("vertexCount").getAsInt();

                    float[] positions = toFloatArray(pObj.getAsJsonArray("positions"));
                    float[] normals = toFloatArray(pObj.getAsJsonArray("normals"));
                    float[] uvs = toFloatArray(pObj.getAsJsonArray("uvs"));
                    int[] boneIndices = toIntArray(pObj.getAsJsonArray("boneIndices"));
                    float[] boneWeights = toFloatArray(pObj.getAsJsonArray("boneWeights"));
                    int[] indices = toIntArray(pObj.getAsJsonArray("indices"));

                    parts.add(new TerraSkinnedMesh.SkinnedPart(
                            partName, vertexCount, positions, normals, uvs, boneIndices, boneWeights, indices
                    ));
                }
            }

            return new TerraSkinnedMesh(name, bones, parts);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load TerraForge skinned mesh: " + location, e);
        }
    }

    private static float[] toFloatArray(JsonArray array) {
        float[] res = new float[array.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = array.get(i).getAsFloat();
        }
        return res;
    }

    private static int[] toIntArray(JsonArray array) {
        int[] res = new int[array.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = array.get(i).getAsInt();
        }
        return res;
    }
}
