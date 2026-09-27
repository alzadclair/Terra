package com.terraforge.rpg.client.animation.skeletal;

import com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory for creating runtime skeletal armatures directly from authentic GLTF / .skin.json bind data.
 * Unifies both runtime rendering and automated test validation from a single source of truth.
 */
public final class EyeSkeletonFactory {

    private EyeSkeletonFactory() {}

    /**
     * Builds a Skeleton hierarchy using the authentic bindLocal, bindWorld, and inverseBind matrices
     * defined in the given meshData.
     *
     * @param meshData the skinned mesh data containing bone hierarchy and bind matrices
     * @return a fully articulated Skeleton initialized to its true GLTF bind pose
     */
    public static Skeleton create(TerraSkinnedMeshData meshData) {
        if (meshData == null) {
            throw new IllegalArgumentException("Cannot create skeleton from null mesh data");
        }

        List<TerraSkinnedMeshData.BoneData> boneDataList = meshData.getBones();
        if (boneDataList == null || boneDataList.isEmpty()) {
            throw new IllegalArgumentException("Mesh data has no bones: " + meshData.getName());
        }

        Map<String, Bone> createdBones = new HashMap<>();
        Bone rootBone = null;

        for (TerraSkinnedMeshData.BoneData bData : boneDataList) {
            Bone parent = null;
            if (bData.parent != null) {
                parent = createdBones.get(bData.parent);
                if (parent == null) {
                    throw new IllegalStateException("Parent bone '" + bData.parent + "' not found for bone '" + bData.name + "'");
                }
            }

            Bone bone = new Bone(bData.name, parent);
            bone.setExplicitBindMatrices(bData.bindLocalMatrix, bData.bindWorldMatrix, bData.inverseBindMatrix);

            createdBones.put(bData.name, bone);
            if (parent == null && rootBone == null) {
                rootBone = bone;
            }
        }

        if (rootBone == null) {
            throw new IllegalStateException("Skeleton created from '" + meshData.getName() + "' has no root bone");
        }

        return new Skeleton(rootBone);
    }
}
