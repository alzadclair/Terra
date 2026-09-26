package com.terraforge.rpg.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.terraforge.rpg.TerraForgeRPG;
import com.terraforge.rpg.boss.endgame.MoonLordEntity;
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
 * Authentic 3D Mesh Model for Moon Lord celestial final boss.
 * Integrates the high-poly decimated 3D model (f6f46d9da39d4b7b8a5a3df5459f4bf0).
 * Completely eliminates the CubeListBuilder placeholder.
 */
public class MoonLordModel extends HierarchicalModel<MoonLordEntity> {

    public static final ResourceLocation MESH_LOCATION =
            ResourceLocation.fromNamespaceAndPath(TerraForgeRPG.MOD_ID, "models/entity/boss/moon_lord.obj");

    private final ModelPart root;
    private float yaw;
    private float pitch;
    private float hoverY;
    private float pulse;

    public MoonLordModel(ModelPart root) {
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
    public void setupAnim(MoonLordEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.yaw = (float) Math.toRadians(netHeadYaw);
        this.pitch = (float) Math.toRadians(headPitch);

        // Cosmic floating hover motion
        this.hoverY = (float) Math.sin(ageInTicks * 0.05f) * 0.25f;

        // Phantasmal core pulse
        this.pulse = 1.0f + (float) Math.sin(ageInTicks * 0.12f) * 0.03f;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        poseStack.pushPose();

        // Position monolithic upper body above ground
        poseStack.translate(0.0, 1.5 + hoverY, 0.0);

        // Apply entity orientation
        poseStack.mulPose(new Quaternionf(new AxisAngle4f(-yaw, 0.0f, 1.0f, 0.0f)));
        poseStack.mulPose(new Quaternionf(new AxisAngle4f(pitch * 0.5f, 1.0f, 0.0f, 0.0f)));

        // Scale geometry for colossal scale
        float baseScale = 0.035f * pulse;
        poseStack.scale(baseScale, baseScale, baseScale);

        TerraMesh3D mesh = TerraMeshLoader.getOrLoad(MESH_LOCATION);

        float a = ((color >> 24) & 0xFF) / 255.0f;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        mesh.render(poseStack, buffer, packedLight, packedOverlay, r, g, b, a);

        poseStack.popPose();
    }
}
