package com.terraforge.rpg.client.model;

import com.terraforge.rpg.boss.endgame.MoonLordEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * 3D Model for Moon Lord celestial final boss.
 * Replaces the generic TitanBossModel placeholder with an eldritch cephaloid deity,
 * glowing core, cranial True Eye, and articulated hands with palm eyes.
 */
public class MoonLordModel extends HierarchicalModel<MoonLordEntity> {
    private final ModelPart root;
    private final ModelPart torso;
    private final ModelPart heartCore;
    private final ModelPart head;
    private final ModelPart foreheadEye;
    private final ModelPart leftArm;
    private final ModelPart leftHand;
    private final ModelPart leftPalmEye;
    private final ModelPart rightArm;
    private final ModelPart rightHand;
    private final ModelPart rightPalmEye;
    private final ModelPart tentacles;

    public MoonLordModel(ModelPart root) {
        this.root = root;
        this.torso = root.getChild("torso");
        this.heartCore = this.torso.getChild("heart_core");
        this.head = this.torso.getChild("head");
        this.foreheadEye = this.head.getChild("forehead_eye");
        this.tentacles = this.head.getChild("tentacles");

        this.leftArm = this.torso.getChild("left_arm");
        this.leftHand = this.leftArm.getChild("left_hand");
        this.leftPalmEye = this.leftHand.getChild("left_palm_eye");

        this.rightArm = this.torso.getChild("right_arm");
        this.rightHand = this.rightArm.getChild("right_hand");
        this.rightPalmEye = this.rightHand.getChild("right_palm_eye");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition rootPart = mesh.getRoot();

        // Massive Upper Torso (24x20x14)
        PartDefinition torso = rootPart.addOrReplaceChild("torso",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-12.0F, -20.0F, -7.0F, 24.0F, 20.0F, 14.0F)
                        .texOffs(64, 0).addBox(-10.0F, -18.0F, -8.0F, 20.0F, 16.0F, 2.0F), // Ribcage
                PartPose.offset(0.0F, 12.0F, 0.0F));

        // Phantasmal Core in chest center
        torso.addOrReplaceChild("heart_core",
                CubeListBuilder.create()
                        .texOffs(0, 34).addBox(-4.0F, -14.0F, -9.0F, 8.0F, 8.0F, 4.0F),
                PartPose.ZERO);

        // Head with alien cranial crest
        PartDefinition head = torso.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 48).addBox(-7.0F, -16.0F, -7.0F, 14.0F, 16.0F, 14.0F)
                        .texOffs(56, 48).addBox(-9.0F, -20.0F, -8.0F, 18.0F, 6.0F, 16.0F), // Crest
                PartPose.offset(0.0F, -20.0F, 0.0F));

        // Forehead True Eye of Cthulhu socket
        head.addOrReplaceChild("forehead_eye",
                CubeListBuilder.create()
                        .texOffs(24, 34).addBox(-3.0F, -12.0F, -9.0F, 6.0F, 6.0F, 3.0F),
                PartPose.ZERO);

        // Beard Tentacles
        head.addOrReplaceChild("tentacles",
                CubeListBuilder.create()
                        .texOffs(42, 34).addBox(-5.0F, 0.0F, -6.0F, 10.0F, 10.0F, 3.0F),
                PartPose.ZERO);

        // Left Arm & Hand with Palm Eye
        PartDefinition leftArm = torso.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(0, 78).addBox(0.0F, -2.0F, -4.0F, 8.0F, 18.0F, 8.0F),
                PartPose.offset(12.0F, -16.0F, 0.0F));

        PartDefinition leftHand = leftArm.addOrReplaceChild("left_hand",
                CubeListBuilder.create()
                        .texOffs(32, 78).addBox(-1.0F, 16.0F, -5.0F, 10.0F, 12.0F, 10.0F),
                PartPose.ZERO);

        leftHand.addOrReplaceChild("left_palm_eye",
                CubeListBuilder.create()
                        .texOffs(72, 78).addBox(2.0F, 19.0F, -6.0F, 4.0F, 4.0F, 2.0F),
                PartPose.ZERO);

        // Right Arm & Hand with Palm Eye
        PartDefinition rightArm = torso.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(0, 78).mirror().addBox(-8.0F, -2.0F, -4.0F, 8.0F, 18.0F, 8.0F),
                PartPose.offset(-12.0F, -16.0F, 0.0F));

        PartDefinition rightHand = rightArm.addOrReplaceChild("right_hand",
                CubeListBuilder.create()
                        .texOffs(32, 78).mirror().addBox(-9.0F, 16.0F, -5.0F, 10.0F, 12.0F, 10.0F),
                PartPose.ZERO);

        rightHand.addOrReplaceChild("right_palm_eye",
                CubeListBuilder.create()
                        .texOffs(72, 78).mirror().addBox(-6.0F, 19.0F, -6.0F, 4.0F, 4.0F, 2.0F),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(MoonLordEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Head tracking
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F) * 0.6F;
        this.head.xRot = headPitch * ((float) Math.PI / 180F) * 0.6F;

        // Floating hover / breathing animation
        float hover = (float) Math.sin(ageInTicks * 0.1F) * 1.5F;
        this.torso.y = 12.0F + hover;

        // Arm floating and menacing idle
        float armSway = (float) Math.sin(ageInTicks * 0.08F) * 0.15F;
        this.leftArm.zRot = 0.2F + armSway;
        this.rightArm.zRot = -0.2F - armSway;

        this.leftArm.xRot = (float) Math.cos(ageInTicks * 0.08F) * 0.1F;
        this.rightArm.xRot = (float) Math.sin(ageInTicks * 0.08F) * 0.1F;

        // Beard tentacle undulating
        float tentacleWiggle = (float) Math.sin(ageInTicks * 0.2F) * 0.25F;
        this.tentacles.xRot = tentacleWiggle;

        // Heart core pulsating
        float pulse = (float) Math.abs(Math.sin(ageInTicks * 0.25F)) * 0.1F;
        this.heartCore.z = -pulse;
    }
}
