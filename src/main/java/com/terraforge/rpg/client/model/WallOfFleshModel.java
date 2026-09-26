package com.terraforge.rpg.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.terraforge.rpg.TerraForgeRPG;
import com.terraforge.rpg.boss.prehardmode.WallOfFleshEntity;
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
 * Authentic 3D Mesh Model for Wall of Flesh.
 * Integrates real 3D geometry from the approved library (90068304ebae4f4abd825acbca9ef468).
 * Completely eliminates the CubeListBuilder placeholder.
 */
public class WallOfFleshModel extends HierarchicalModel<WallOfFleshEntity> {

    public static final ResourceLocation MESH_LOCATION =
            ResourceLocation.fromNamespaceAndPath(TerraForgeRPG.MOD_ID, "models/entity/boss/wall_of_flesh.obj");

    private final ModelPart root;
    private float yaw;
    private float pitch;
    private float undulate;

    public WallOfFleshModel(ModelPart root) {
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
    public void setupAnim(WallOfFleshEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.yaw = (float) Math.toRadians(netHeadYaw);
        this.pitch = (float) Math.toRadians(headPitch);

        // Fleshy undulating breathing wall motion
        this.undulate = (float) Math.sin(ageInTicks * 0.15f) * 0.05f;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        poseStack.pushPose();

        poseStack.translate(0.0, 1.0, 0.0);
        poseStack.mulPose(new Quaternionf(new AxisAngle4f(-yaw, 0.0f, 1.0f, 0.0f)));

        float sx = 0.05f * (1.0f + undulate);
        float sy = 0.05f * (1.0f - undulate);
        float sz = 0.05f;
        poseStack.scale(sx, sy, sz);

        TerraMesh3D mesh = TerraMeshLoader.getOrLoad(MESH_LOCATION);

        float a = ((color >> 24) & 0xFF) / 255.0f;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        mesh.render(poseStack, buffer, packedLight, packedOverlay, r, g, b, a);

        poseStack.popPose();
    }
}
