package com.terraforge.rpg.client.render.mesh;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.terraforge.rpg.client.animation.skeletal.Bone;
import com.terraforge.rpg.client.animation.skeletal.Skeleton;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * High-performance CPU weighted linear blend skinning (LBS) mesh for TerraForge RPG.
 * Transforms vertices and normals per-frame using preallocated float buffers,
 * ensuring zero heap allocations and 60+ FPS stability during complex skeletal boss animations.
 */
public class TerraSkinnedMesh {

    public static class SkinnedPart {
        public final String name;
        public final int vertexCount;
        public final float[] bindPositions; // [x, y, z] * vertexCount
        public final float[] bindNormals;   // [nx, ny, nz] * vertexCount
        public final float[] uvs;           // [u, v] * vertexCount
        public final int[] boneIndices;     // 4 bone indices per vertex
        public final float[] boneWeights;   // 4 weights per vertex
        public final int[] indices;         // triangle indices

        // Preallocated mutable working buffers for zero-allocation skinning
        public final float[] skinnedPositions;
        public final float[] skinnedNormals;

        public SkinnedPart(String name, int vertexCount, float[] bindPositions, float[] bindNormals,
                           float[] uvs, int[] boneIndices, float[] boneWeights, int[] indices) {
            this.name = name;
            this.vertexCount = vertexCount;
            this.bindPositions = bindPositions;
            this.bindNormals = bindNormals;
            this.uvs = uvs;
            this.boneIndices = boneIndices;
            this.boneWeights = boneWeights;
            this.indices = indices;

            this.skinnedPositions = new float[vertexCount * 3];
            this.skinnedNormals = new float[vertexCount * 3];

            // Initialize skinned buffers with rest pose
            System.arraycopy(bindPositions, 0, this.skinnedPositions, 0, bindPositions.length);
            System.arraycopy(bindNormals, 0, this.skinnedNormals, 0, bindNormals.length);
        }

        public void skin(Matrix4f[] palette) {
            for (int v = 0; v < vertexCount; v++) {
                int v3 = v * 3;
                int v4 = v * 4;

                float bx = bindPositions[v3];
                float by = bindPositions[v3 + 1];
                float bz = bindPositions[v3 + 2];

                float bnx = bindNormals[v3];
                float bny = bindNormals[v3 + 1];
                float bnz = bindNormals[v3 + 2];

                float px = 0.0f, py = 0.0f, pz = 0.0f;
                float nx = 0.0f, ny = 0.0f, nz = 0.0f;

                for (int k = 0; k < 4; k++) {
                    float w = boneWeights[v4 + k];
                    if (w > 0.0001f) {
                        int bIdx = boneIndices[v4 + k];
                        if (bIdx >= 0 && bIdx < palette.length) {
                            Matrix4f m = palette[bIdx];

                            // Position transformation (affine)
                            px += w * (m.m00() * bx + m.m10() * by + m.m20() * bz + m.m30());
                            py += w * (m.m01() * bx + m.m11() * by + m.m21() * bz + m.m31());
                            pz += w * (m.m02() * bx + m.m12() * by + m.m22() * bz + m.m32());

                            // Normal transformation (rotation only)
                            nx += w * (m.m00() * bnx + m.m10() * bny + m.m20() * bnz);
                            ny += w * (m.m01() * bnx + m.m11() * bny + m.m21() * bnz);
                            nz += w * (m.m02() * bnx + m.m12() * bny + m.m22() * bnz);
                        }
                    }
                }

                // Normalize transformed normal
                float lenSq = nx * nx + ny * ny + nz * nz;
                if (lenSq > 0.00001f) {
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

    private final String name;
    private final List<String> boneNames;
    private final List<SkinnedPart> parts;
    private final Matrix4f[] bonePalette;
    private final Matrix4f identityMatrix = new Matrix4f();

    public TerraSkinnedMesh(String name, List<String> boneNames, List<SkinnedPart> parts) {
        this.name = name;
        this.boneNames = new ArrayList<>(boneNames);
        this.parts = new ArrayList<>(parts);
        this.bonePalette = new Matrix4f[boneNames.size()];
        for (int i = 0; i < bonePalette.length; i++) {
            bonePalette[i] = new Matrix4f();
        }
    }

    public String getName() {
        return name;
    }

    public List<String> getBoneNames() {
        return Collections.unmodifiableList(boneNames);
    }

    public List<SkinnedPart> getParts() {
        return Collections.unmodifiableList(parts);
    }

    public void skin(Skeleton skeleton) {
        for (int i = 0; i < boneNames.size(); i++) {
            Bone bone = skeleton.getBone(boneNames.get(i));
            if (bone != null) {
                bonePalette[i].set(bone.skinMatrix);
            } else {
                bonePalette[i].set(identityMatrix);
            }
        }

        for (SkinnedPart part : parts) {
            part.skin(bonePalette);
        }
    }

    public void render(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay,
                       float r, float g, float b, float a) {
        for (SkinnedPart part : parts) {
            part.render(poseStack, consumer, packedLight, packedOverlay, r, g, b, a);
        }
    }

    public void render(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay) {
        render(poseStack, consumer, packedLight, packedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);
    }
}
