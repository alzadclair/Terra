package com.terraforge.rpg.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.terraforge.rpg.TerraForgeRPG;
import com.terraforge.rpg.boss.prehardmode.EyeOfCthulhuEntity;
import com.terraforge.rpg.client.animation.skeletal.AnimationController;
import com.terraforge.rpg.client.animation.skeletal.Bone;
import com.terraforge.rpg.client.animation.skeletal.EyeOfCthulhuArmature;
import com.terraforge.rpg.client.animation.skeletal.Skeleton;
import com.terraforge.rpg.client.render.mesh.TerraMesh3D;
import com.terraforge.rpg.client.render.mesh.TerraMeshLoader;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

import java.util.Set;

/**
 * Authentic 3D Mesh Model for Eye of Cthulhu with real skeletal animation.
 * Features 16 distinct animation clips, dual phase armatures, bone-to-mesh vertex transformations,
 * and high-fidelity OBJ rendering without CubeListBuilder placeholders.
 */
public class EyeOfCthulhuModel extends HierarchicalModel<EyeOfCthulhuEntity> {

    public static final ResourceLocation SKIN_P1 =
            ResourceLocation.fromNamespaceAndPath(TerraForgeRPG.MOD_ID, "models/entity/boss/eye_of_cthulhu_p1.skin.json");
    public static final ResourceLocation SKIN_P2 =
            ResourceLocation.fromNamespaceAndPath(TerraForgeRPG.MOD_ID, "models/entity/boss/eye_of_cthulhu_p2.skin.json");
    public static final ResourceLocation MESH_P1 =
            ResourceLocation.fromNamespaceAndPath(TerraForgeRPG.MOD_ID, "models/entity/boss/eye_of_cthulhu_p1.obj");
    public static final ResourceLocation MESH_P2 =
            ResourceLocation.fromNamespaceAndPath(TerraForgeRPG.MOD_ID, "models/entity/boss/eye_of_cthulhu_p2.obj");

    private final ModelPart root;
    private final Skeleton skeleton;
    private final AnimationController animController;

    private EyeOfCthulhuEntity activeEntity;
    private float roll;
    private float pitch;
    private float yaw;
    private float lastAgeInTicks = 0.0f;

    private com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshInstance instanceP1;
    private com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshInstance instanceP2;

    public EyeOfCthulhuModel(ModelPart root) {
        this.root = root;
        this.skeleton = EyeOfCthulhuArmature.createArmature();
        this.animController = EyeOfCthulhuArmature.createController();
        this.animController.play("idle", true);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        return LayerDefinition.create(mesh, 16, 16);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    public Skeleton getSkeleton() {
        return skeleton;
    }

    public AnimationController getAnimController() {
        return animController;
    }

    @Override
    public void setupAnim(EyeOfCthulhuEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.activeEntity = entity;
        this.yaw = (float) Math.toRadians(netHeadYaw);
        this.pitch = (float) Math.toRadians(headPitch);

        boolean isPhase2 = entity.getCurrentPhase().phaseNumber() >= 2;

        // Dynamic roll banking during high-speed charge dashes
        double velX = entity.getDeltaMovement().x;
        double velZ = entity.getDeltaMovement().z;
        double horizontalSpeed = Math.sqrt(velX * velX + velZ * velZ);

        if (horizontalSpeed > 0.4) {
            this.roll = (float) Math.sin(ageInTicks * 0.5f) * (isPhase2 ? 0.25f : 0.12f);
        } else {
            this.roll = 0.0f;
        }

        // Map synced animation state to clip
        EyeOfCthulhuEntity.EyeAnimState animState = entity.getAnimState();
        String targetClip = switch (animState) {
            case SPAWN -> "spawn";
            case IDLE -> isPhase2 ? "phase2_idle" : "idle";
            case HOVER -> "hover";
            case LOOK -> "look";
            case SUMMON_SERVANT -> "summon_servant";
            case CHARGE_PREPARE -> "charge_prepare";
            case CHARGE -> "charge";
            case CHARGE_RECOVER -> "charge_recover";
            case HURT -> "hurt";
            case TRANSITIONING -> "phase_transition";
            case PHASE2_IDLE -> "phase2_idle";
            case PHASE2_CHARGE_PREPARE -> "phase2_charge_prepare";
            case PHASE2_CHARGE -> "phase2_charge";
            case PHASE2_BITE -> "phase2_bite";
            case ENRAGED -> "enrage";
            case DYING -> "death";
        };

        if (!animController.getCurrentClipName().equals(targetClip)) {
            float blendDur = (animState == EyeOfCthulhuEntity.EyeAnimState.HURT || animState == EyeOfCthulhuEntity.EyeAnimState.PHASE2_BITE)
                    ? 0.10f : 0.20f;
            animController.crossFade(targetClip, blendDur);
        }

        // Evaluate skeletal animation
        float deltaTicks = (lastAgeInTicks > 0.0f) ? Math.min(2.0f, Math.max(0.01f, ageInTicks - lastAgeInTicks)) : 1.0f;
        lastAgeInTicks = ageInTicks;
        float deltaTime = deltaTicks * 0.05f;

        animController.update(deltaTime);
        animController.apply(skeleton);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        poseStack.pushPose();

        // Position model root in entity coordinate frame
        poseStack.translate(0.0, 1.0, 0.0);

        // Apply entity orientation rotations (yaw, pitch, bank roll)
        poseStack.mulPose(new Quaternionf(new AxisAngle4f(-yaw, 0.0f, 1.0f, 0.0f)));
        poseStack.mulPose(new Quaternionf(new AxisAngle4f(pitch, 1.0f, 0.0f, 0.0f)));
        if (roll != 0.0f) {
            poseStack.mulPose(new Quaternionf(new AxisAngle4f(roll, 0.0f, 0.0f, 1.0f)));
        }

        // Base model scale
        poseStack.scale(0.04f, 0.04f, 0.04f);

        boolean isPhase2 = activeEntity != null && activeEntity.getCurrentPhase().phaseNumber() >= 2;
        ResourceLocation skinLoc = isPhase2 ? SKIN_P2 : SKIN_P1;

        float a = ((color >> 24) & 0xFF) / 255.0f;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        try {
            com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshData meshData =
                    com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshLoader.getOrLoad(skinLoc);
            com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshInstance instance;
            if (isPhase2) {
                if (instanceP2 == null || instanceP2.getMeshData() != meshData) {
                    instanceP2 = meshData.createInstance();
                }
                instance = instanceP2;
            } else {
                if (instanceP1 == null || instanceP1.getMeshData() != meshData) {
                    instanceP1 = meshData.createInstance();
                }
                instance = instanceP1;
            }

            instance.skin(skeleton);
            instance.render(poseStack, buffer, packedLight, packedOverlay, r, g, b, a);
        } catch (Exception e) {
            // Fallback to static OBJ mesh if skin loading encounters an issue
            ResourceLocation meshLoc = isPhase2 ? MESH_P2 : MESH_P1;
            TerraMesh3D mesh = TerraMeshLoader.getOrLoad(meshLoc);
            mesh.render(poseStack, buffer, packedLight, packedOverlay, r, g, b, a);
        }

        poseStack.popPose();
    }
}
