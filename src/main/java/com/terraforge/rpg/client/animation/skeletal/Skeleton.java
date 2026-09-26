package com.terraforge.rpg.client.animation.skeletal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a complete skeletal armature containing a root bone and named lookup map.
 */
public class Skeleton {
    private final Bone rootBone;
    private final Map<String, Bone> bonesByName = new HashMap<>();

    public Skeleton(Bone rootBone) {
        this.rootBone = rootBone;
        registerBonesRecursively(rootBone);
        initBindPose();
        updateMatrices();
    }

    private void registerBonesRecursively(Bone bone) {
        bonesByName.put(bone.getName(), bone);
        for (Bone child : bone.getChildren()) {
            registerBonesRecursively(child);
        }
    }

    public Bone getRoot() {
        return rootBone;
    }

    public Bone getBone(String name) {
        return bonesByName.get(name);
    }

    public int getBoneCount() {
        return bonesByName.size();
    }

    public Map<String, Bone> getAllBones() {
        return Collections.unmodifiableMap(bonesByName);
    }

    public void initBindPose() {
        rootBone.initBindPose();
    }

    public void resetToBindPose() {
        for (Bone bone : bonesByName.values()) {
            bone.resetToBindPose();
        }
        updateMatrices();
    }

    public void applyBindMatrices(com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshData meshData) {
        if (meshData == null) return;
        java.util.List<String> boneNames = meshData.getBoneNames();
        for (int i = 0; i < boneNames.size(); i++) {
            Bone bone = bonesByName.get(boneNames.get(i));
            if (bone != null) {
                bone.bindWorldMatrix.set(meshData.getBindWorldMatrix(i));
                bone.invBindWorldMatrix.set(meshData.getInverseBindMatrix(i));
                bone.worldMatrix.set(bone.bindWorldMatrix);
                bone.skinMatrix.set(bone.worldMatrix).mul(bone.invBindWorldMatrix);
            }
        }
    }

    public void updateMatrices() {
        rootBone.updateMatrices();
    }
}
