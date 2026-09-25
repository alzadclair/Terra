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
 * Blockbench-modeled Duke Fishron boss.
 * Mutant aquatic pig-shark-dragon with tusks, dorsal fins, webbed wings, and shark tail.
 */
public class DukeFishronModel<T extends Entity> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart snout;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart tail;

    public DukeFishronModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.snout = this.body.getChild("snout");
        this.leftWing = this.body.getChild("left_wing");
        this.rightWing = this.body.getChild("right_wing");
        this.tail = this.body.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition rootPart = mesh.getRoot();

        // Massive shark-pig torso
        PartDefinition body = rootPart.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-7.0F, -7.0F, -10.0F, 14.0F, 14.0F, 20.0F)
                        .texOffs(0, 34).addBox(-1.0F, -14.0F, -4.0F, 2.0F, 7.0F, 10.0F), // Dorsal fin
                PartPose.offset(0.0F, 14.0F, 0.0F));

        // Pig snout with sharp tusks
        body.addOrReplaceChild("snout",
                CubeListBuilder.create()
                        .texOffs(48, 0).addBox(-4.0F, -2.0F, -15.0F, 8.0F, 6.0F, 5.0F)
                        .texOffs(48, 12).addBox(-5.0F, 0.0F, -13.0F, 2.0F, 4.0F, 2.0F) // Left tusk
                        .texOffs(48, 12).mirror().addBox(3.0F, 0.0F, -13.0F, 2.0F, 4.0F, 2.0F), // Right tusk
                PartPose.ZERO);

        // Dragon/Aquatic Webbed Wings
        body.addOrReplaceChild("left_wing",
                CubeListBuilder.create().texOffs(24, 34).addBox(7.0F, -2.0F, -4.0F, 16.0F, 1.0F, 12.0F),
                PartPose.ZERO);
        body.addOrReplaceChild("right_wing",
                CubeListBuilder.create().texOffs(24, 34).mirror().addBox(-23.0F, -2.0F, -4.0F, 16.0F, 1.0F, 12.0F),
                PartPose.ZERO);

        // Shark Tail and Fluke
        body.addOrReplaceChild("tail",
                CubeListBuilder.create()
                        .texOffs(0, 52).addBox(-3.0F, -3.0F, 10.0F, 6.0F, 6.0F, 10.0F)
                        .texOffs(32, 52).addBox(-1.0F, -7.0F, 18.0F, 2.0F, 14.0F, 8.0F), // Fin fluke
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.body.yRot = netHeadYaw * ((float)Math.PI / 180F);
        this.body.xRot = headPitch * ((float)Math.PI / 180F);

        // Flapping aquatic wings
        float wingFlap = (float) Math.sin(ageInTicks * 0.4F) * 0.4F;
        this.leftWing.zRot = wingFlap;
        this.rightWing.zRot = -wingFlap;

        // Tail waggle
        this.tail.yRot = (float) Math.sin(ageInTicks * 0.3F) * 0.35F;
    }
}
