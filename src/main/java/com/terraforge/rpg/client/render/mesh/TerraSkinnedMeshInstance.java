package com.terraforge.rpg.client.render.mesh;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.terraforge.rpg.client.animation.skeletal.Bone;
import com.terraforge.rpg.client.animation.skeletal.Skeleton;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mutable, per-entity/per-model instance of a skinned mesh.
 * Owns preallocated working buffers for bone matrices, normal matrices, and deformed vertex/normal positions.
 * Ensures zero heap allocations during the render loop and total thread/instance isolation when multiple
 * entities share the same underlying immutable {@link TerraSkinnedMeshData}.
 */
public class TerraSkinnedMeshInstance {

    public static class PartInstance {
        public final TerraSkinnedMeshData.PartData data;
        public final float[] skinnedPositions;
        public final float[] skinnedNormals;

        public PartInstance(TerraSkinnedMeshData.PartData data) {
            this.data = data;
            this.skinnedPositions = new float[data.vertexCount * 3];
            this.skinnedNormals = new float[data.vertexCount * 3];

            // Initialize with bind pose geometry
            System.arraycopy(data.bindPositions, 0, this.skinnedPositions, 0, data.bindPositions.length);
            System.arraycopy(data.bindNormals, 0, this.skinnedNormals, 0, data.bindNormals.length);
        }

        public void skin(Matrix4f[] bonePalette, Matrix3f[] normalPalette) {
            int vertexCount = data.vertexCount;
            float[] bindPos = data.bindPositions;
            float[] bindNorm = data.bindNormals;
            int[] bIndices = data.boneIndices;
            float[] bWeights = data.boneWeights;

            for (int v = 0; v < vertexCount; v++) {
                int v3 = v * 3;
                int v4 = v * 4;

                float bx = bindPos[v3];
                float by = bindPos[v3 + 1];
                float bz = bindPos[v3 + 2];

                float bnx = bindNorm[v3];
                float bny = bindNorm[v3 + 1];
                float bnz = bindNorm[v3 + 2];

                float px = 0.0f, py = 0.0f, pz = 0.0f;
                float nx = 0.0f, ny = 0.0f, nz = 0.0f;

                for (int k = 0; k < 4; k++) {
                    float w = bWeights[v4 + k];
                    if (w > 0.0001f) {
                        int bIdx = bIndices[v4 + k];
                        if (bIdx >= 0 && bIdx < bonePalette.length) {
                            Matrix4f m = bonePalette[bIdx];
                            Matrix3f nm = normalPalette[bIdx];

                            // Position transformation (affine)
                            px += w * (m.m00() * bx + m.m10() * by + m.m20() * bz + m.m30());
                            py += w * (m.m01() * bx + m.m11() * by + m.m21() * bz + m.m31());
                            pz += w * (m.m02() * bx + m.m12() * by + m.m22() * bz + m.m32());

                            // Normal transformation (via inverse-transpose normal matrix (M^3x3)^-T)
                            nx += w * (nm.m00 * bnx + nm.m10 * bny + nm.m20 * bnz);
                            ny += w * (nm.m01 * bnx + nm.m11 * bny + nm.m21 * bnz);
                            nz += w * (nm.m02 * bnx + nm.m12 * bny + nm.m22 * bnz);
                        }
                    }
                }

                // Re-normalize skinned normal
                float lenSq = nx * nx + ny * ny + nz * nz;
                if (lenSq > 1.0e-8f) {
                    float invLen = (float) (1.0 / Math.sqrt(lenSq));
                    nx *= invLen;
                    ny *= invLen;
                    nz *= invLen;
                } else {
                    ny = 1.0f;
                }

                skinnedPositions[v3] = px;
                skinnedPositions[v3 + 1] = py;
                skinnedPositions[v3 + 2] = pz;

                skinnedNormals[v3] = nx;
                skinnedNormals[v3 + 1] = ny;
                skinnedNormals[v3 + 2] = nz;
            }
        }

        public void render(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay,
                           float r, float g, float b, float a) {
            Matrix4f pose = poseStack.last().pose();
            int red = (int) (r * 255.0f);
            int green = (int) (g * 255.0f);
            int blue = (int) (b * 255.0f);
            int alpha = (int) (a * 255.0f);

            int[] indices = data.indices;
            float[] uvs = data.uvs;

            for (int i = 0; i < indices.length; i++) {
                int idx = indices[i];
                int pIdx = idx * 3;
                int uIdx = idx * 2;

                float x = skinnedPositions[pIdx];
                float y = skinnedPositions[pIdx + 1];
                float z = skinnedPositions[pIdx + 2];

                float u = uvs[uIdx];
                float v = uvs[uIdx + 1];

                float nx = skinnedNormals[pIdx];
                float ny = skinnedNormals[pIdx + 1];
                float nz = skinnedNormals[pIdx + 2];

                consumer.addVertex(pose, x, y, z)
                        .setColor(red, green, blue, alpha)
                        .setUv(u, v)
                        .setOverlay(packedOverlay)
                        .setLight(packedLight)
                        .setNormal(poseStack.last(), nx, ny, nz);
            }
        }
    }

    private final TerraSkinnedMeshData meshData;
    private final Matrix4f[] bonePalette;
    private final Matrix3f[] normalPalette;
    private final List<PartInstance> parts;
    private final Matrix4f identityMatrix = new Matrix4f();

    public TerraSkinnedMeshInstance(TerraSkinnedMeshData meshData) {
        this.meshData = meshData;
        int boneCount = meshData.getBoneCount();
        this.bonePalette = new Matrix4f[boneCount];
        this.normalPalette = new Matrix3f[boneCount];

        for (int i = 0; i < boneCount; i++) {
            this.bonePalette[i] = new Matrix4f();
            this.normalPalette[i] = new Matrix3f();
        }

        List<PartInstance> partList = new ArrayList<>(meshData.getParts().size());
        for (TerraSkinnedMeshData.PartData partData : meshData.getParts()) {
            partList.add(new PartInstance(partData));
        }
        this.parts = Collections.unmodifiableList(partList);
    }

    public TerraSkinnedMeshData getMeshData() {
        return meshData;
    }

    public List<PartInstance> getParts() {
        return parts;
    }

    public Matrix4f[] getBonePalette() {
        return bonePalette;
    }

    public Matrix3f[] getNormalPalette() {
        return normalPalette;
    }

    /**
     * Deforms this mesh instance using the current skeletal pose.
     * Computes the bone skinning matrices S_i = M_bone * M_invBind and their
     * normal matrices N_i = (S_i^3x3)^-T with zero heap allocations.
     */
    public void skin(Skeleton skeleton) {
        List<String> boneNames = meshData.getBoneNames();
        for (int i = 0; i < bonePalette.length; i++) {
            Bone bone = skeleton != null ? skeleton.getBone(boneNames.get(i)) : null;
            if (bone != null) {
                bonePalette[i].set(bone.worldMatrix).mul(meshData.getInverseBindMatrix(i));
            } else {
                bonePalette[i].set(identityMatrix);
            }
            bonePalette[i].normal(normalPalette[i]);
        }

        for (PartInstance part : parts) {
            part.skin(bonePalette, normalPalette);
        }
    }

    private static volatile String debugSubmeshFilter = null;

    public static void setDebugSubmeshFilter(String filter) {
        debugSubmeshFilter = (filter == null || filter.trim().isEmpty() || filter.equalsIgnoreCase("ALL")) ? null : filter.trim();
    }

    public static String getDebugSubmeshFilter() {
        return debugSubmeshFilter != null ? debugSubmeshFilter : "ALL";
    }

    public static String getEffectiveDebugSubmeshFilter() {
        if (debugSubmeshFilter != null) {
            return debugSubmeshFilter;
        }
        String prop = System.getProperty("terraforge.debug.eye.submesh");
        return (prop != null && !prop.trim().isEmpty() && !prop.equalsIgnoreCase("ALL")) ? prop.trim() : null;
    }

    public boolean hasPartsWithMode(TerraSkinnedMeshData.RenderMode mode) {
        if (mode == null) return true;
        for (PartInstance part : parts) {
            if (part.data.renderMode == mode) return true;
        }
        return false;
    }

    public void render(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay,
                       float r, float g, float b, float a) {
        render(poseStack, consumer, packedLight, packedOverlay, r, g, b, a, null);
    }

    public void render(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay,
                       float r, float g, float b, float a, TerraSkinnedMeshData.RenderMode targetMode) {
        String filter = getEffectiveDebugSubmeshFilter();
        boolean hasFilter = (filter != null);

        for (PartInstance part : parts) {
            if (hasFilter) {
                if (!part.data.name.equalsIgnoreCase(filter)) {
                    continue;
                }
            } else if (targetMode != null && part.data.renderMode != targetMode) {
                continue;
            }
            part.render(poseStack, consumer, packedLight, packedOverlay, r, g, b, a);
        }
    }

    public void render(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay) {
        render(poseStack, consumer, packedLight, packedOverlay, 1.0f, 1.0f, 1.0f, 1.0f, null);
    }
}
