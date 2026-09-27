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
    private final Skeleton skeleton;
    private final AnimationController animController;
    private TerraSkinnedMeshInstance instanceP1;
    private TerraSkinnedMeshInstance instanceP2;
    private float lastAgeInTicks = -1.0f;
    private float roll;
    private float pitch;
    private float yaw;

    public EyeRenderState(UUID entityUuid, Skeleton skeleton, AnimationController animController) {
        this.entityUuid = entityUuid;
        this.skeleton = skeleton;
        this.animController = animController;
    }

    public static EyeRenderState create(UUID entityUuid, TerraSkinnedMeshData meshData) {
        Skeleton skel = EyeSkeletonFactory.create(meshData);
        AnimationController ctrl = EyeOfCthulhuArmature.createController();
        ctrl.play("idle", true);
        return new EyeRenderState(entityUuid, skel, ctrl);
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
        return skeleton;
    }

    public AnimationController getAnimController() {
        return animController;
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

    public void resetInstances() {
        this.instanceP1 = null;
        this.instanceP2 = null;
    }
}
