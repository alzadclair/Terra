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
 * Blockbench-modeled King Slime boss.
 * Features giant translucent slime body, suspended ninja inside the gel, and golden jewel crown.
 */
public class KingSlimeModel<T extends Entity> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart ninja;
    private final ModelPart crown;

    public KingSlimeModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.ninja = this.body.getChild("ninja");
        this.crown = this.body.getChild("crown");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition rootPart = mesh.getRoot();

        // Main Slime Body (16x16x16 cube)
        PartDefinition body = rootPart.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -16.0F, -8.0F, 16.0F, 16.0F, 16.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        // Suspended Ninja inside the slime core
        PartDefinition ninja = body.addOrReplaceChild("ninja",
                CubeListBuilder.create().texOffs(0, 32).addBox(-2.5F, -11.0F, -2.5F, 5.0F, 8.0F, 5.0F),
                PartPose.ZERO);
        ninja.addOrReplaceChild("ninja_head",
                CubeListBuilder.create().texOffs(20, 32).addBox(-2.0F, -14.0F, -2.0F, 4.0F, 3.0F, 4.0F),
                PartPose.ZERO);

        // Royal Jewel Crown on top
        body.addOrReplaceChild("crown",
                CubeListBuilder.create()
                        .texOffs(0, 48).addBox(-5.0F, -19.0F, -5.0F, 10.0F, 3.0F, 10.0F)
                        .texOffs(40, 48).addBox(-6.0F, -22.0F, -6.0F, 12.0F, 3.0F, 12.0F),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float squish = (float) Math.sin(ageInTicks * 0.2F) * 0.08F;
        this.body.xScale = 1.0F + squish;
        this.body.zScale = 1.0F + squish;
        this.body.yScale = 1.0F - squish;
        this.ninja.yRot = (float) Math.sin(ageInTicks * 0.05F) * 0.2F;
    }
}
