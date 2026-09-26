package com.terraforge.rpg.client.render.mesh;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.terraforge.rpg.client.animation.skeletal.Skeleton;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Backwards-compatible facade combining immutable {@link TerraSkinnedMeshData}
 * and a default {@link TerraSkinnedMeshInstance}.
 */
public class TerraSkinnedMesh {

    private final TerraSkinnedMeshData data;
    private final TerraSkinnedMeshInstance defaultInstance;

    public TerraSkinnedMesh(String name, List<String> boneNames, List<TerraSkinnedMeshData.PartData> parts) {
        this(new TerraSkinnedMeshData(name, "", boneNames, null, parts));
    }

    public TerraSkinnedMesh(TerraSkinnedMeshData data) {
        this.data = data;
        this.defaultInstance = data.createInstance();
    }

    public TerraSkinnedMeshData getData() {
        return data;
    }

    public TerraSkinnedMeshInstance createInstance() {
        return data.createInstance();
    }

    public String getName() {
        return data.getName();
    }

    public List<String> getBoneNames() {
        return data.getBoneNames();
    }

    public void skin(Skeleton skeleton) {
        defaultInstance.skin(skeleton);
    }

    public void render(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay,
                       float r, float g, float b, float a) {
        defaultInstance.render(poseStack, consumer, packedLight, packedOverlay, r, g, b, a);
    }

    public void render(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay) {
        defaultInstance.render(poseStack, consumer, packedLight, packedOverlay);
    }
}
