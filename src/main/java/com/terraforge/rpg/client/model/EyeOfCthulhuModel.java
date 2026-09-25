package com.terraforge.rpg.client.model;

import com.terraforge.rpg.boss.BossPhase;
import com.terraforge.rpg.boss.prehardmode.EyeOfCthulhuEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Blockbench-modeled Eye of Cthulhu boss.
 * Eyeball with optic veins, 6 articulated trailing tendrils, and Phase 2 fanged razor jaw opening.
 */
public class EyeOfCthulhuModel extends HierarchicalModel<EyeOfCthulhuEntity> {
    private final ModelPart root;
    private final ModelPart eyeball;
    private final ModelPart pupil;
    private final ModelPart upperJaw;
    private final ModelPart lowerJaw;
    private final ModelPart tendril1;
    private final ModelPart tendril2;
    private final ModelPart tendril3;

    public EyeOfCthulhuModel(ModelPart root) {
        this.root = root;
        this.eyeball = root.getChild("eyeball");
        this.pupil = this.eyeball.getChild("pupil");
        this.upperJaw = this.eyeball.getChild("upper_jaw");
        this.lowerJaw = this.eyeball.getChild("lower_jaw");
        this.tendril1 = this.eyeball.getChild("tendril1");
        this.tendril2 = this.eyeball.getChild("tendril2");
        this.tendril3 = this.eyeball.getChild("tendril3");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition rootPart = mesh.getRoot();

        // Massive Eyeball Core (14x14x14)
        PartDefinition eyeball = rootPart.addOrReplaceChild("eyeball",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-7.0F, -7.0F, -7.0F, 14.0F, 14.0F, 14.0F),
                PartPose.offset(0.0F, 16.0F, 0.0F));

        // Phase 1 Pupil
        eyeball.addOrReplaceChild("pupil",
                CubeListBuilder.create().texOffs(0, 28).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 1.0F),
                PartPose.ZERO);

        // Phase 2 Razor Teeth Jaws
        eyeball.addOrReplaceChild("upper_jaw",
                CubeListBuilder.create().texOffs(18, 28).addBox(-4.0F, -4.0F, -9.0F, 8.0F, 3.0F, 2.0F),
                PartPose.ZERO);
        eyeball.addOrReplaceChild("lower_jaw",
                CubeListBuilder.create().texOffs(18, 33).addBox(-4.0F, 1.0F, -9.0F, 8.0F, 3.0F, 2.0F),
                PartPose.ZERO);

        // Trailing optic tendrils
        eyeball.addOrReplaceChild("tendril1",
                CubeListBuilder.create().texOffs(40, 28).addBox(-2.0F, -2.0F, 7.0F, 4.0F, 4.0F, 8.0F),
                PartPose.ZERO);
        eyeball.addOrReplaceChild("tendril2",
                CubeListBuilder.create().texOffs(40, 28).addBox(2.0F, -4.0F, 7.0F, 3.0F, 3.0F, 7.0F),
                PartPose.ZERO);
        eyeball.addOrReplaceChild("tendril3",
                CubeListBuilder.create().texOffs(40, 28).addBox(-5.0F, 2.0F, 7.0F, 3.0F, 3.0F, 7.0F),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(EyeOfCthulhuEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.eyeball.yRot = netHeadYaw * ((float)Math.PI / 180F);
        this.eyeball.xRot = headPitch * ((float)Math.PI / 180F);

        boolean isPhase2 = entity.getCurrentPhase() == BossPhase.PHASE_2;
        this.pupil.visible = !isPhase2;
        this.upperJaw.visible = isPhase2;
        this.lowerJaw.visible = isPhase2;

        if (isPhase2) {
            float bite = (float) Math.abs(Math.sin(ageInTicks * 0.4F)) * 0.3F;
            this.upperJaw.xRot = -bite;
            this.lowerJaw.xRot = bite;
        }

        float wiggle = (float) Math.sin(ageInTicks * 0.25F) * 0.2F;
        this.tendril1.xRot = wiggle;
        this.tendril2.yRot = -wiggle;
        this.tendril3.zRot = wiggle;
    }
}
