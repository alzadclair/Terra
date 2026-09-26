package com.terraforge.rpg.client.model;

import com.terraforge.rpg.boss.BossPhase;
import com.terraforge.rpg.boss.endgame.GolemEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Original TerraForge 3D Model for Golem (Lihzahrd Temple Guardian).
 * Features solid monolithic stone base, carved torso with sun altar core,
 * piston-driven fists, and detached hovering laser head in Phase 2.
 */
public class GolemModel<T extends GolemEntity> extends HierarchicalModel<T> {

    private final ModelPart root;
    private final ModelPart base;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart rightFist;
    private final ModelPart leftArm;
    private final ModelPart leftFist;

    public GolemModel(ModelPart root) {
        this.root = root;
        this.base = root.getChild("base");
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.rightArm = this.body.getChild("right_arm");
        this.rightFist = this.rightArm.getChild("right_fist");
        this.leftArm = this.body.getChild("left_arm");
        this.leftFist = this.leftArm.getChild("left_fist");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition rootPart = mesh.getRoot();

        // Monolithic stone altar pedestal / lower legs
        rootPart.addOrReplaceChild("base",
                CubeListBuilder.create()
                        .texOffs(0, 72)
                        .addBox(-14.0F, 8.0F, -10.0F, 28.0F, 16.0F, 20.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Massive carved brick torso
        PartDefinition body = rootPart.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 36)
                        .addBox(-12.0F, -10.0F, -8.0F, 24.0F, 18.0F, 16.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Detachable Lihzahrd idol head
        rootPart.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-8.0F, -14.0F, -7.0F, 16.0F, 14.0F, 14.0F),
                PartPose.offset(0.0F, -10.0F, 0.0F));

        // Right arm and piston fist
        PartDefinition rightArm = body.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(64, 0)
                        .addBox(-6.0F, -2.0F, -4.0F, 6.0F, 16.0F, 8.0F),
                PartPose.offset(-12.0F, -6.0F, 0.0F));

        rightArm.addOrReplaceChild("right_fist",
                CubeListBuilder.create()
                        .texOffs(64, 28)
                        .addBox(-7.0F, 14.0F, -6.0F, 8.0F, 10.0F, 12.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Left arm and piston fist
        PartDefinition leftArm = body.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(96, 0).mirror()
                        .addBox(0.0F, -2.0F, -4.0F, 6.0F, 16.0F, 8.0F),
                PartPose.offset(12.0F, -6.0F, 0.0F));

        leftArm.addOrReplaceChild("left_fist",
                CubeListBuilder.create()
                        .texOffs(96, 28).mirror()
                        .addBox(-1.0F, 14.0F, -6.0F, 8.0F, 10.0F, 12.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        boolean isPhase2 = entity.getCurrentPhase() == BossPhase.PHASE_2;

        if (isPhase2) {
            // Detached head hovering above the stone body in Phase 2
            this.head.y = -22.0F + (float) Math.sin(ageInTicks * 0.15F) * 3.5F;
            this.head.z = (float) Math.cos(ageInTicks * 0.12F) * 2.0F;
            this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
            this.head.xRot = headPitch * ((float) Math.PI / 180F);
            this.head.zRot = (float) Math.sin(ageInTicks * 0.1F) * 0.08F;
        } else {
            // Firmly attached to the shoulders in Phase 1
            this.head.y = -10.0F;
            this.head.z = 0.0F;
            this.head.zRot = 0.0F;
            this.head.yRot = netHeadYaw * ((float) Math.PI / 180F) * 0.6F;
            this.head.xRot = headPitch * ((float) Math.PI / 180F) * 0.6F;
        }

        // Torso breathing/vibrating with ancient power
        this.body.y = (float) Math.sin(ageInTicks * 0.05F) * 0.4F;

        // Piston punch arms
        float walkFactor = limbSwingAmount;
        float idlePunch = (float) Math.sin(ageInTicks * 0.08F);

        this.rightArm.xRot = (-0.1F + (float) Math.sin(limbSwing * 0.5F) * walkFactor * 0.8F) + idlePunch * 0.15F;
        this.leftArm.xRot = (-0.1F - (float) Math.sin(limbSwing * 0.5F) * walkFactor * 0.8F) - idlePunch * 0.15F;

        // Fists thrust forward during attacks
        this.rightFist.z = Math.min(0.0F, (float) Math.sin(ageInTicks * 0.18F) * -6.0F);
        this.leftFist.z = Math.min(0.0F, (float) Math.sin(ageInTicks * 0.18F + Math.PI) * -6.0F);
    }
}
