package com.terraforge.rpg;

import com.terraforge.rpg.client.animation.skeletal.*;
import com.terraforge.rpg.client.model.EyeOfCthulhuModel;
import com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshData;
import com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshInstance;
import com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshLoader;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class EyeDeformationRuntimeTest {

    private static final Path MODELS_DIR = Path.of("src/main/resources/assets/terraforge_rpg/models/entity/boss");

    @Test
    @DisplayName("Verify mesh bounds in Minecraft world space match expected 2.5x2.5m hitbox proportion")
    void testMeshBoundsWithinSanityLimits() throws Exception {
        String[] files = {"eye_of_cthulhu_p1.skin.json", "eye_of_cthulhu_p2.skin.json"};
        float scale = EyeOfCthulhuModel.BASE_SCALE;

        for (String filename : files) {
            File file = MODELS_DIR.resolve(filename).toFile();
            assertTrue(file.exists());

            TerraSkinnedMeshData meshData;
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                meshData = TerraSkinnedMeshLoader.loadFromReader(reader, filename);
            }

            float[] rawBounds = meshData.computeBounds();
            float rawExtX = rawBounds[3] - rawBounds[0];
            float rawExtY = rawBounds[4] - rawBounds[1];
            float rawExtZ = rawBounds[5] - rawBounds[2];

            // In Minecraft space (after 90 degree X rotation and scale):
            // Width = rawExtX * scale
            // Height = rawExtZ * scale
            // Length = rawExtY * scale
            float worldWidth = rawExtX * scale;
            float worldHeight = rawExtZ * scale;
            float worldLength = rawExtY * scale;

            System.out.printf("%s -> World dimensions: %.2f m wide, %.2f m tall, %.2f m long%n",
                    filename, worldWidth, worldHeight, worldLength);

            // Sane thresholds for Eye of Cthulhu in Minecraft:
            // Eyeball body: ~2.5 - 3.5 blocks wide and tall (hitbox is 2.5 x 2.5)
            assertTrue(worldWidth >= 2.5f && worldWidth <= 3.5f,
                    "Eyeball width out of sane range: " + worldWidth);
            assertTrue(worldHeight >= 2.5f && worldHeight <= 3.5f,
                    "Eyeball height out of sane range: " + worldHeight);

            // Total length with tendrils: ~4.5 - 6.0 blocks
            assertTrue(worldLength >= 4.5f && worldLength <= 6.0f,
                    "Eyeball total length out of sane range: " + worldLength);
        }
    }

    @Test
    @DisplayName("Verify animation clip keyframes do not apply insane scales or offsets")
    void testAnimationClipScaleWithinSaneLimits() {
        AnimationController controller = EyeOfCthulhuArmature.createController();
        String[] allClips = {
                "spawn", "idle", "hover", "look", "summon_servant", "charge_prepare",
                "charge", "charge_recover", "hurt", "phase_transition", "phase2_idle",
                "phase2_charge_prepare", "phase2_charge", "phase2_bite", "enrage", "death"
        };

        for (String clipName : allClips) {
            AnimationClip clip = controller.getClip(clipName);
            assertNotNull(clip, "Missing clip: " + clipName);

            for (BoneTrack track : clip.getTracks().values()) {
                for (Keyframe kf : track.getKeyframes()) {
                    // Sane scale limits: scale between 0.05x and 2.5x (e.g. 0.1x for pupil spawn intro)
                    assertTrue(kf.scale.x >= 0.05f && kf.scale.x <= 2.5f,
                            "Insane X scale in " + clipName + " for " + track.getBoneName() + ": " + kf.scale.x);
                    assertTrue(kf.scale.y >= 0.05f && kf.scale.y <= 2.5f,
                            "Insane Y scale in " + clipName + " for " + track.getBoneName() + ": " + kf.scale.y);
                    assertTrue(kf.scale.z >= 0.05f && kf.scale.z <= 2.5f,
                            "Insane Z scale in " + clipName + " for " + track.getBoneName() + ": " + kf.scale.z);

                    // Sane offset limits: offsets within [-5, 5]
                    assertTrue(Math.abs(kf.translation.x) <= 5.0f,
                            "Insane X offset in " + clipName + " for " + track.getBoneName() + ": " + kf.translation.x);
                    assertTrue(Math.abs(kf.translation.y) <= 5.0f,
                            "Insane Y offset in " + clipName + " for " + track.getBoneName() + ": " + kf.translation.y);
                    assertTrue(Math.abs(kf.translation.z) <= 5.0f,
                            "Insane Z offset in " + clipName + " for " + track.getBoneName() + ": " + kf.translation.z);
                }
            }
        }
    }

    @Test
    @DisplayName("Verify jaw pivot remains stable under full rotation range (-55 deg to +55 deg)")
    void testJawPivotIntegrityUnderAllRotations() throws Exception {
        File file = MODELS_DIR.resolve("eye_of_cthulhu_p2.skin.json").toFile();
        assertTrue(file.exists());

        TerraSkinnedMeshData meshData;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            meshData = TerraSkinnedMeshLoader.loadFromReader(reader, file.getName());
        }

        Skeleton skeleton = EyeSkeletonFactory.create(meshData);
        Bone upperJaw = skeleton.getBone("upper_jaw");
        Bone lowerJaw = skeleton.getBone("lower_jaw");

        assertNotNull(upperJaw);
        assertNotNull(lowerJaw);

        Vector4f upOrig = new Vector4f(0, 0, 0, 1).mul(upperJaw.bindWorldMatrix);
        Vector4f loOrig = new Vector4f(0, 0, 0, 1).mul(lowerJaw.bindWorldMatrix);

        float[] angles = {-55f, -35f, -20f, 0f, 20f, 35f, 55f};
        for (float deg : angles) {
            upperJaw.animRot.rotationXYZ((float) Math.toRadians(deg), 0, 0);
            lowerJaw.animRot.rotationXYZ((float) Math.toRadians(-deg), 0, 0);
            skeleton.updateMatrices();

            Vector4f upCurr = new Vector4f(0, 0, 0, 1).mul(upperJaw.worldMatrix);
            Vector4f loCurr = new Vector4f(0, 0, 0, 1).mul(lowerJaw.worldMatrix);

            assertEquals(upOrig.x, upCurr.x, 1e-4f, "Upper jaw pivot X drift at " + deg);
            assertEquals(upOrig.y, upCurr.y, 1e-4f, "Upper jaw pivot Y drift at " + deg);
            assertEquals(upOrig.z, upCurr.z, 1e-4f, "Upper jaw pivot Z drift at " + deg);

            assertEquals(loOrig.x, loCurr.x, 1e-4f, "Lower jaw pivot X drift at " + deg);
            assertEquals(loOrig.y, loCurr.y, 1e-4f, "Lower jaw pivot Y drift at " + deg);
            assertEquals(loOrig.z, loCurr.z, 1e-4f, "Lower jaw pivot Z drift at " + deg);
        }
    }

    @Test
    @DisplayName("Simulate runtime animation playback and verify vertex stability across all clips")
    void testAnimationVertexStability() throws Exception {
        String[] files = {"eye_of_cthulhu_p1.skin.json", "eye_of_cthulhu_p2.skin.json"};
        String[] clipsP1 = {"spawn", "idle", "hover", "look", "summon_servant", "charge_prepare", "charge", "charge_recover", "hurt", "phase_transition"};
        String[] clipsP2 = {"phase2_idle", "phase2_charge_prepare", "phase2_charge", "phase2_bite", "enrage", "death"};

        for (int f = 0; f < files.length; f++) {
            String filename = files[f];
            File file = MODELS_DIR.resolve(filename).toFile();
            assertTrue(file.exists());

            TerraSkinnedMeshData meshData;
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                meshData = TerraSkinnedMeshLoader.loadFromReader(reader, filename);
            }

            Skeleton skeleton = EyeSkeletonFactory.create(meshData);
            TerraSkinnedMeshInstance instance = meshData.createInstance();
            AnimationController controller = EyeOfCthulhuArmature.createController();

            String[] testClips = (f == 0) ? clipsP1 : clipsP2;

            for (String clipName : testClips) {
                controller.play(clipName, true);
                float duration = controller.getClip(clipName).getDuration();

                // Sample at 10 time steps through the clip
                for (int step = 0; step <= 10; step++) {
                    float time = (float) step / 10.0f * duration;
                    controller.update(time - controller.getCurrentTime());
                    controller.apply(skeleton);
                    instance.skin(skeleton);

                    // Check bounds for each part
                    for (TerraSkinnedMeshInstance.PartInstance part : instance.getParts()) {
                        float[] skinPos = part.skinnedPositions;
                        for (int v = 0; v < part.data.vertexCount; v++) {
                            int v3 = v * 3;
                            float x = skinPos[v3];
                            float y = skinPos[v3 + 1];
                            float z = skinPos[v3 + 2];

                            assertFalse(Float.isNaN(x) || Float.isInfinite(x), "NaN/Inf in vertex X for " + clipName + " at t=" + time);
                            assertFalse(Float.isNaN(y) || Float.isInfinite(y), "NaN/Inf in vertex Y for " + clipName + " at t=" + time);
                            assertFalse(Float.isNaN(z) || Float.isInfinite(z), "NaN/Inf in vertex Z for " + clipName + " at t=" + time);

                            // The raw mesh is ~450x750x450 in GLTF units.
                            // If any vertex exceeds 2000 units, skinning is exploding!
                            assertTrue(Math.abs(x) < 2000.0f, "Vertex exploded laterally: " + x + " in " + clipName + " at t=" + time);
                            assertTrue(Math.abs(y) < 2000.0f, "Vertex exploded vertically: " + y + " in " + clipName + " at t=" + time);
                            assertTrue(Math.abs(z) < 2000.0f, "Vertex exploded in depth: " + z + " in " + clipName + " at t=" + time);
                        }
                    }
                }
            }
        }
    }
}
