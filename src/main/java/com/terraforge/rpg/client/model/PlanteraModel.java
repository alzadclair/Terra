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
 * Blockbench-modeled Plantera boss.
 * Carnivorous jungle floral bulb with 4 opening petals and 4 writhing thorny vine tentacles.
 */
public class PlanteraModel<T extends Entity> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart bulb;
    private final ModelPart petalNorth;
    private final ModelPart petalSouth;
    private final ModelPart petalEast;
    private final ModelPart petalWest;
    private final ModelPart tentacle1;
    private final ModelPart tentacle2;

    public PlanteraModel(ModelPart root) {
        this.root = root;
        this.bulb = root.getChild("bulb");
        this.petalNorth = this.bulb.getChild("petal_north");
        this.petalSouth = this.bulb.getChild("petal_south");
        this.petalEast = this.bulb.getChild("petal_east");
        this.petalWest = this.bulb.getChild("petal_west");
        this.tentacle1 = root.getChild("tentacle1");
        this.tentacle2 = root.getChild("tentacle2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition rootPart = mesh.getRoot();

        // Main Floral Bulb Core
        PartDefinition bulb = rootPart.addOrReplaceChild("bulb",
                CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F),
                PartPose.offset(0.0F, 12.0F, 0.0F));

        // 4 Opening Spiked Petals
        bulb.addOrReplaceChild("petal_north",
                CubeListBuilder.create().texOffs(64, 0).addBox(-6.0F, -14.0F, -9.0F, 12.0F, 12.0F, 2.0F),
                PartPose.ZERO);
        bulb.addOrReplaceChild("petal_south",
                CubeListBuilder.create().texOffs(64, 0).addBox(-6.0F, -14.0F, 7.0F, 12.0F, 12.0F, 2.0F),
                PartPose.ZERO);
        bulb.addOrReplaceChild("petal_east",
                CubeListBuilder.create().texOffs(64, 14).addBox(7.0F, -14.0F, -6.0F, 2.0F, 12.0F, 12.0F),
                PartPose.ZERO);
        bulb.addOrReplaceChild("petal_west",
                CubeListBuilder.create().texOffs(64, 14).addBox(-9.0F, -14.0F, -6.0F, 2.0F, 12.0F, 12.0F),
                PartPose.ZERO);

        // Writhing Vine Tentacles with Thorn Traps
        rootPart.addOrReplaceChild("tentacle1",
                CubeListBuilder.create()
                        .texOffs(0, 32).addBox(8.0F, 2.0F, -12.0F, 3.0F, 18.0F, 3.0F)
                        .texOffs(16, 32).addBox(7.0F, 18.0F, -13.0F, 5.0F, 5.0F, 5.0F),
                PartPose.ZERO);

        rootPart.addOrReplaceChild("tentacle2",
                CubeListBuilder.create()
                        .texOffs(0, 32).addBox(-11.0F, 2.0F, 9.0F, 3.0F, 18.0F, 3.0F)
                        .texOffs(16, 32).addBox(-12.0F, 18.0F, 8.0F, 5.0F, 5.0F, 5.0F),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float pulse = (float) Math.sin(ageInTicks * 0.15F) * 0.08F;
        this.bulb.xScale = 1.0F + pulse;
        this.bulb.yScale = 1.0F + pulse;
        this.bulb.zScale = 1.0F + pulse;

        float petalOpen = (float) Math.abs(Math.sin(ageInTicks * 0.1F)) * 0.35F;
        this.petalNorth.xRot = -petalOpen;
        this.petalSouth.xRot = petalOpen;
        this.petalEast.zRot = petalOpen;
        this.petalWest.zRot = -petalOpen;

        float wiggle = (float) Math.sin(ageInTicks * 0.2F) * 0.3F;
        this.tentacle1.xRot = wiggle;
        this.tentacle2.zRot = -wiggle;
    }
}
