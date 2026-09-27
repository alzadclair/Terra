package com.terraforge.rpg.client.render.mesh;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.terraforge.rpg.TerraForgeRPG;
import com.terraforge.rpg.util.TerraLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.Matrix4f;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads, parses, caches, and preloads runtime weighted skeletal meshes (.skin.json) for TerraForge RPG.
 * Preloading runs during resource reload to guarantee zero disk I/O and zero GC stalls when bosses spawn.
 */
public final class TerraSkinnedMeshLoader {

    private static final Map<ResourceLocation, TerraSkinnedMeshData> CACHE = new ConcurrentHashMap<>();

    public static final ResourceLocation SKIN_EYE_P1 =
            ResourceLocation.fromNamespaceAndPath(TerraForgeRPG.MOD_ID, "models/entity/boss/eye_of_cthulhu_p1.skin.json");
    public static final ResourceLocation SKIN_EYE_P2 =
            ResourceLocation.fromNamespaceAndPath(TerraForgeRPG.MOD_ID, "models/entity/boss/eye_of_cthulhu_p2.skin.json");

    private TerraSkinnedMeshLoader() {}

    public static TerraSkinnedMeshData getOrLoad(ResourceLocation location) {
        return CACHE.computeIfAbsent(location, loc -> load(loc, Minecraft.getInstance().getResourceManager()));
    }

    public static void clearCache() {
        CACHE.clear();
    }

    /**
     * Preloads all known skinned meshes into memory and profiles their parse times.
     * Invoked by client resource reload listener.
     */
    public static void preload(ResourceManager resourceManager) {
        clearCache();
        ResourceLocation[] toPreload = new ResourceLocation[] {
                SKIN_EYE_P1,
                SKIN_EYE_P2
        };

        for (ResourceLocation loc : toPreload) {
            try {
                long startNs = System.nanoTime();
                TerraSkinnedMeshData data = load(loc, resourceManager);
                CACHE.put(loc, data);
                long elapsedNs = System.nanoTime() - startNs;
                double elapsedMs = elapsedNs / 1_000_000.0;

                int totalVerts = 0;
                for (TerraSkinnedMeshData.PartData p : data.getParts()) {
                    totalVerts += p.vertexCount;
                }

                float[] bounds = data.computeBounds();
                float extX = bounds[3] - bounds[0];
                float extY = bounds[4] - bounds[1];
                float extZ = bounds[5] - bounds[2];

                TerraLogger.info("CLIENT", String.format(
                        "Preloaded TerraForge skinned mesh: %s (%d parts, %d vertices, bounds=[%.1f, %.1f, %.1f]) in %.2f ms",
                        loc, data.getParts().size(), totalVerts, extX, extY, extZ, elapsedMs
                ));
            } catch (Exception e) {
                TerraLogger.error("CLIENT", "Failed to preload skinned mesh: " + loc, e);
            }
        }
    }

    public static TerraSkinnedMeshData load(ResourceLocation location, ResourceManager rm) {
        try (InputStream stream = rm.open(location);
             BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            return loadFromReader(reader, location.getPath());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load TerraForge skinned mesh: " + location, e);
        }
    }

    public static TerraSkinnedMeshData loadFromReader(BufferedReader reader, String defaultName) {
        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
        String name = root.has("name") ? root.get("name").getAsString() : defaultName;
        String texture = root.has("texture") ? root.get("texture").getAsString() : "";

        List<TerraSkinnedMeshData.BoneData> boneDataList = new ArrayList<>();
        List<String> legacyBoneNames = new ArrayList<>();
        Matrix4f[] invBindMatrices = null;

        if (root.has("bones")) {
            JsonArray bArr = root.getAsJsonArray("bones");
            if (!bArr.isEmpty() && bArr.get(0).isJsonObject()) {
                for (JsonElement elem : bArr) {
                    JsonObject bObj = elem.getAsJsonObject();
                    String bName = bObj.get("name").getAsString();
                    String parent = bObj.has("parent") && !bObj.get("parent").isJsonNull() ? bObj.get("parent").getAsString() : null;
                    Matrix4f bindLocal = new Matrix4f();
                    if (bObj.has("bindLocal")) {
                        bindLocal.set(toFloatArray(bObj.getAsJsonArray("bindLocal")));
                    }
                    Matrix4f bindWorld = new Matrix4f();
                    if (bObj.has("bindWorld")) {
                        bindWorld.set(toFloatArray(bObj.getAsJsonArray("bindWorld")));
                    }
                    Matrix4f invBind = new Matrix4f();
                    if (bObj.has("inverseBind")) {
                        invBind.set(toFloatArray(bObj.getAsJsonArray("inverseBind")));
                    }
                    boneDataList.add(new TerraSkinnedMeshData.BoneData(bName, parent, bindLocal, bindWorld, invBind));
                }
            } else {
                for (JsonElement elem : bArr) {
                    legacyBoneNames.add(elem.getAsString());
                }
            }
        }

        if (boneDataList.isEmpty() && root.has("inverseBindMatrices")) {
            JsonArray arr = root.getAsJsonArray("inverseBindMatrices");
            invBindMatrices = new Matrix4f[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                float[] mFloats = toFloatArray(arr.get(i).getAsJsonArray());
                Matrix4f m = new Matrix4f();
                if (mFloats.length == 16) {
                    m.set(mFloats);
                }
                invBindMatrices[i] = m;
            }
        }

        List<TerraSkinnedMeshData.PartData> parts = new ArrayList<>();
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

                TerraSkinnedMeshData.RenderMode renderMode = pObj.has("renderMode")
                        ? TerraSkinnedMeshData.RenderMode.fromString(pObj.get("renderMode").getAsString())
                        : ("glass".equalsIgnoreCase(partName) ? TerraSkinnedMeshData.RenderMode.TRANSLUCENT : TerraSkinnedMeshData.RenderMode.OPAQUE);

                parts.add(new TerraSkinnedMeshData.PartData(
                        partName, vertexCount, positions, normals, uvs, boneIndices, boneWeights, indices, renderMode
                ));
            }
        }

        if (!boneDataList.isEmpty()) {
            return new TerraSkinnedMeshData(name, texture, boneDataList, parts);
        } else {
            return new TerraSkinnedMeshData(name, texture, legacyBoneNames, invBindMatrices, parts);
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
