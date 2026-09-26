package com.terraforge.rpg.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Custom 3D Wearable Phoenix Wings model for TerraForge RPG.
 * Replaces vanilla Elytra geometry with multi-segmented fiery plumage:
 * - Central gold harness / spine anchor
 * - Left/Right articulated wing roots
 * - Primary swept feather blades
 * - Secondary lower radiant plumage
 *
 * Implements 5 procedural animation states:
 * - RETRACT: folded closely along spine when standing idle
 * - CROUCH: tucked tightly down during sneaking
 * - DEPLOY: flared open during sprinting and horizontal movement
 * - FLAP: dynamic upward harmonic wingstrokes during jumping/ascension
 * - GLIDE: swept back aerodynamic spread during gliding/falling
 */
public class PhoenixWingsModel extends Model {

    private final ModelPart root;
    private final ModelPart leftWing;
    private final ModelPart leftPrimary;
    private final ModelPart leftSecondary;
    private final ModelPart rightWing;
    private final ModelPart rightPrimary;
    private final ModelPart rightSecondary;

    public PhoenixWingsModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
        this.leftWing = root.getChild("left_wing");
        this.leftPrimary = leftWing.getChild("left_primary");
        this.leftSecondary = leftWing.getChild("left_secondary");
        this.rightWing = root.getChild("right_wing");
        this.rightPrimary = rightWing.getChild("right_primary");
        this.rightSecondary = rightWing.getChild("right_secondary");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Central gold & crimson harness attached to upper spine
        root.addOrReplaceChild("harness",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-2.5f, -1.0f, 0.0f, 5.0f, 6.0f, 2.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 2.0f, 2.0f));

        // Left Wing Hierarchy
        PartDefinition leftWing = root.addOrReplaceChild("left_wing",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(0.0f, -1.0f, 0.0f, 10.0f, 2.0f, 1.5f, new CubeDeformation(0.0f)),
                PartPose.offset(2.0f, 3.0f, 2.5f));

        leftWing.addOrReplaceChild("left_primary",
                CubeListBuilder.create()
                        .texOffs(0, 24)
                        .addBox(0.0f, -2.0f, 0.0f, 18.0f, 14.0f, 1.0f, new CubeDeformation(0.0f)),
                PartPose.offset(9.0f, 0.0f, 0.0f));

        leftWing.addOrReplaceChild("left_secondary",
                CubeListBuilder.create()
                        .texOffs(0, 42)
                        .addBox(0.0f, 0.0f, 0.0f, 14.0f, 16.0f, 1.0f, new CubeDeformation(0.0f)),
                PartPose.offset(4.0f, 2.0f, 0.5f));

        // Right Wing Hierarchy (Mirrored)
        PartDefinition rightWing = root.addOrReplaceChild("right_wing",
                CubeListBuilder.create()
                        .texOffs(32, 16)
                        .addBox(-10.0f, -1.0f, 0.0f, 10.0f, 2.0f, 1.5f, new CubeDeformation(0.0f)),
                PartPose.offset(-2.0f, 3.0f, 2.5f));

        rightWing.addOrReplaceChild("right_primary",
                CubeListBuilder.create()
                        .texOffs(32, 24)
                        .addBox(-18.0f, -2.0f, 0.0f, 18.0f, 14.0f, 1.0f, new CubeDeformation(0.0f)),
                PartPose.offset(-9.0f, 0.0f, 0.0f));

        rightWing.addOrReplaceChild("right_secondary",
                CubeListBuilder.create()
                        .texOffs(32, 42)
                        .addBox(-14.0f, 0.0f, 0.0f, 14.0f, 16.0f, 1.0f, new CubeDeformation(0.0f)),
                PartPose.offset(-4.0f, 2.0f, 0.5f));

        return LayerDefinition.create(mesh, 64, 64);
    }

    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        Vec3 delta = entity.getDeltaMovement();
        double horizontalSpeed = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        boolean isGrounded = entity.onGround();
        boolean isGliding = entity.isFallFlying() || (!isGrounded && delta.y < -0.15);
        boolean isAscending = !isGrounded && delta.y > 0.05;
        boolean isCrouching = entity.isCrouching();

        if (isGliding) {
            // GLIDE STATE: Swept back wide aerodynamic wingspan
            float rollTilt = (float) (delta.x * 0.4);
            leftWing.xRot = 0.25f;
            leftWing.yRot = -0.95f;
            leftWing.zRot = -0.30f + rollTilt;

            rightWing.xRot = 0.25f;
            rightWing.yRot = 0.95f;
            rightWing.zRot = 0.30f + rollTilt;

            leftPrimary.zRot = -0.15f;
            rightPrimary.zRot = 0.15f;
            leftSecondary.zRot = -0.10f;
            rightSecondary.zRot = 0.10f;

        } else if (isAscending) {
            // FLAP STATE: Harmonic rhythmic ascension wingstrokes
            float flapCycle = (float) Math.sin(ageInTicks * 0.75f);
            float flapLag = (float) Math.sin(ageInTicks * 0.75f - 0.35f);

            leftWing.xRot = 0.15f;
            leftWing.yRot = -0.65f - flapCycle * 0.45f;
            leftWing.zRot = flapCycle * 0.55f;

            rightWing.xRot = 0.15f;
            rightWing.yRot = 0.65f + flapCycle * 0.45f;
            rightWing.zRot = -flapCycle * 0.55f;

            leftPrimary.zRot = flapLag * 0.30f;
            rightPrimary.zRot = -flapLag * 0.30f;
            leftSecondary.zRot = flapLag * 0.40f;
            rightSecondary.zRot = -flapLag * 0.40f;

        } else if (isCrouching) {
            // CROUCH STATE: Tucked down close to hunched spine
            leftWing.xRot = 0.65f;
            leftWing.yRot = -0.18f;
            leftWing.zRot = -0.10f;

            rightWing.xRot = 0.65f;
            rightWing.yRot = 0.18f;
            rightWing.zRot = 0.10f;

            leftPrimary.zRot = 0.05f;
            rightPrimary.zRot = -0.05f;
            leftSecondary.zRot = 0.05f;
            rightSecondary.zRot = -0.05f;

        } else if (horizontalSpeed > 0.15) {
            // DEPLOY / RUN STATE: Flared plumage with gentle natural sway
            float sway = (float) Math.sin(ageInTicks * 0.35f) * 0.08f;
            leftWing.xRot = 0.10f + (float) (horizontalSpeed * 0.3);
            leftWing.yRot = -0.55f + sway;
            leftWing.zRot = -0.15f;

            rightWing.xRot = 0.10f + (float) (horizontalSpeed * 0.3);
            rightWing.yRot = 0.55f - sway;
            rightWing.zRot = 0.15f;

            leftPrimary.zRot = -0.10f;
            rightPrimary.zRot = 0.10f;
            leftSecondary.zRot = -0.05f;
            rightSecondary.zRot = 0.05f;

        } else {
            // RETRACT / IDLE STATE: Resting folded along back
            float breath = (float) Math.sin(ageInTicks * 0.1f) * 0.03f;
            leftWing.xRot = 0.40f + breath;
            leftWing.yRot = -0.15f;
            leftWing.zRot = -0.12f;

            rightWing.xRot = 0.40f + breath;
            rightWing.yRot = 0.15f;
            rightWing.zRot = 0.12f;

            leftPrimary.zRot = 0.05f;
            rightPrimary.zRot = -0.05f;
            leftSecondary.zRot = 0.05f;
            rightSecondary.zRot = -0.05f;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
