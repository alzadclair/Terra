package com.terraforge.rpg.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.terraforge.rpg.TerraForgeRPG;
import com.terraforge.rpg.client.render.mesh.TerraMesh3D;
import com.terraforge.rpg.client.render.mesh.TerraMeshLoader;
import com.terraforge.rpg.entity.mob.FaceMonsterEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

/**
 * Authentic 3D Mesh Model for Face Monster Crimson mob.
 * Integrates real 3D geometry from approved library (edd6df55e59f47a587b59b59358bb8d3).
 * Completely eliminates the CubeListBuilder placeholder.
 */
public class FaceMonsterModel extends HierarchicalModel<FaceMonsterEntity> {

    public static final ResourceLocation MESH_LOCATION =
            ResourceLocation.fromNamespaceAndPath(TerraForgeRPG.MOD_ID, "models/entity/mob/face_monster.obj");

    private final ModelPart root;
    private float yaw;
    private float pitch;
    private float limbSwing;
    private float limbSwingAmount;

    public FaceMonsterModel(ModelPart root) {
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
    public void setupAnim(FaceMonsterEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.yaw = (float) Math.toRadians(netHeadYaw);
        this.pitch = (float) Math.toRadians(headPitch);
        this.limbSwing = limbSwing;
        this.limbSwingAmount = limbSwingAmount;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        poseStack.pushPose();

        poseStack.translate(0.0, 1.2, 0.0);
        poseStack.mulPose(new Quaternionf(new AxisAngle4f(-yaw, 0.0f, 1.0f, 0.0f)));

        // Hunched stalking motion
        float stalkWobble = (float) Math.sin(limbSwing * 0.6662f) * 0.1f * limbSwingAmount;
        poseStack.mulPose(new Quaternionf(new AxisAngle4f(0.15f + stalkWobble, 1.0f, 0.0f, 0.0f)));

        float scale = 0.035f;
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
