package com.terraforge.rpg.client.model;

import com.terraforge.rpg.boss.BossPhase;
import com.terraforge.rpg.boss.prehardmode.WallOfFleshEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * 3D Model for the Wall of Flesh boss.
 * Replaces the generic GhastModel placeholder with a monolithic fleshy wall,
 * articulated top and bottom laser eyes, gaping fanged maw, and trailing hungry tendrils.
 */
public class WallOfFleshModel extends HierarchicalModel<WallOfFleshEntity> {
    private final ModelPart root;
    private final ModelPart wall;
    private final ModelPart topEye;
    private final ModelPart bottomEye;
    private final ModelPart upperJaw;
    private final ModelPart lowerJaw;
    private final ModelPart tendril1;
    private final ModelPart tendril2;
    private final ModelPart tendril3;
    private final ModelPart tendril4;

    public WallOfFleshModel(ModelPart root) {
        this.root = root;
        this.wall = root.getChild("wall");
        this.topEye = this.wall.getChild("top_eye");
        this.bottomEye = this.wall.getChild("bottom_eye");
        this.upperJaw = this.wall.getChild("upper_jaw");
        this.lowerJaw = this.wall.getChild("lower_jaw");
        this.tendril1 = this.wall.getChild("tendril1");
        this.tendril2 = this.wall.getChild("tendril2");
        this.tendril3 = this.wall.getChild("tendril3");
        this.tendril4 = this.wall.getChild("tendril4");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition rootPart = mesh.getRoot();

        // Central Flesh Wall (Huge monolithic slab 16 wide, 32 high, 12 deep)
        PartDefinition wall = rootPart.addOrReplaceChild("wall",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-8.0F, -24.0F, -6.0F, 16.0F, 48.0F, 12.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Top Eye (Upper Laser Cannon)
        wall.addOrReplaceChild("top_eye",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-5.0F, -5.0F, -10.0F, 10.0F, 10.0F, 6.0F)
                        .texOffs(32, 0).addBox(-2.0F, -2.0F, -11.0F, 4.0F, 4.0F, 1.0F),
                PartPose.offset(0.0F, -16.0F, 0.0F));

        // Bottom Eye (Lower Laser Cannon)
        wall.addOrReplaceChild("bottom_eye",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-5.0F, -5.0F, -10.0F, 10.0F, 10.0F, 6.0F)
                        .texOffs(32, 0).addBox(-2.0F, -2.0F, -11.0F, 4.0F, 4.0F, 1.0F),
                PartPose.offset(0.0F, 16.0F, 0.0F));

        // Central Gaping Maw - Upper Jaw with razor fangs
        wall.addOrReplaceChild("upper_jaw",
                CubeListBuilder.create()
                        .texOffs(0, 20).addBox(-6.0F, -4.0F, -12.0F, 12.0F, 4.0F, 8.0F)
                        .texOffs(40, 20).addBox(-5.0F, 0.0F, -11.0F, 10.0F, 3.0F, 1.0F),
                PartPose.offset(0.0F, -2.0F, 0.0F));

        // Central Gaping Maw - Lower Jaw with razor fangs
        wall.addOrReplaceChild("lower_jaw",
                CubeListBuilder.create()
                        .texOffs(0, 32).addBox(-6.0F, 0.0F, -12.0F, 12.0F, 4.0F, 8.0F)
                        .texOffs(40, 24).addBox(-5.0F, -3.0F, -11.0F, 10.0F, 3.0F, 1.0F),
                PartPose.offset(0.0F, 2.0F, 0.0F));

        // Trailing flesh roots and tentacles
        wall.addOrReplaceChild("tendril1",
                CubeListBuilder.create().texOffs(48, 0).addBox(-2.0F, -2.0F, 6.0F, 4.0F, 4.0F, 14.0F),
                PartPose.offset(-5.0F, -10.0F, 0.0F));

        wall.addOrReplaceChild("tendril2",
                CubeListBuilder.create().texOffs(48, 0).addBox(-2.0F, -2.0F, 6.0F, 4.0F, 4.0F, 14.0F),
                PartPose.offset(5.0F, -10.0F, 0.0F));

        wall.addOrReplaceChild("tendril3",
                CubeListBuilder.create().texOffs(48, 0).addBox(-2.0F, -2.0F, 6.0F, 4.0F, 4.0F, 14.0F),
                PartPose.offset(-5.0F, 10.0F, 0.0F));

        wall.addOrReplaceChild("tendril4",
                CubeListBuilder.create().texOffs(48, 0).addBox(-2.0F, -2.0F, 6.0F, 4.0F, 4.0F, 14.0F),
                PartPose.offset(5.0F, 10.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(WallOfFleshEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float eyeYaw = netHeadYaw * ((float) Math.PI / 180F) * 0.5F;
        float eyePitch = headPitch * ((float) Math.PI / 180F) * 0.5F;

        this.topEye.yRot = eyeYaw;
        this.topEye.xRot = eyePitch;
        this.bottomEye.yRot = eyeYaw;
        this.bottomEye.xRot = eyePitch;

        // Animated biting maw
        boolean isEnraged = entity.getCurrentPhase() == BossPhase.PHASE_2;
        float biteSpeed = isEnraged ? 0.6F : 0.25F;
        float bite = (float) Math.abs(Math.sin(ageInTicks * biteSpeed)) * 0.3F;
        this.upperJaw.xRot = -bite;
        this.lowerJaw.xRot = bite;

        // Trailing tendril writhing
        float wiggle1 = (float) Math.sin(ageInTicks * 0.15F) * 0.2F;
        float wiggle2 = (float) Math.cos(ageInTicks * 0.15F) * 0.2F;
        this.tendril1.xRot = wiggle1;
        this.tendril2.xRot = -wiggle1;
        this.tendril3.yRot = wiggle2;
        this.tendril4.yRot = -wiggle2;
    }
}
