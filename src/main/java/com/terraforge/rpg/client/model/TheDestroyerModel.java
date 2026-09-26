package com.terraforge.rpg.client.model;

import com.terraforge.rpg.boss.hardmode.TheDestroyerEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * 3D Model for The Destroyer mechanical boss.
 * Replaces the generic GhastModel placeholder with a mechanical segmented worm body,
 * glowing red probe socket core, and articulated steel mandibles.
 */
public class TheDestroyerModel extends HierarchicalModel<TheDestroyerEntity> {
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart leftMandible;
    private final ModelPart rightMandible;
    private final ModelPart laserCore;
    private final ModelPart spineCrest;

    public TheDestroyerModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.leftMandible = this.body.getChild("left_mandible");
        this.rightMandible = this.body.getChild("right_mandible");
        this.laserCore = this.body.getChild("laser_core");
        this.spineCrest = this.body.getChild("spine_crest");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition rootPart = mesh.getRoot();

        // Heavy Armored Cylinder Segment (16x16x16)
        PartDefinition body = rootPart.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F)
                        .texOffs(0, 32).addBox(-9.0F, -9.0F, -7.0F, 18.0F, 18.0F, 14.0F),
                PartPose.offset(0.0F, 16.0F, 0.0F));

        // Central Red Laser Core / Probe Socket
        body.addOrReplaceChild("laser_core",
                CubeListBuilder.create()
                        .texOffs(32, 0).addBox(-3.0F, -3.0F, -10.0F, 6.0F, 6.0F, 3.0F),
                PartPose.ZERO);

        // Top Armored Spine Crest
        body.addOrReplaceChild("spine_crest",
                CubeListBuilder.create()
                        .texOffs(40, 20).addBox(-2.0F, -12.0F, -6.0F, 4.0F, 4.0F, 12.0F),
                PartPose.ZERO);

        // Articulated Steel Mandibles
        body.addOrReplaceChild("left_mandible",
                CubeListBuilder.create()
                        .texOffs(0, 24).addBox(-2.0F, -3.0F, -14.0F, 3.0F, 6.0F, 8.0F),
                PartPose.offset(-7.0F, 0.0F, 0.0F));

        body.addOrReplaceChild("right_mandible",
                CubeListBuilder.create()
                        .texOffs(0, 24).mirror().addBox(-1.0F, -3.0F, -14.0F, 3.0F, 6.0F, 8.0F),
                PartPose.offset(7.0F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(TheDestroyerEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.body.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.body.xRot = headPitch * ((float) Math.PI / 180F);

        // Mandible drilling & clamping animation
        float clamp = (float) Math.sin(ageInTicks * 0.35F) * 0.25F;
        this.leftMandible.yRot = clamp;
        this.rightMandible.yRot = -clamp;

        // Mechanical pulse
        float pulse = (float) Math.sin(ageInTicks * 0.5F) * 0.05F;
        this.laserCore.z = pulse;
    }
}
