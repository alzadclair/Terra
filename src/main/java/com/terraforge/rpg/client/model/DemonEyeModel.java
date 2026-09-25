package com.terraforge.rpg.client.model;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.Entity;

/**
 * 3D Model for Terraria flying eyes (Demon Eye, Servant of Cthulhu, Eye of Cthulhu, Retinazer, Spazmatism).
 */
public class DemonEyeModel<T extends Entity> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart eyeball;
    private final ModelPart pupil;
    private final ModelPart tendril1;
    private final ModelPart tendril2;

    public DemonEyeModel(ModelPart root) {
        this.root = root;
        this.eyeball = root.getChild("eyeball");
        this.pupil = this.eyeball.getChild("pupil");
        this.tendril1 = this.eyeball.getChild("tendril1");
        this.tendril2 = this.eyeball.getChild("tendril2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition rootPart = mesh.getRoot();

        PartDefinition eyeball = rootPart.addOrReplaceChild("eyeball",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-6.0F, -6.0F, -6.0F, 12.0F, 12.0F, 12.0F),
                PartPose.offset(0.0F, 16.0F, 0.0F));

        eyeball.addOrReplaceChild("pupil",
                CubeListBuilder.create()
                        .texOffs(0, 24)
                        .addBox(-3.0F, -3.0F, -7.0F, 6.0F, 6.0F, 1.0F),
                PartPose.ZERO);

        eyeball.addOrReplaceChild("tendril1",
                CubeListBuilder.create()
                        .texOffs(16, 24)
                        .addBox(-2.0F, -1.0F, 6.0F, 4.0F, 2.0F, 5.0F),
                PartPose.ZERO);

        eyeball.addOrReplaceChild("tendril2",
                CubeListBuilder.create()
                        .texOffs(16, 24)
                        .addBox(-1.0F, -2.0F, 6.0F, 2.0F, 4.0F, 4.0F),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.eyeball.yRot = netHeadYaw * ((float)Math.PI / 180F);
        this.eyeball.xRot = headPitch * ((float)Math.PI / 180F);
        float wiggle = (float)Math.sin(ageInTicks * 0.2F) * 0.15F;
        this.tendril1.xRot = wiggle;
        this.tendril2.yRot = -wiggle;
    }
}
