package com.terraforge.rpg.client.render.entity.state;

import com.terraforge.rpg.client.animation.skeletal.AnimationController;
import com.terraforge.rpg.client.animation.skeletal.EyeOfCthulhuArmature;
import com.terraforge.rpg.client.animation.skeletal.EyeSkeletonFactory;
import com.terraforge.rpg.client.animation.skeletal.Skeleton;
import com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshData;
import com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshInstance;

import java.util.UUID;

/**
 * Client-side per-entity render and animation state for Eye of Cthulhu.
 * Isolates skeletal transforms, animation clips, time tracking, and mesh instances
 * on a per-entity basis to guarantee zero cross-contamination when multiple bosses exist.
 */
public class EyeRenderState {
    private final UUID entityUuid;
    private int entityId;
    private final Skeleton skeletonP1;
    private final Skeleton skeletonP2;
    private final AnimationController controllerP1;
    private final AnimationController controllerP2;
    private TerraSkinnedMeshInstance instanceP1;
    private TerraSkinnedMeshInstance instanceP2;
    private float lastAgeInTicks = -1.0f;
    private float roll;
    private float pitch;
    private float yaw;

    public EyeRenderState(UUID entityUuid, Skeleton skeletonP1, Skeleton skeletonP2,
                          AnimationController controllerP1, AnimationController controllerP2) {
        this.entityUuid = entityUuid;
        this.skeletonP1 = skeletonP1;
        this.skeletonP2 = skeletonP2;
        this.controllerP1 = controllerP1;
        this.controllerP2 = controllerP2;
    }

    public EyeRenderState(UUID entityUuid, Skeleton skeleton, AnimationController animController) {
        this(entityUuid, skeleton, skeleton, animController, animController);
    }

    public static EyeRenderState create(UUID entityUuid, TerraSkinnedMeshData meshDataP1, TerraSkinnedMeshData meshDataP2) {
        Skeleton skel1 = EyeSkeletonFactory.create(meshDataP1);
        Skeleton skel2 = EyeSkeletonFactory.create(meshDataP2);
        AnimationController ctrl1 = EyeOfCthulhuArmature.createController();
        AnimationController ctrl2 = EyeOfCthulhuArmature.createController();
        ctrl1.play("idle", true);
        ctrl2.play("phase2_idle", true);
        return new EyeRenderState(entityUuid, skel1, skel2, ctrl1, ctrl2);
    }

    public static EyeRenderState create(UUID entityUuid, TerraSkinnedMeshData meshData) {
        Skeleton skel = EyeSkeletonFactory.create(meshData);
        AnimationController ctrl = EyeOfCthulhuArmature.createController();
        ctrl.play("idle", true);
        return new EyeRenderState(entityUuid, skel, skel, ctrl, ctrl);
    }

    public UUID getEntityUuid() {
        return entityUuid;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public Skeleton getSkeleton() {
        return skeletonP1;
    }

    public Skeleton getSkeleton(boolean isPhase2) {
        return isPhase2 ? skeletonP2 : skeletonP1;
    }

    public Skeleton getSkeletonForPhase(int phase) {
        return phase >= 2 ? skeletonP2 : skeletonP1;
    }

    public Skeleton getSkeletonP1() {
        return skeletonP1;
    }

    public Skeleton getSkeletonP2() {
        return skeletonP2;
    }

    public AnimationController getAnimController() {
        return controllerP1;
    }

    public AnimationController getAnimController(boolean isPhase2) {
        return isPhase2 ? controllerP2 : controllerP1;
    }

    public AnimationController getAnimControllerForPhase(int phase) {
        return phase >= 2 ? controllerP2 : controllerP1;
    }

    public AnimationController getControllerP1() {
        return controllerP1;
    }

    public AnimationController getControllerP2() {
        return controllerP2;
    }

    public float getLastAgeInTicks() {
        return lastAgeInTicks;
    }

    public void setLastAgeInTicks(float lastAgeInTicks) {
        this.lastAgeInTicks = lastAgeInTicks;
    }

    public float getRoll() {
        return roll;
    }

    public void setRoll(float roll) {
        this.roll = roll;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public TerraSkinnedMeshInstance getOrCreateInstanceP1(TerraSkinnedMeshData meshData) {
        if (instanceP1 == null || instanceP1.getMeshData() != meshData) {
            instanceP1 = meshData.createInstance();
        }
        return instanceP1;
    }

    public TerraSkinnedMeshInstance getOrCreateInstanceP2(TerraSkinnedMeshData meshData) {
        if (instanceP2 == null || instanceP2.getMeshData() != meshData) {
            instanceP2 = meshData.createInstance();
        }
        return instanceP2;
    }

    public TerraSkinnedMeshInstance getMeshInstance(boolean isPhase2, TerraSkinnedMeshData meshData) {
        return isPhase2 ? getOrCreateInstanceP2(meshData) : getOrCreateInstanceP1(meshData);
    }

    public void resetInstances() {
        this.instanceP1 = null;
        this.instanceP2 = null;
    }
}
