package com.terraforge.rpg.client.render.mesh;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads, parses, and caches real 3D OBJ meshes for entity rendering in TerraForge RPG.
 */
public final class TerraMeshLoader {

    private static final Map<ResourceLocation, TerraMesh3D> CACHE = new ConcurrentHashMap<>();

    private TerraMeshLoader() {}

    public static TerraMesh3D getOrLoad(ResourceLocation location) {
        return CACHE.computeIfAbsent(location, TerraMeshLoader::load);
    }

    public static void clearCache() {
        CACHE.clear();
    }

    private static TerraMesh3D load(ResourceLocation location) {
        try (InputStream stream = Minecraft.getInstance().getResourceManager().open(location);
             BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {

            List<float[]> rawPositions = new ArrayList<>();
            List<float[]> rawUvs = new ArrayList<>();
            List<float[]> rawNormals = new ArrayList<>();

            Map<String, List<int[]>> partFaces = new LinkedHashMap<>();
            String currentPart = "main";
            partFaces.put(currentPart, new ArrayList<>());

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                if (line.startsWith("v ")) {
                    String[] tokens = line.split("\\s+");
                    float x = Float.parseFloat(tokens[1]);
                    float y = Float.parseFloat(tokens[2]);
                    float z = Float.parseFloat(tokens[3]);
                    rawPositions.add(new float[]{x, y, z});
                } else if (line.startsWith("vt ")) {
                    String[] tokens = line.split("\\s+");
                    float u = Float.parseFloat(tokens[1]);
                    float v = 1.0f - Float.parseFloat(tokens[2]); // Flip V for Minecraft/OpenGL UV coords
                    rawUvs.add(new float[]{u, v});
                } else if (line.startsWith("vn ")) {
                    String[] tokens = line.split("\\s+");
                    float nx = Float.parseFloat(tokens[1]);
                    float ny = Float.parseFloat(tokens[2]);
                    float nz = Float.parseFloat(tokens[3]);
                    rawNormals.add(new float[]{nx, ny, nz});
                } else if (line.startsWith("o ") || line.startsWith("g ")) {
                    String[] tokens = line.split("\\s+");
                    if (tokens.length > 1) {
                        currentPart = tokens[1];
                        partFaces.computeIfAbsent(currentPart, k -> new ArrayList<>());
                    }
                } else if (line.startsWith("f ")) {
                    String[] tokens = line.split("\\s+");
                    // parse vertex triplets (v/vt/vn)
                    List<int[]> faceVerts = new ArrayList<>();
                    for (int i = 1; i < tokens.length; i++) {
                        String[] indices = tokens[i].split("/");
                        int vIdx = Integer.parseInt(indices[0]) - 1;
                        int vtIdx = (indices.length > 1 && !indices[1].isEmpty()) ? Integer.parseInt(indices[1]) - 1 : -1;
                        int vnIdx = (indices.length > 2 && !indices[2].isEmpty()) ? Integer.parseInt(indices[2]) - 1 : -1;
                        faceVerts.add(new int[]{vIdx, vtIdx, vnIdx});
                    }

                    // Triangulate convex polygons (e.g. fan triangulation)
                    for (int i = 1; i < faceVerts.size() - 1; i++) {
                        partFaces.get(currentPart).add(faceVerts.get(0));
                        partFaces.get(currentPart).add(faceVerts.get(i));
                        partFaces.get(currentPart).add(faceVerts.get(i + 1));
                    }
                }
            }

            Map<String, TerraMesh3D.Part> builtParts = new HashMap<>();

            for (Map.Entry<String, List<int[]>> entry : partFaces.entrySet()) {
                String partName = entry.getKey();
                List<int[]> faces = entry.getValue();
                if (faces.isEmpty()) continue;

                // De-duplicate vertex definitions
                Map<String, Integer> uniqueKeyToIndex = new HashMap<>();
                List<Float> posList = new ArrayList<>();
                List<Float> uvList = new ArrayList<>();
                List<Float> normList = new ArrayList<>();
                int[] finalIndices = new int[faces.size()];

                for (int i = 0; i < faces.size(); i++) {
                    int[] vert = faces.get(i);
                    int vIdx = vert[0];
                    int vtIdx = vert[1];
                    int vnIdx = vert[2];
                    String key = vIdx + "/" + vtIdx + "/" + vnIdx;

                    Integer existing = uniqueKeyToIndex.get(key);
                    if (existing == null) {
                        int newIdx = uniqueKeyToIndex.size();
                        uniqueKeyToIndex.put(key, newIdx);
                        finalIndices[i] = newIdx;

                        float[] pos = (vIdx >= 0 && vIdx < rawPositions.size()) ? rawPositions.get(vIdx) : new float[]{0, 0, 0};
                        posList.add(pos[0]);
                        posList.add(pos[1]);
                        posList.add(pos[2]);

                        float[] uv = (vtIdx >= 0 && vtIdx < rawUvs.size()) ? rawUvs.get(vtIdx) : new float[]{0, 0};
                        uvList.add(uv[0]);
                        uvList.add(uv[1]);

                        float[] norm = (vnIdx >= 0 && vnIdx < rawNormals.size()) ? rawNormals.get(vnIdx) : new float[]{0, 1, 0};
                        normList.add(norm[0]);
                        normList.add(norm[1]);
                        normList.add(norm[2]);
                    } else {
                        finalIndices[i] = existing;
                    }
                }

                float[] posArray = new float[posList.size()];
                for (int j = 0; j < posList.size(); j++) posArray[j] = posList.get(j);

                float[] uvArray = new float[uvList.size()];
                for (int j = 0; j < uvList.size(); j++) uvArray[j] = uvList.get(j);

                float[] normArray = new float[normList.size()];
                for (int j = 0; j < normList.size(); j++) normArray[j] = normList.get(j);

                builtParts.put(partName, new TerraMesh3D.Part(partName, posArray, uvArray, normArray, finalIndices));
            }

            return new TerraMesh3D(builtParts);
        } catch (Exception e) {
            System.err.println("[TerraForge] Failed to load 3D mesh: " + location + " - " + e.getMessage());
            return new TerraMesh3D(Collections.emptyMap());
        }
    }
}
