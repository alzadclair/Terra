package com.terraforge.rpg;

import com.terraforge.rpg.client.animation.skeletal.*;
import com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshData;
import com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshInstance;
import com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshLoader;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class EyeP2SubdivisionAndContinuityTest {

    private static final Path MODELS_DIR = Path.of("src/main/resources/assets/terraforge_rpg/models/entity/boss");

    /**
     * Replicates the exact algorithm in tools/assets/retarget_eye_geometry.py
     * to verify the midpoint skin weight interpolation specification.
     */
    public static class SubdividedMidpoint {
        public final int[] boneIndices;
        public final float[] boneWeights;

        public SubdividedMidpoint(int[] boneIndices, float[] boneWeights) {
            this.boneIndices = boneIndices;
            this.boneWeights = boneWeights;
        }

        public static SubdividedMidpoint interpolate(int[] idx0, float[] w0, int[] idx1, float[] w1) {
            Map<Integer, Float> weightMap = new HashMap<>();
            for (int k = 0; k < 4; k++) {
                if (w0[k] > 1e-5f) {
                    weightMap.put(idx0[k], weightMap.getOrDefault(idx0[k], 0.0f) + 0.5f * w0[k]);
                }
                if (w1[k] > 1e-5f) {
                    weightMap.put(idx1[k], weightMap.getOrDefault(idx1[k], 0.0f) + 0.5f * w1[k]);
                }
            }

            List<Map.Entry<Integer, Float>> sorted = new ArrayList<>(weightMap.entrySet());
            sorted.sort((a, b) -> Float.compare(b.getValue(), a.getValue()));

            int topCount = Math.min(4, sorted.size());
            float totalW = 0.0f;
            for (int i = 0; i < topCount; i++) {
                totalW += sorted.get(i).getValue();
            }

            int[] outIndices = new int[4];
            float[] outWeights = new float[4];

            if (totalW < 1e-5f) {
                outWeights[0] = 1.0f;
            } else {
                for (int i = 0; i < topCount; i++) {
                    outIndices[i] = sorted.get(i).getKey();
                    outWeights[i] = Math.round((sorted.get(i).getValue() / totalW) * 10000.0f) / 10000.0f;
                }
                float sum = 0.0f;
                for (float w : outWeights) sum += w;
                outWeights[0] += (1.0f - sum);
            }

            return new SubdividedMidpoint(outIndices, outWeights);
        }

        public float getWeightForBone(int boneIndex) {
            for (int i = 0; i < 4; i++) {
                if (boneIndices[i] == boneIndex) {
                    return boneWeights[i];
                }
            }
            return 0.0f;
        }
    }

    @Test
    @DisplayName("Verify edge midpoint skin weight interpolation: combines endpoints A (body=1.0) and B (upper_jaw=1.0) into body~=0.5, upper_jaw~=0.5")
    void testSubdivisionMidpointSkinWeightInterpolation() {
        int BONE_BODY = 0;
        int BONE_UPPER_JAW = 1;

        int[] idxA = {BONE_BODY, 0, 0, 0};
        float[] wA = {1.0f, 0.0f, 0.0f, 0.0f};

        int[] idxB = {BONE_UPPER_JAW, 0, 0, 0};
        float[] wB = {1.0f, 0.0f, 0.0f, 0.0f};

        SubdividedMidpoint mid = SubdividedMidpoint.interpolate(idxA, wA, idxB, wB);

        float weightBody = mid.getWeightForBone(BONE_BODY);
        float weightUpperJaw = mid.getWeightForBone(BONE_UPPER_JAW);

        // Crucial requirement: Must NOT simply copy weight 1.0 from endpoint A or endpoint B
        assertNotEquals(1.0f, weightBody, 0.01f, "Midpoint must NOT copy 1.0 body weight directly from endpoint A");
        assertNotEquals(1.0f, weightUpperJaw, 0.01f, "Midpoint must NOT copy 1.0 upper_jaw weight directly from endpoint B");

        // Must interpolate influences from both endpoints
        assertEquals(0.5f, weightBody, 0.02f, "Expected body weight ~= 0.5 at midpoint");
        assertEquals(0.5f, weightUpperJaw, 0.02f, "Expected upper_jaw weight ~= 0.5 at midpoint");

        // Must sum to exactly 1.0
        float sum = 0.0f;
        for (float w : mid.boneWeights) sum += w;
        assertEquals(1.0f, sum, 1e-4f, "Midpoint skin weights must sum to 1.0");
    }

    @Test
    @DisplayName("Verify P2 mesh continuity across animation clips: phase2_idle, phase2_charge, phase2_bite (no tearing, no floating teeth, finite bounds)")
    void testPhase2ContinuityAcrossAnimations() throws Exception {
        File file = MODELS_DIR.resolve("eye_of_cthulhu_p2.skin.json").toFile();
        assertTrue(file.exists(), "eye_of_cthulhu_p2.skin.json must exist");

        TerraSkinnedMeshData meshData;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            meshData = TerraSkinnedMeshLoader.loadFromReader(reader, "eye_of_cthulhu_p2");
        }
        assertNotNull(meshData);

        Skeleton skeleton = EyeSkeletonFactory.create(meshData);
        TerraSkinnedMeshInstance instance = meshData.createInstance();
        AnimationController controller = EyeOfCthulhuArmature.createController();

        String[] p2Clips = {"phase2_idle", "phase2_charge", "phase2_bite"};

        for (String clipName : p2Clips) {
            controller.play(clipName, false);
            float duration = controller.getClip(clipName).getDuration();
            assertTrue(duration > 0.0f, "Clip " + clipName + " must have positive duration");

            // Sample across 15 frames per animation
            int numSamples = 15;
            for (int s = 0; s <= numSamples; s++) {
                float targetTime = ((float) s / numSamples) * duration;
                float dt = targetTime - controller.getCurrentTime();
                controller.update(dt);
                controller.apply(skeleton);
                instance.skin(skeleton);

                Vector3f bodyMin = new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
                Vector3f bodyMax = new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
                Vector3f teethMin = new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
                Vector3f teethMax = new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);

                for (TerraSkinnedMeshInstance.PartInstance part : instance.getParts()) {
                    int count = part.data.vertexCount;
                    float[] pos = part.skinnedPositions;
                    float[] norm = part.skinnedNormals;
                    int[] ind = part.data.indices;

                    for (int v = 0; v < count; v++) {
                        int v3 = v * 3;
                        float x = pos[v3];
                        float y = pos[v3 + 1];
                        float z = pos[v3 + 2];

                        // Invariant: Finite vertices (no NaN / Infinity)
                        assertFalse(Float.isNaN(x) || Float.isInfinite(x), "NaN/Inf in vertex X for " + clipName + " at t=" + targetTime);
                        assertFalse(Float.isNaN(y) || Float.isInfinite(y), "NaN/Inf in vertex Y for " + clipName + " at t=" + targetTime);
                        assertFalse(Float.isNaN(z) || Float.isInfinite(z), "NaN/Inf in vertex Z for " + clipName + " at t=" + targetTime);

                        // Invariant: Normalized normals
                        float nx = norm[v3];
                        float ny = norm[v3 + 1];
                        float nz = norm[v3 + 2];
                        float nLen = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
                        assertEquals(1.0f, nLen, 0.05f, "Normal length must remain normalized (~1.0) in " + part.data.name);

                        // Track part bounds
                        if ("body".equals(part.data.name)) {
                            bodyMin.min(new Vector3f(x, y, z));
                            bodyMax.max(new Vector3f(x, y, z));
                        } else if ("teeth".equals(part.data.name) || "inner_teeth".equals(part.data.name)) {
                            teethMin.min(new Vector3f(x, y, z));
                            teethMax.max(new Vector3f(x, y, z));
                        }
                    }

                    // Check triangle edge lengths: no crazy tearing / vertex popping
                    for (int i = 0; i < ind.length; i += 3) {
                        int i0 = ind[i] * 3;
                        int i1 = ind[i + 1] * 3;
                        int i2 = ind[i + 2] * 3;

                        float d01 = dist(pos[i0], pos[i0+1], pos[i0+2], pos[i1], pos[i1+1], pos[i1+2]);
                        float d12 = dist(pos[i1], pos[i1+1], pos[i1+2], pos[i2], pos[i2+1], pos[i2+2]);
                        float d20 = dist(pos[i2], pos[i2+1], pos[i2+2], pos[i0], pos[i0+1], pos[i0+2]);

                        // Triangle edges in the boss asset space must remain under reasonable max length (< 600 units)
                        assertTrue(d01 < 600.0f, "Torn edge d01=" + d01 + " in " + part.data.name + " during " + clipName);
                        assertTrue(d12 < 600.0f, "Torn edge d12=" + d12 + " in " + part.data.name + " during " + clipName);
                        assertTrue(d20 < 600.0f, "Torn edge d20=" + d20 + " in " + part.data.name + " during " + clipName);
                    }
                }

                // Invariant: Teeth must not float away from body (teeth bounds overlap body bounds)
                assertTrue(teethMin.x < bodyMax.x && teethMax.x > bodyMin.x, "Teeth separated along X during " + clipName);
                assertTrue(teethMin.y < bodyMax.y && teethMax.y > bodyMin.y, "Teeth separated along Y during " + clipName);
                assertTrue(teethMin.z < bodyMax.z && teethMax.z > bodyMin.z, "Teeth separated along Z during " + clipName);
            }
        }
    }

    private static float dist(float x1, float y1, float z1, float x2, float y2, float z2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        float dz = z1 - z2;
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
