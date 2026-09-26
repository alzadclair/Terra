package com.terraforge.rpg.client.model;

import com.terraforge.rpg.entity.mob.FaceMonsterEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * 3D Model for Face Monster Crimson mob.
 * Slender hunched humanoid with elongated arms, disjointed fanged jaw, and erratic animations.
 */
public class FaceMonsterModel extends HierarchicalModel<FaceMonsterEntity> {
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public FaceMonsterModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.leftArm = this.body.getChild("left_arm");
        this.rightArm = this.body.getChild("right_arm");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition rootPart = mesh.getRoot();

        // Hunched Spine / Body
        PartDefinition body = rootPart.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-4.0F, -14.0F, -3.0F, 8.0F, 14.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 10.0F, 0.0F, 0.25F, 0.0F, 0.0F));

        // Elongated Monster Skull
        PartDefinition head = body.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -6.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offset(0.0F, -14.0F, -1.0F));

        // Disjointed Dropped Jaw
        head.addOrReplaceChild("jaw",
                CubeListBuilder.create()
                        .texOffs(32, 0).addBox(-3.5F, 0.0F, -7.0F, 7.0F, 5.0F, 6.0F),
                PartPose.offset(0.0F, -1.0F, 0.0F));

        // Lanky Arms
        body.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(28, 16).addBox(0.0F, -1.0F, -2.0F, 3.0F, 18.0F, 4.0F),
                PartPose.offset(4.0F, -12.0F, 0.0F));

        body.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(28, 16).mirror().addBox(-3.0F, -1.0F, -2.0F, 3.0F, 18.0F, 4.0F),
                PartPose.offset(-4.0F, -12.0F, 0.0F));

        // Legs
        rootPart.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(42, 16).addBox(-1.5F, 0.0F, -2.0F, 3.0F, 14.0F, 4.0F),
                PartPose.offset(2.0F, 10.0F, 2.0F));

        rootPart.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(42, 16).mirror().addBox(-1.5F, 0.0F, -2.0F, 3.0F, 14.0F, 4.0F),
                PartPose.offset(-2.0F, 10.0F, 2.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(FaceMonsterEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F) * 0.6F;
        this.head.xRot = (headPitch * ((float) Math.PI / 180F) * 0.6F) - 0.25F;

        // Snapping jaw animation
        float jawSnap = (float) Math.abs(Math.sin(ageInTicks * 0.3F)) * 0.35F;
        this.jaw.xRot = jawSnap;

        // Lanky walking / pursuit animation
        this.leftLeg.xRot = (float) Math.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.rightLeg.xRot = (float) Math.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;

        this.leftArm.xRot = (float) Math.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.2F * limbSwingAmount;
        this.rightArm.xRot = (float) Math.cos(limbSwing * 0.6662F) * 1.2F * limbSwingAmount;
    }
}
