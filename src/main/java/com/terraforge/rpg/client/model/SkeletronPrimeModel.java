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
 * Blockbench-modeled Skeletron Prime boss.
 * Mechanical skull with 4 articulated arms: Prime Saw, Prime Vice, Prime Cannon, Prime Laser.
 */
public class SkeletronPrimeModel<T extends Entity> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart primeSaw;
    private final ModelPart primeVice;
    private final ModelPart primeCannon;
    private final ModelPart primeLaser;

    public SkeletronPrimeModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.primeSaw = root.getChild("prime_saw");
        this.primeVice = root.getChild("prime_vice");
        this.primeCannon = root.getChild("prime_cannon");
        this.primeLaser = root.getChild("prime_laser");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition rootPart = mesh.getRoot();

        // Main Mechanical Skull
        PartDefinition head = rootPart.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-6.0F, -6.0F, -6.0F, 12.0F, 10.0F, 12.0F)
                        .texOffs(48, 0).addBox(-4.0F, -1.0F, -8.0F, 8.0F, 4.0F, 2.0F), // Steel nasal/teeth
                PartPose.offset(0.0F, 8.0F, 0.0F));

        head.addOrReplaceChild("jaw",
                CubeListBuilder.create().texOffs(0, 22).addBox(-5.0F, 4.0F, -7.0F, 10.0F, 3.0F, 8.0F),
                PartPose.ZERO);

        // Arm 1: Prime Saw (Upper Right)
        rootPart.addOrReplaceChild("prime_saw",
                CubeListBuilder.create()
                        .texOffs(36, 22).addBox(8.0F, -10.0F, -4.0F, 3.0F, 14.0F, 3.0F) // arm
                        .texOffs(48, 22).addBox(6.5F, 4.0F, -8.0F, 6.0F, 6.0F, 1.0F), // saw blade
                PartPose.ZERO);

        // Arm 2: Prime Vice (Upper Left)
        rootPart.addOrReplaceChild("prime_vice",
                CubeListBuilder.create()
                        .texOffs(36, 22).addBox(-11.0F, -10.0F, -4.0F, 3.0F, 14.0F, 3.0F) // arm
                        .texOffs(64, 22).addBox(-13.0F, 4.0F, -8.0F, 7.0F, 5.0F, 2.0F), // pincer claw
                PartPose.ZERO);

        // Arm 3: Prime Cannon (Lower Right)
        rootPart.addOrReplaceChild("prime_cannon",
                CubeListBuilder.create()
                        .texOffs(36, 22).addBox(9.0F, 0.0F, 4.0F, 3.0F, 12.0F, 3.0F) // arm
                        .texOffs(82, 22).addBox(8.0F, 8.0F, -4.0F, 5.0F, 5.0F, 10.0F), // heavy barrel
                PartPose.ZERO);

        // Arm 4: Prime Laser (Lower Left)
        rootPart.addOrReplaceChild("prime_laser",
                CubeListBuilder.create()
                        .texOffs(36, 22).addBox(-12.0F, 0.0F, 4.0F, 3.0F, 12.0F, 3.0F) // arm
                        .texOffs(82, 37).addBox(-13.0F, 8.0F, -3.0F, 5.0F, 4.0F, 9.0F), // laser emitter
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = headPitch * ((float)Math.PI / 180F);
        this.jaw.xRot = (float) Math.abs(Math.sin(ageInTicks * 0.25F)) * 0.3F;

        // Animate arms
        float sawSwing = (float) Math.sin(ageInTicks * 0.3F) * 0.25F;
        this.primeSaw.zRot = sawSwing;
        this.primeVice.zRot = -sawSwing;
        this.primeCannon.xRot = (float) Math.cos(ageInTicks * 0.2F) * 0.2F;
        this.primeLaser.xRot = (float) Math.sin(ageInTicks * 0.2F) * 0.2F;
    }
}
