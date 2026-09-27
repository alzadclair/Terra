package com.terraforge.rpg.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.terraforge.rpg.TerraForgeRPG;
import com.terraforge.rpg.boss.prehardmode.EyeOfCthulhuEntity;
import com.terraforge.rpg.client.animation.skeletal.AnimationController;
import com.terraforge.rpg.client.animation.skeletal.Skeleton;
import com.terraforge.rpg.client.render.entity.state.EyeRenderState;
import com.terraforge.rpg.client.render.entity.state.EyeRenderStateManager;
import com.terraforge.rpg.client.render.mesh.TerraMesh3D;
import com.terraforge.rpg.client.render.mesh.TerraMeshLoader;
import com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshData;
import com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshInstance;
import com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshLoader;
import com.terraforge.rpg.util.TerraLogger;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

/**
 * Authentic 3D Mesh Model for Eye of Cthulhu with real skeletal animation.
 * Model instance is completely stateless across entities; all mutable animation, skeleton,
 * and mesh instances live in per-entity EyeRenderState managed by EyeRenderStateManager.
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
    private EyeRenderState currentRenderState;
    private EyeOfCthulhuEntity currentEntity;

    public EyeOfCthulhuModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        return LayerDefinition.create(mesh, 16, 16);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    public void setRenderState(EyeRenderState renderState) {
        this.currentRenderState = renderState;
    }

    public Skeleton getSkeleton() {
        return currentRenderState != null ? currentRenderState.getSkeleton() : null;
    }

    public AnimationController getAnimController() {
        return currentRenderState != null ? currentRenderState.getAnimController() : null;
    }

    @Override
    public void setupAnim(EyeOfCthulhuEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.currentEntity = entity;
        EyeRenderState state = this.currentRenderState;
        if (state == null) {
            state = EyeRenderStateManager.getOrCreate(entity);
            this.currentRenderState = state;
        }

        state.setYaw((float) Math.toRadians(netHeadYaw));
        state.setPitch((float) Math.toRadians(headPitch));

        boolean isPhase2 = entity.getCurrentPhase().phaseNumber() >= 2;

        // Dynamic roll banking during high-speed charge dashes
        double velX = entity.getDeltaMovement().x;
        double velZ = entity.getDeltaMovement().z;
        double horizontalSpeed = Math.sqrt(velX * velX + velZ * velZ);

        if (horizontalSpeed > 0.4) {
            state.setRoll((float) Math.sin(ageInTicks * 0.5f) * (isPhase2 ? 0.25f : 0.12f));
        } else {
            state.setRoll(0.0f);
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

        AnimationController animController = state.getAnimController();
        if (!animController.getCurrentClipName().equals(targetClip)) {
            float blendDur = (animState == EyeOfCthulhuEntity.EyeAnimState.HURT || animState == EyeOfCthulhuEntity.EyeAnimState.PHASE2_BITE)
                    ? 0.10f : 0.20f;
            animController.crossFade(targetClip, blendDur);
        }

        // Evaluate skeletal animation
        float lastAge = state.getLastAgeInTicks();
        float deltaTicks = (lastAge > 0.0f) ? Math.min(2.0f, Math.max(0.01f, ageInTicks - lastAge)) : 1.0f;
        state.setLastAgeInTicks(ageInTicks);
        float deltaTime = deltaTicks * 0.05f;

        animController.update(deltaTime);
        animController.apply(state.getSkeleton());
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        poseStack.pushPose();

        // Position model root in entity coordinate frame
        poseStack.translate(0.0, 1.0, 0.0);

        EyeRenderState state = this.currentRenderState;
        float yaw = state != null ? state.getYaw() : 0.0f;
        float pitch = state != null ? state.getPitch() : 0.0f;
        float roll = state != null ? state.getRoll() : 0.0f;

        // Apply entity orientation rotations (yaw, pitch, bank roll)
        poseStack.mulPose(new Quaternionf(new AxisAngle4f(-yaw, 0.0f, 1.0f, 0.0f)));
        poseStack.mulPose(new Quaternionf(new AxisAngle4f(pitch, 1.0f, 0.0f, 0.0f)));
        if (roll != 0.0f) {
            poseStack.mulPose(new Quaternionf(new AxisAngle4f(roll, 0.0f, 0.0f, 1.0f)));
        }

        // Base model scale
        poseStack.scale(0.04f, 0.04f, 0.04f);

        boolean isPhase2 = currentEntity != null && currentEntity.getCurrentPhase().phaseNumber() >= 2;
        ResourceLocation skinLoc = isPhase2 ? SKIN_P2 : SKIN_P1;

        float a = ((color >> 24) & 0xFF) / 255.0f;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        try {
            TerraSkinnedMeshData meshData = TerraSkinnedMeshLoader.getOrLoad(skinLoc);
            TerraSkinnedMeshInstance instance = (state != null)
                    ? (isPhase2 ? state.getOrCreateInstanceP2(meshData) : state.getOrCreateInstanceP1(meshData))
                    : meshData.createInstance();

            Skeleton skeleton = (state != null) ? state.getSkeleton() : null;
            if (skeleton != null) {
                instance.skin(skeleton);
            }
            instance.render(poseStack, buffer, packedLight, packedOverlay, r, g, b, a);
        } catch (Exception e) {
            TerraLogger.error("CLIENT", "Failed to render skinned Eye of Cthulhu mesh, falling back to static OBJ mesh", e);
            // Fallback to static OBJ mesh if skin loading encounters an issue
            ResourceLocation meshLoc = isPhase2 ? MESH_P2 : MESH_P1;
            TerraMesh3D mesh = TerraMeshLoader.getOrLoad(meshLoc);
            mesh.render(poseStack, buffer, packedLight, packedOverlay, r, g, b, a);
        } finally {
            this.currentRenderState = null;
            this.currentEntity = null;
        }

        poseStack.popPose();
    }
}
