package com.terraforge.rpg.client.render.mesh;

import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable geometry and skeletal bind data for a 3D skinned mesh.
 * Loaded once from disk/resource pack and cached globally across all entities.
 * Thread-safe and read-only at runtime.
 */
public class TerraSkinnedMeshData {

    public static class PartData {
        public final String name;
        public final int vertexCount;
        public final float[] bindPositions;
        public final float[] bindNormals;
        public final float[] uvs;
        public final int[] boneIndices;
        public final float[] boneWeights;
        public final int[] indices;

        public PartData(String name, int vertexCount, float[] bindPositions, float[] bindNormals,
                        float[] uvs, int[] boneIndices, float[] boneWeights, int[] indices) {
            this.name = name;
            this.vertexCount = vertexCount;
            this.bindPositions = bindPositions;
            this.bindNormals = bindNormals;
            this.uvs = uvs;
            this.boneIndices = boneIndices;
            this.boneWeights = boneWeights;
            this.indices = indices;
        }
    }

    private final String name;
    private final String texture;
    private final List<String> boneNames;
    private final Matrix4f[] inverseBindMatrices;
    private final Matrix4f[] bindWorldMatrices;
    private final List<PartData> parts;

    public TerraSkinnedMeshData(String name, String texture, List<String> boneNames,
                                Matrix4f[] inverseBindMatrices, List<PartData> parts) {
        this.name = name;
        this.texture = texture;
        this.boneNames = Collections.unmodifiableList(new ArrayList<>(boneNames));
        this.parts = Collections.unmodifiableList(new ArrayList<>(parts));

        int boneCount = boneNames.size();
        this.inverseBindMatrices = new Matrix4f[boneCount];
        this.bindWorldMatrices = new Matrix4f[boneCount];

        for (int i = 0; i < boneCount; i++) {
            if (inverseBindMatrices != null && i < inverseBindMatrices.length && inverseBindMatrices[i] != null) {
                this.inverseBindMatrices[i] = new Matrix4f(inverseBindMatrices[i]);
            } else {
                this.inverseBindMatrices[i] = new Matrix4f().identity();
            }
            this.bindWorldMatrices[i] = new Matrix4f(this.inverseBindMatrices[i]).invert();
        }
    }

    public String getName() {
        return name;
    }

    public String getTexture() {
        return texture;
    }

    public List<String> getBoneNames() {
        return boneNames;
    }

    public int getBoneCount() {
        return boneNames.size();
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

    public TerraSkinnedMeshInstance createInstance() {
        return new TerraSkinnedMeshInstance(this);
    }
}
