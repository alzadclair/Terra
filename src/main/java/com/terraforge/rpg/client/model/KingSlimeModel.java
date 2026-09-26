package com.terraforge.rpg.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.terraforge.rpg.TerraForgeRPG;
import com.terraforge.rpg.client.render.mesh.TerraMesh3D;
import com.terraforge.rpg.client.render.mesh.TerraMeshLoader;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * Authentic 3D Mesh Model for King Slime boss.
 * Integrates real 3D geometry from approved library (36f21229c93a4f23a7ac9a06c7994aaa / 6c0ed679476541579e441a2043c84c3d).
 * Completely eliminates the CubeListBuilder placeholder.
 */
public class KingSlimeModel<T extends Entity> extends HierarchicalModel<T> {

    public static final ResourceLocation MESH_LOCATION =
            ResourceLocation.fromNamespaceAndPath(TerraForgeRPG.MOD_ID, "models/entity/boss/king_slime.obj");

    private final ModelPart root;
    private float squashStretchX = 1.0f;
    private float squashStretchY = 1.0f;

    public KingSlimeModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        return LayerDefinition.create(mesh, 16, 16);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Dynamic Gel squash and stretch pulsation
        float jumpCycle = (float) Math.sin(ageInTicks * 0.25f);
        if (jumpCycle > 0.0f) {
            // Stretch upward when leaping
            this.squashStretchY = 1.0f + jumpCycle * 0.18f;
            this.squashStretchX = 1.0f - jumpCycle * 0.09f;
        } else {
            // Squash downward when impacting ground
            this.squashStretchY = 1.0f + jumpCycle * 0.15f;
            this.squashStretchX = 1.0f - jumpCycle * 0.12f;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        poseStack.pushPose();

        poseStack.translate(0.0, 0.5, 0.0);
        float baseScale = 0.045f;
        poseStack.scale(baseScale * squashStretchX, baseScale * squashStretchY, baseScale * squashStretchX);

        TerraMesh3D mesh = TerraMeshLoader.getOrLoad(MESH_LOCATION);

        float a = ((color >> 24) & 0xFF) / 255.0f;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        mesh.render(poseStack, buffer, packedLight, packedOverlay, r, g, b, a);

        poseStack.popPose();
    }
}
