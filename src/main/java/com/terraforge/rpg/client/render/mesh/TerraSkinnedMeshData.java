package com.terraforge.rpg.client.render.mesh;

import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable geometry and skeletal bind data for a 3D skinned mesh.
 * Loaded once from disk/resource pack and cached globally across all entities.
 * Thread-safe and read-only at runtime.
 */
public class TerraSkinnedMeshData {

    public static class BoneData {
        public final String name;
        public final String parent;
        public final Matrix4f bindLocalMatrix;
        public final Matrix4f bindWorldMatrix;
        public final Matrix4f inverseBindMatrix;

        public BoneData(String name, String parent, Matrix4f bindLocalMatrix, Matrix4f bindWorldMatrix, Matrix4f inverseBindMatrix) {
            this.name = name;
            this.parent = parent;
            this.bindLocalMatrix = new Matrix4f(bindLocalMatrix);
            this.bindWorldMatrix = new Matrix4f(bindWorldMatrix);
            this.inverseBindMatrix = new Matrix4f(inverseBindMatrix);
        }
    }

    public enum RenderMode {
        OPAQUE,
        CUTOUT,
        TRANSLUCENT;

        public static RenderMode fromString(String str) {
            if (str == null) return OPAQUE;
            try {
                return RenderMode.valueOf(str.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return OPAQUE;
            }
        }
    }

    public static class PartData {
        public final String name;
        public final int vertexCount;
        public final float[] bindPositions;
        public final float[] bindNormals;
        public final float[] uvs;
        public final int[] boneIndices;
        public final float[] boneWeights;
        public final int[] indices;
        public final RenderMode renderMode;
        public final String texture;
        public final net.minecraft.resources.ResourceLocation textureLocation;

        public PartData(String name, int vertexCount, float[] bindPositions, float[] bindNormals,
                        float[] uvs, int[] boneIndices, float[] boneWeights, int[] indices) {
            this(name, vertexCount, bindPositions, bindNormals, uvs, boneIndices, boneWeights, indices,
                 "glass".equalsIgnoreCase(name) ? RenderMode.TRANSLUCENT : RenderMode.OPAQUE, null);
        }

        public PartData(String name, int vertexCount, float[] bindPositions, float[] bindNormals,
                        float[] uvs, int[] boneIndices, float[] boneWeights, int[] indices, RenderMode renderMode) {
            this(name, vertexCount, bindPositions, bindNormals, uvs, boneIndices, boneWeights, indices, renderMode, null);
        }

        public PartData(String name, int vertexCount, float[] bindPositions, float[] bindNormals,
                        float[] uvs, int[] boneIndices, float[] boneWeights, int[] indices,
                        RenderMode renderMode, String texture) {
            this.name = name;
            this.vertexCount = vertexCount;
            this.bindPositions = bindPositions;
            this.bindNormals = bindNormals;
            this.uvs = uvs;
            this.boneIndices = boneIndices;
            this.boneWeights = boneWeights;
            this.indices = indices;
            this.renderMode = (renderMode != null) ? renderMode : ("glass".equalsIgnoreCase(name) ? RenderMode.TRANSLUCENT : RenderMode.OPAQUE);
            this.texture = texture;
            if (texture != null && !texture.trim().isEmpty()) {
                String clean = texture.trim();
                this.textureLocation = clean.contains(":")
                        ? net.minecraft.resources.ResourceLocation.parse(clean)
                        : net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(com.terraforge.rpg.TerraForgeRPG.MOD_ID, clean);
            } else {
                this.textureLocation = null;
            }
        }
    }

    private final String name;
    private final String texture;
    private final List<BoneData> bones;
    private final Map<String, Integer> boneIndexMap;
    private final List<String> boneNames;
    private final Matrix4f[] inverseBindMatrices;
    private final Matrix4f[] bindWorldMatrices;
    private final List<PartData> parts;

    public TerraSkinnedMeshData(String name, String texture, List<BoneData> bones, List<PartData> parts) {
        this.name = name;
        this.texture = texture;
        this.bones = Collections.unmodifiableList(new ArrayList<>(bones));
        this.parts = Collections.unmodifiableList(new ArrayList<>(parts));

        int boneCount = bones.size();
        List<String> names = new ArrayList<>(boneCount);
        Map<String, Integer> idxMap = new HashMap<>(boneCount);
        this.inverseBindMatrices = new Matrix4f[boneCount];
        this.bindWorldMatrices = new Matrix4f[boneCount];

        for (int i = 0; i < boneCount; i++) {
            BoneData b = bones.get(i);
            names.add(b.name);
            idxMap.put(b.name, i);
            this.inverseBindMatrices[i] = new Matrix4f(b.inverseBindMatrix);
            this.bindWorldMatrices[i] = new Matrix4f(b.bindWorldMatrix);
        }

        this.boneNames = Collections.unmodifiableList(names);
        this.boneIndexMap = Collections.unmodifiableMap(idxMap);
    }

    /**
     * Legacy constructor for backwards compatibility with tests and older format loaders.
     */
    public TerraSkinnedMeshData(String name, String texture, List<String> boneNames,
                                Matrix4f[] inverseBindMatrices, List<PartData> parts) {
        this.name = name;
        this.texture = texture;
        this.boneNames = Collections.unmodifiableList(new ArrayList<>(boneNames));
        this.parts = Collections.unmodifiableList(new ArrayList<>(parts));

        int boneCount = boneNames.size();
        this.inverseBindMatrices = new Matrix4f[boneCount];
        this.bindWorldMatrices = new Matrix4f[boneCount];
        List<BoneData> boneList = new ArrayList<>(boneCount);
        Map<String, Integer> idxMap = new HashMap<>(boneCount);

        for (int i = 0; i < boneCount; i++) {
            String bName = boneNames.get(i);
            idxMap.put(bName, i);
            if (inverseBindMatrices != null && i < inverseBindMatrices.length && inverseBindMatrices[i] != null) {
                this.inverseBindMatrices[i] = new Matrix4f(inverseBindMatrices[i]);
            } else {
                this.inverseBindMatrices[i] = new Matrix4f().identity();
            }
            this.bindWorldMatrices[i] = new Matrix4f(this.inverseBindMatrices[i]).invert();
            boneList.add(new BoneData(bName, null, this.bindWorldMatrices[i], this.bindWorldMatrices[i], this.inverseBindMatrices[i]));
        }

        this.bones = Collections.unmodifiableList(boneList);
        this.boneIndexMap = Collections.unmodifiableMap(idxMap);
    }

    public String getName() {
        return name;
    }

    public String getTexture() {
        return texture;
    }

    public List<BoneData> getBones() {
        return bones;
    }

    public BoneData getBone(String boneName) {
        Integer idx = boneIndexMap.get(boneName);
        return idx != null ? bones.get(idx) : null;
    }

    public int getBoneIndex(String boneName) {
        Integer idx = boneIndexMap.get(boneName);
        return idx != null ? idx : -1;
    }

    public List<String> getBoneNames() {
        return boneNames;
    }

    public int getBoneCount() {
        return bones.size();
    }

    public Matrix4f getInverseBindMatrix(int boneIndex) {
        return inverseBindMatrices[boneIndex];
    }

    public Matrix4f getBindWorldMatrix(int boneIndex) {
        return bindWorldMatrices[boneIndex];
    }

    public List<PartData> getParts() {
        return parts;
    }

    public float[] computeBounds() {
        float minX = Float.POSITIVE_INFINITY, minY = Float.POSITIVE_INFINITY, minZ = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY, maxZ = Float.NEGATIVE_INFINITY;
        for (PartData part : parts) {
            float[] pos = part.bindPositions;
            for (int i = 0; i < pos.length; i += 3) {
                float x = pos[i];
                float y = pos[i + 1];
                float z = pos[i + 2];
                if (x < minX) minX = x;
                if (x > maxX) maxX = x;
                if (y < minY) minY = y;
                if (y > maxY) maxY = y;
                if (z < minZ) minZ = z;
                if (z > maxZ) maxZ = z;
            }
        }
        return new float[]{minX, minY, minZ, maxX, maxY, maxZ};
    }

    public TerraSkinnedMeshInstance createInstance() {
        return new TerraSkinnedMeshInstance(this);
    }
}
