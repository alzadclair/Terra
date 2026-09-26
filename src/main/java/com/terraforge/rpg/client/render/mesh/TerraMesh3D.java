package com.terraforge.rpg.client.render.mesh;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * High-performance runtime 3D polygon mesh renderer for TerraForge RPG.
 * Reads real exported 3D geometry (vertices, normals, UVs, triangle indices)
 * and streams it directly to Minecraft's GPU VertexConsumer pipeline.
 */
public class TerraMesh3D {

    public static class Part {
        public final String name;
        public final float[] positions; // x, y, z triplets
        public final float[] uvs;       // u, v pairs
        public final float[] normals;   // nx, ny, nz triplets
        public final int[] indices;     // triangle vertex indices

        public Part(String name, float[] positions, float[] uvs, float[] normals, int[] indices) {
            this.name = name;
            this.positions = positions;
            this.uvs = uvs;
            this.normals = normals;
            this.indices = indices;
        }

        public void render(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, float r, float g, float b, float a) {
            Matrix4f pose = poseStack.last().pose();
            int red = (int) (r * 255.0f);
            int green = (int) (g * 255.0f);
            int blue = (int) (b * 255.0f);
            int alpha = (int) (a * 255.0f);

            for (int i = 0; i < indices.length; i++) {
                int idx = indices[i];
                int pIdx = idx * 3;
                int uIdx = idx * 2;
                int nIdx = idx * 3;

                float x = positions[pIdx];
                float y = positions[pIdx + 1];
                float z = positions[pIdx + 2];

                float u = uvs[uIdx];
                float v = uvs[uIdx + 1];

                float nx = (normals.length > nIdx + 2) ? normals[nIdx] : 0.0f;
                float ny = (normals.length > nIdx + 2) ? normals[nIdx + 1] : 1.0f;
                float nz = (normals.length > nIdx + 2) ? normals[nIdx + 2] : 0.0f;

                consumer.addVertex(pose, x, y, z)
                        .setColor(red, green, blue, alpha)
                        .setUv(u, v)
                        .setOverlay(packedOverlay)
                        .setLight(packedLight)
                        .setNormal(poseStack.last(), nx, ny, nz);
            }
        }
    }

    private final Map<String, Part> parts;

    public TerraMesh3D(Map<String, Part> parts) {
        this.parts = new HashMap<>(parts);
    }

    public Set<String> getPartNames() {
        return Collections.unmodifiableSet(parts.keySet());
    }

    public Part getPart(String name) {
        return parts.get(name);
    }

    public void render(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, float r, float g, float b, float a) {
        for (Part part : parts.values()) {
            part.render(poseStack, consumer, packedLight, packedOverlay, r, g, b, a);
        }
    }

    public void render(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay) {
        render(poseStack, consumer, packedLight, packedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);
    }
}
