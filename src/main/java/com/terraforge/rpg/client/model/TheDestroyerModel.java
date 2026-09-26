package com.terraforge.rpg.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.terraforge.rpg.TerraForgeRPG;
import com.terraforge.rpg.boss.hardmode.TheDestroyerEntity;
import com.terraforge.rpg.client.render.mesh.TerraMesh3D;
import com.terraforge.rpg.client.render.mesh.TerraMeshLoader;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

/**
 * Authentic 3D Mesh Model for The Destroyer mechanical boss.
 * Integrates real mechanical segment geometry from approved library (2576e274f05f4668bf2b8519d6789348).
 * Completely eliminates the CubeListBuilder placeholder.
 */
public class TheDestroyerModel extends HierarchicalModel<TheDestroyerEntity> {

    public static final ResourceLocation MESH_LOCATION =
            ResourceLocation.fromNamespaceAndPath(TerraForgeRPG.MOD_ID, "models/entity/boss/the_destroyer.obj");

    private final ModelPart root;
    private float yaw;
    private float pitch;
    private float roll;

    public TheDestroyerModel(ModelPart root) {
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
    public void setupAnim(TheDestroyerEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.yaw = (float) Math.toRadians(netHeadYaw);
        this.pitch = (float) Math.toRadians(headPitch);

        // Sinusoidal mechanical body wave roll
        this.roll = (float) Math.sin(ageInTicks * 0.2f) * 0.15f;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        poseStack.pushPose();

        poseStack.translate(0.0, 1.0, 0.0);
        poseStack.mulPose(new Quaternionf(new AxisAngle4f(-yaw, 0.0f, 1.0f, 0.0f)));
        poseStack.mulPose(new Quaternionf(new AxisAngle4f(pitch, 1.0f, 0.0f, 0.0f)));
        poseStack.mulPose(new Quaternionf(new AxisAngle4f(roll, 0.0f, 0.0f, 1.0f)));

        float scale = 0.04f;
        poseStack.scale(scale, scale, scale);

        TerraMesh3D mesh = TerraMeshLoader.getOrLoad(MESH_LOCATION);

        float a = ((color >> 24) & 0xFF) / 255.0f;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        mesh.render(poseStack, buffer, packedLight, packedOverlay, r, g, b, a);

        poseStack.popPose();
    }
}
