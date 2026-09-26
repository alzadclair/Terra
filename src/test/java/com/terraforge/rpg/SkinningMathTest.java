package com.terraforge.rpg;

import com.terraforge.rpg.client.animation.skeletal.Bone;
import com.terraforge.rpg.client.animation.skeletal.Skeleton;
import com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshData;
import com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshInstance;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SkinningMathTest {

    @Test
    @DisplayName("Verify identity skinning leaves vertices and normals invariant")
    void testIdentitySkinning() {
        TerraSkinnedMeshData.PartData part = new TerraSkinnedMeshData.PartData(
                "test", 1,
                new float[]{10.0f, 20.0f, 30.0f},
                new float[]{0.0f, 1.0f, 0.0f},
                new float[]{0.0f, 0.0f},
                new int[]{0, 0, 0, 0},
                new float[]{1.0f, 0.0f, 0.0f, 0.0f},
                new int[]{0}
        );

        TerraSkinnedMeshData data = new TerraSkinnedMeshData(
                "single_bone", "",
                List.of("root"),
                new Matrix4f[]{new Matrix4f().identity()},
                List.of(part)
        );

        Bone root = new Bone("root", null);
        root.setBindPose(0, 0, 0, new Quaternionf(), 1, 1, 1);
        Skeleton skeleton = new Skeleton(root);

        TerraSkinnedMeshInstance instance = data.createInstance();
        instance.skin(skeleton);

        float[] pos = instance.getParts().get(0).skinnedPositions;
        assertEquals(10.0f, pos[0], 1e-5f);
        assertEquals(20.0f, pos[1], 1e-5f);
        assertEquals(30.0f, pos[2], 1e-5f);

        float[] norm = instance.getParts().get(0).skinnedNormals;
        assertEquals(0.0f, norm[0], 1e-5f);
        assertEquals(1.0f, norm[1], 1e-5f);
        assertEquals(0.0f, norm[2], 1e-5f);
    }

    @Test
    @DisplayName("Verify pure translation (+1 on X moves vertex +1 on X, normal unchanged)")
    void testPureTranslation() {
        TerraSkinnedMeshData.PartData part = new TerraSkinnedMeshData.PartData(
                "test", 1,
                new float[]{10.0f, 20.0f, 30.0f},
                new float[]{0.0f, 1.0f, 0.0f},
                new float[]{0.0f, 0.0f},
                new int[]{0, 0, 0, 0},
                new float[]{1.0f, 0.0f, 0.0f, 0.0f},
                new int[]{0}
        );

        TerraSkinnedMeshData data = new TerraSkinnedMeshData(
                "single_bone", "",
                List.of("root"),
                new Matrix4f[]{new Matrix4f().identity()},
                List.of(part)
        );

        Bone root = new Bone("root", null);
        root.setBindPose(0, 0, 0, new Quaternionf(), 1, 1, 1);
        Skeleton skeleton = new Skeleton(root);

        // Move root by +1 on X
        root.localPos.set(1.0f, 0.0f, 0.0f);
        skeleton.updateMatrices();

        TerraSkinnedMeshInstance instance = data.createInstance();
        instance.skin(skeleton);

        float[] pos = instance.getParts().get(0).skinnedPositions;
        assertEquals(11.0f, pos[0], 1e-5f);
        assertEquals(20.0f, pos[1], 1e-5f);
        assertEquals(30.0f, pos[2], 1e-5f);

        float[] norm = instance.getParts().get(0).skinnedNormals;
        assertEquals(0.0f, norm[0], 1e-5f);
        assertEquals(1.0f, norm[1], 1e-5f);
        assertEquals(0.0f, norm[2], 1e-5f);
    }

    @Test
    @DisplayName("Verify hinge rotation rotates around pivot with zero pivot drift")
    void testHingeRotation() {
        float px = 0.018f, py = 12.302f, pz = 0.144f;
        // Vertex 1: at pivot itself. Vertex 2: 10 units forward along Z
        TerraSkinnedMeshData.PartData part = new TerraSkinnedMeshData.PartData(
                "jaw", 2,
                new float[]{
                        px, py, pz,
                        px, py, pz + 10.0f
                },
                new float[]{
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 0.0f
                },
                new float[]{0, 0, 0, 0},
                new int[]{
                        0, 0, 0, 0,
                        0, 0, 0, 0
                },
                new float[]{
                        1.0f, 0.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f, 0.0f
                },
                new int[]{0, 1}
        );

        Matrix4f bindWorld = new Matrix4f().identity().translate(px, py, pz);
        Matrix4f invBind = new Matrix4f(bindWorld).invert();

        TerraSkinnedMeshData data = new TerraSkinnedMeshData(
                "jaw_data", "",
                List.of("jaw"),
                new Matrix4f[]{invBind},
                List.of(part)
        );

        Bone jaw = new Bone("jaw", null);
        jaw.setBindPose(px, py, pz, new Quaternionf(), 1, 1, 1);
        Skeleton skeleton = new Skeleton(jaw);

        // Rotate jaw by 90 degrees around X in its local hinge space
        Quaternionf rot90X = new Quaternionf().rotationX((float) Math.toRadians(90.0));
        jaw.localRot.set(rot90X);
        skeleton.updateMatrices();

        TerraSkinnedMeshInstance instance = data.createInstance();
        instance.skin(skeleton);

        float[] pos = instance.getParts().get(0).skinnedPositions;

        // Vertex 0 (pivot point) must NOT drift
        assertEquals(px, pos[0], 1e-4f, "Hinge pivot X must remain invariant under rotation");
        assertEquals(py, pos[1], 1e-4f, "Hinge pivot Y must remain invariant under rotation");
        assertEquals(pz, pos[2], 1e-4f, "Hinge pivot Z must remain invariant under rotation");

        // Vertex 1 (+10 along Z) rotates +90 deg around X -> moves to -10 along Y relative to pivot
        assertEquals(px, pos[3], 1e-4f);
        assertEquals(py - 10.0f, pos[4], 1e-4f);
        assertEquals(pz, pos[5], 1e-4f);
    }

    @Test
    @DisplayName("Verify normal matrix (M^3x3)^-T correctly preserves normal orthogonality under non-uniform scale (0.88, 0.88, 1.35)")
    void testNonUniformScaleNormalSkinning() {
        // Vertex at (0, 0, 0) with surface tangent along (1, 1, 0) normalized
        // and normal along (-1, 1, 0) normalized (orthogonal)
        float invSqrt2 = (float) (1.0 / Math.sqrt(2.0));
        float tx = invSqrt2, ty = invSqrt2, tz = 0.0f;
        float nx = -invSqrt2, ny = invSqrt2, nz = 0.0f;

        TerraSkinnedMeshData.PartData part = new TerraSkinnedMeshData.PartData(
                "surf", 1,
                new float[]{0.0f, 0.0f, 0.0f},
                new float[]{nx, ny, nz},
                new float[]{0.0f, 0.0f},
                new int[]{0, 0, 0, 0},
                new float[]{1.0f, 0.0f, 0.0f, 0.0f},
                new int[]{0}
        );

        TerraSkinnedMeshData data = new TerraSkinnedMeshData(
                "scale_data", "",
                List.of("body"),
                new Matrix4f[]{new Matrix4f().identity()},
                List.of(part)
        );

        Bone body = new Bone("body", null);
        body.setBindPose(0, 0, 0, new Quaternionf(), 1, 1, 1);
        Skeleton skeleton = new Skeleton(body);

        // Apply non-uniform scale typical of charge animations: (0.88, 0.88, 1.35)
        float sx = 0.88f, sy = 0.88f, sz = 1.35f;
        body.localScale.set(sx, sy, sz);
        skeleton.updateMatrices();

        TerraSkinnedMeshInstance instance = data.createInstance();
        instance.skin(skeleton);

        // Scaled tangent vector
        Vector3f scaledTangent = new Vector3f(tx * sx, ty * sy, tz * sz).normalize();

        // Skinned normal
        float[] skinnedNorm = instance.getParts().get(0).skinnedNormals;
        Vector3f deformedNormal = new Vector3f(skinnedNorm[0], skinnedNorm[1], skinnedNorm[2]);

        // Tangent and normal must remain strictly perpendicular (dot product == 0)
        float dot = scaledTangent.dot(deformedNormal);
        assertEquals(0.0f, dot, 1e-5f, "Normal must remain orthogonal to surface under non-uniform scale");
        assertEquals(1.0f, deformedNormal.length(), 1e-5f, "Normal must remain normalized");
    }

    @Test
    @DisplayName("Verify two-bone 50/50 weighted blend linearly interpolates vertex displacement")
    void testTwoBoneWeightedBlend() {
        TerraSkinnedMeshData.PartData part = new TerraSkinnedMeshData.PartData(
                "blend", 1,
                new float[]{0.0f, 0.0f, 0.0f},
                new float[]{0.0f, 1.0f, 0.0f},
                new float[]{0.0f, 0.0f},
                new int[]{0, 1, 0, 0},
                new float[]{0.5f, 0.5f, 0.0f, 0.0f},
                new int[]{0}
        );

        TerraSkinnedMeshData data = new TerraSkinnedMeshData(
                "blend_data", "",
                List.of("boneA", "boneB"),
                new Matrix4f[]{new Matrix4f().identity(), new Matrix4f().identity()},
                List.of(part)
        );

        Bone boneA = new Bone("boneA", null);
        boneA.setBindPose(0, 0, 0, new Quaternionf(), 1, 1, 1);
        Bone boneB = new Bone("boneB", null);
        boneB.setBindPose(0, 0, 0, new Quaternionf(), 1, 1, 1);

        // Bone A translates +10 on X, Bone B translates +10 on Y
        boneA.localPos.set(10.0f, 0.0f, 0.0f);
        boneA.updateMatrices();
        boneB.localPos.set(0.0f, 10.0f, 0.0f);
        boneB.updateMatrices();

        // Artificial skeleton root
        Bone root = new Bone("root", null);
        Skeleton skeleton = new Skeleton(root) {
            @Override
            public Bone getBone(String name) {
                if ("boneA".equals(name)) return boneA;
                if ("boneB".equals(name)) return boneB;
                return null;
            }
        };

        TerraSkinnedMeshInstance instance = data.createInstance();
        instance.skin(skeleton);

        float[] pos = instance.getParts().get(0).skinnedPositions;
        // 50% * 10 + 50% * 0 = 5 on X; 50% * 0 + 50% * 10 = 5 on Y
        assertEquals(5.0f, pos[0], 1e-5f);
        assertEquals(5.0f, pos[1], 1e-5f);
        assertEquals(0.0f, pos[2], 1e-5f);
    }

    @Test
    @DisplayName("Verify multi-instance isolation: mutating Eye A pose never affects Eye B buffers")
    void testMultiInstanceIsolation() {
        TerraSkinnedMeshData.PartData part = new TerraSkinnedMeshData.PartData(
                "part", 1,
                new float[]{100.0f, 100.0f, 100.0f},
                new float[]{0.0f, 1.0f, 0.0f},
                new float[]{0.0f, 0.0f},
                new int[]{0, 0, 0, 0},
                new float[]{1.0f, 0.0f, 0.0f, 0.0f},
                new int[]{0}
        );

        TerraSkinnedMeshData sharedData = new TerraSkinnedMeshData(
                "shared", "",
                List.of("body"),
                new Matrix4f[]{new Matrix4f().identity()},
                List.of(part)
        );

        TerraSkinnedMeshInstance instanceEyeA = sharedData.createInstance();
        TerraSkinnedMeshInstance instanceEyeB = sharedData.createInstance();

        Bone boneA = new Bone("body", null);
        boneA.setBindPose(0, 0, 0, new Quaternionf(), 1, 1, 1);
        Skeleton skeletonA = new Skeleton(boneA); // Idle rest pose

        Bone boneB = new Bone("body", null);
        boneB.setBindPose(0, 0, 0, new Quaternionf(), 1, 1, 1);
        Skeleton skeletonB = new Skeleton(boneB);
        boneB.localPos.set(50.0f, -25.0f, 80.0f); // High-speed dash pose
        skeletonB.updateMatrices();

        // Skin both instances
        instanceEyeA.skin(skeletonA);
        instanceEyeB.skin(skeletonB);

        float[] posA = instanceEyeA.getParts().get(0).skinnedPositions;
        float[] posB = instanceEyeB.getParts().get(0).skinnedPositions;

        // Eye A must remain in rest pose
        assertEquals(100.0f, posA[0], 1e-5f);
        assertEquals(100.0f, posA[1], 1e-5f);
        assertEquals(100.0f, posA[2], 1e-5f);

        // Eye B must be deformed by dash offset
        assertEquals(150.0f, posB[0], 1e-5f);
        assertEquals(75.0f, posB[1], 1e-5f);
        assertEquals(180.0f, posB[2], 1e-5f);

        // Re-skin Eye B multiple times with different transforms
        for (int i = 0; i < 10; i++) {
            boneB.localPos.set(i * 10.0f, 0, 0);
            skeletonB.updateMatrices();
            instanceEyeB.skin(skeletonB);
        }

        // Eye A must remain completely unaffected and invariant
        assertEquals(100.0f, posA[0], 1e-5f);
        assertEquals(100.0f, posA[1], 1e-5f);
        assertEquals(100.0f, posA[2], 1e-5f);
    }
}
