package com.terraforge.rpg.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.terraforge.rpg.TerraForgeRPG;
import com.terraforge.rpg.boss.prehardmode.EyeOfCthulhuEntity;
import com.terraforge.rpg.client.render.mesh.TerraMesh3D;
import com.terraforge.rpg.client.render.mesh.TerraMeshLoader;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

/**
 * Authentic 3D Mesh Model for Eye of Cthulhu.
 * Renders high-fidelity geometry from the approved 3D model library (ddf286114b384050b293cf1a00c77846
 * and rigs 06664c90cf3e4d24a74a43dc771ae74f / a53f70fa34284699a57af3d161182611).
 * Completely eliminates CubeListBuilder placeholder boxes.
 */
public class EyeOfCthulhuModel extends HierarchicalModel<EyeOfCthulhuEntity> {

    public static final ResourceLocation MESH_P1 =
            ResourceLocation.fromNamespaceAndPath(TerraForgeRPG.MOD_ID, "models/entity/boss/eye_of_cthulhu_p1.obj");
    public static final ResourceLocation MESH_P2 =
            ResourceLocation.fromNamespaceAndPath(TerraForgeRPG.MOD_ID, "models/entity/boss/eye_of_cthulhu_p2.obj");

    private final ModelPart root;
    private EyeOfCthulhuEntity activeEntity;
    private float roll;
    private float pitch;
    private float yaw;
    private float scaleMod = 1.0f;

    public EyeOfCthulhuModel(ModelPart root) {
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
    public void setupAnim(EyeOfCthulhuEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.activeEntity = entity;
        this.yaw = (float) Math.toRadians(netHeadYaw);
        this.pitch = (float) Math.toRadians(headPitch);

        boolean isPhase2 = entity.getCurrentPhase().phaseNumber() >= 2;

        // Dynamic tilt & breathing / pulsation
        float pulse = (float) Math.sin(ageInTicks * 0.15f) * 0.04f;
        this.scaleMod = 1.0f + pulse;

        // Dynamic roll banking during high-speed charge dashes
        double velX = entity.getDeltaMovement().x;
        double velZ = entity.getDeltaMovement().z;
        double horizontalSpeed = Math.sqrt(velX * velX + velZ * velZ);

        if (horizontalSpeed > 0.4) {
            this.roll = (float) Math.sin(ageInTicks * 0.5f) * (isPhase2 ? 0.25f : 0.12f);
        } else {
            this.roll = 0.0f;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        poseStack.pushPose();

        // Position model root in entity coordinate frame
        poseStack.translate(0.0, 1.0, 0.0);

        // Apply orientation rotations (yaw, pitch, bank roll)
        poseStack.mulPose(new Quaternionf(new AxisAngle4f(-yaw, 0.0f, 1.0f, 0.0f)));
        poseStack.mulPose(new Quaternionf(new AxisAngle4f(pitch, 1.0f, 0.0f, 0.0f)));
        if (roll != 0.0f) {
            poseStack.mulPose(new Quaternionf(new AxisAngle4f(roll, 0.0f, 0.0f, 1.0f)));
        }

        // Apply scale
        poseStack.scale(scaleMod * 0.04f, scaleMod * 0.04f, scaleMod * 0.04f);

        boolean isPhase2 = activeEntity != null && activeEntity.getCurrentPhase().phaseNumber() >= 2;
        ResourceLocation meshLoc = isPhase2 ? MESH_P2 : MESH_P1;
        TerraMesh3D mesh = TerraMeshLoader.getOrLoad(meshLoc);

        float a = ((color >> 24) & 0xFF) / 255.0f;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        mesh.render(poseStack, buffer, packedLight, packedOverlay, r, g, b, a);

        poseStack.popPose();
    }
}
