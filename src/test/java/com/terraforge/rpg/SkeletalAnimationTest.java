package com.terraforge.rpg;

import com.terraforge.rpg.client.animation.skeletal.*;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SkeletalAnimationTest {

    @Test
    @DisplayName("Armature Master Rig hierarchy and bone count")
    void testArmatureHierarchy() {
        Skeleton skeleton = EyeOfCthulhuArmature.createArmature();
        assertNotNull(skeleton.getRoot());
        assertEquals("root", skeleton.getRoot().getName());

        // Master Rig required bones
        String[] requiredBones = {
                "root", "body", "iris", "pupil", "optic_back",
                "jaw_root", "upper_jaw", "teeth_upper", "lower_jaw", "teeth_lower",
                "tendril_01", "tendril_02", "tendril_03", "tendril_04", "tendril_05", "tendril_06"
        };

        for (String boneName : requiredBones) {
            assertNotNull(skeleton.getBone(boneName), "Missing required bone: " + boneName);
        }

        assertEquals(16, skeleton.getBoneCount());
    }

    @Test
    @DisplayName("Bind pose skinMatrix defaults to identity matrix")
    void testBindPoseSkinMatrixIdentity() {
        Skeleton skeleton = EyeOfCthulhuArmature.createArmature();
        for (Bone bone : skeleton.getAllBones().values()) {
            // In bind pose: skinMatrix = bindWorldMatrix * invBindWorldMatrix = Identity
            Vector3f testVec = new Vector3f(1.0f, 2.0f, 3.0f);
            Vector3f transformed = new Vector3f();
            bone.skinMatrix.transformPosition(testVec, transformed);

            assertEquals(testVec.x, transformed.x, 0.0001f, "Bone " + bone.getName() + " skinMatrix X is not identity");
            assertEquals(testVec.y, transformed.y, 0.0001f, "Bone " + bone.getName() + " skinMatrix Y is not identity");
            assertEquals(testVec.z, transformed.z, 0.0001f, "Bone " + bone.getName() + " skinMatrix Z is not identity");
        }
    }

    @Test
    @DisplayName("BoneTrack LERP and SLERP interpolation accuracy")
    void testBoneTrackInterpolation() {
        BoneTrack track = new BoneTrack("test_bone")
                .addKeyframe(0.0f, 0.0f, 0.0f, 0.0f, new Quaternionf().identity(), 1.0f, 1.0f, 1.0f)
                .addKeyframe(1.0f, 10.0f, 20.0f, 30.0f, new Quaternionf().rotationY((float) Math.PI), 2.0f, 2.0f, 2.0f);

        BoneTransform result = new BoneTransform();

        // Sample at midpoint 0.5s
        track.sample(0.5f, result);

        assertEquals(5.0f, result.translation.x, 0.001f);
        assertEquals(10.0f, result.translation.y, 0.001f);
        assertEquals(15.0f, result.translation.z, 0.001f);
        assertEquals(1.5f, result.scale.x, 0.001f);

        // Rotation at 0.5 should be rotated around Y by PI/2
        Vector3f forward = new Vector3f(0.0f, 0.0f, 1.0f);
        Vector3f rotated = new Vector3f();
        result.rotation.transform(forward, rotated);
        assertEquals(1.0f, rotated.x, 0.001f); // Rotated to +X
        assertEquals(0.0f, rotated.z, 0.001f);
    }

    @Test
    @DisplayName("Controller registers all 16 required canonical clips")
    void testAllSixteenClipsRegistered() {
        AnimationController controller = EyeOfCthulhuArmature.createController();

        String[] requiredClips = {
                "spawn", "idle", "hover", "look", "summon_servant",
                "charge_prepare", "charge", "charge_recover", "hurt",
                "phase_transition", "phase2_idle", "phase2_charge_prepare",
                "phase2_charge", "phase2_bite", "enrage", "death"
        };

        for (String clipName : requiredClips) {
            assertTrue(controller.hasClip(clipName), "Missing canonical clip: " + clipName);
            AnimationClip clip = controller.getClip(clipName);
            assertTrue(clip.getDuration() > 0.0f);
            assertFalse(clip.getTracks().isEmpty(), "Clip has no tracks: " + clipName);
        }
    }

    @Test
    @DisplayName("Cross-fade blending interpolates between two animation clips smoothly")
    void testCrossFadeBlending() {
        Skeleton skeleton = EyeOfCthulhuArmature.createArmature();
        AnimationController controller = EyeOfCthulhuArmature.createController();

        controller.play("idle", true);
        controller.update(0.1f);
        controller.apply(skeleton);

        // Trigger cross-fade to phase2_charge_prepare over 0.5s
        controller.crossFade("phase2_charge_prepare", 0.5f);
        assertTrue(controller.isBlending());
        assertEquals("phase2_charge_prepare", controller.getCurrentClipName());

        // Update halfway through blend (0.25s)
        controller.update(0.25f);
        assertTrue(controller.isBlending());
        controller.apply(skeleton);

        // Update past blend duration (another 0.3s -> total 0.55s)
        controller.update(0.3f);
        assertFalse(controller.isBlending());
    }
}
