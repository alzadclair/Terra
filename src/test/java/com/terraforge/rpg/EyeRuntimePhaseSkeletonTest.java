package com.terraforge.rpg;

import com.terraforge.rpg.client.animation.skeletal.AnimationController;
import com.terraforge.rpg.client.animation.skeletal.Bone;
import com.terraforge.rpg.client.animation.skeletal.Skeleton;
import com.terraforge.rpg.client.render.entity.state.EyeRenderState;
import com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshData;
import com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshInstance;
import com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class EyeRuntimePhaseSkeletonTest {

    private static final Path MODELS_DIR = Path.of("src/main/resources/assets/terraforge_rpg/models/entity/boss");
    private TerraSkinnedMeshData meshDataP1;
    private TerraSkinnedMeshData meshDataP2;

    @BeforeEach
    void setUp() throws Exception {
        File fileP1 = MODELS_DIR.resolve("eye_of_cthulhu_p1.skin.json").toFile();
        File fileP2 = MODELS_DIR.resolve("eye_of_cthulhu_p2.skin.json").toFile();

        assertTrue(fileP1.exists(), "P1 skin file must exist");
        assertTrue(fileP2.exists(), "P2 skin file must exist");

        try (BufferedReader reader = new BufferedReader(new FileReader(fileP1))) {
            meshDataP1 = TerraSkinnedMeshLoader.loadFromReader(reader, fileP1.getName());
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(fileP2))) {
            meshDataP2 = TerraSkinnedMeshLoader.loadFromReader(reader, fileP2.getName());
        }

        assertNotNull(meshDataP1);
        assertNotNull(meshDataP2);
    }

    @Test
    @DisplayName("Verify Phase 1 EyeRenderState runtime rest pose has < 1e-4 identity error")
    void testPhase1RuntimeRestPoseError() {
        UUID uuid = UUID.randomUUID();
        EyeRenderState state = EyeRenderState.create(uuid, meshDataP1, meshDataP2);

        Skeleton skeletonP1 = state.getSkeleton(false);
        assertNotNull(skeletonP1);

        TerraSkinnedMeshInstance instanceP1 = state.getMeshInstance(false, meshDataP1);
        assertNotNull(instanceP1);

        instanceP1.skin(skeletonP1);

        float maxPosErr = 0.0f;
        float maxNormErr = 0.0f;
        int totalVerts = 0;

        for (TerraSkinnedMeshInstance.PartInstance part : instanceP1.getParts()) {
            int count = part.data.vertexCount;
            totalVerts += count;
            float[] bindPos = part.data.bindPositions;
            float[] bindNorm = part.data.bindNormals;
            float[] skinPos = part.skinnedPositions;
            float[] skinNorm = part.skinnedNormals;

            for (int v = 0; v < count * 3; v++) {
                float posDiff = Math.abs(skinPos[v] - bindPos[v]);
                if (posDiff > maxPosErr) maxPosErr = posDiff;
                assertTrue(posDiff < 1e-4f, String.format("P1 pos diff %.6f at idx %d in part %s", posDiff, v, part.data.name));

                float normDiff = Math.abs(skinNorm[v] - bindNorm[v]);
                if (normDiff > maxNormErr) maxNormErr = normDiff;
                assertTrue(normDiff < 1e-4f, String.format("P1 norm diff %.6f at idx %d in part %s", normDiff, v, part.data.name));
            }
        }

        assertTrue(totalVerts >= 4500, "Phase 1 must have at least 4500 vertices");
        System.out.printf("Eye Phase 1 Runtime Rest Pose: %d vertices, maxPosErr = %.2e, maxNormErr = %.2e%n",
                totalVerts, maxPosErr, maxNormErr);
    }

    @Test
    @DisplayName("Verify Phase 2 EyeRenderState runtime rest pose has < 1e-4 identity error")
    void testPhase2RuntimeRestPoseError() {
        UUID uuid = UUID.randomUUID();
        EyeRenderState state = EyeRenderState.create(uuid, meshDataP1, meshDataP2);

        Skeleton skeletonP2 = state.getSkeleton(true);
        assertNotNull(skeletonP2);

        TerraSkinnedMeshInstance instanceP2 = state.getMeshInstance(true, meshDataP2);
        assertNotNull(instanceP2);

        instanceP2.skin(skeletonP2);

        float maxPosErr = 0.0f;
        float maxNormErr = 0.0f;
        int totalVerts = 0;

        for (TerraSkinnedMeshInstance.PartInstance part : instanceP2.getParts()) {
            int count = part.data.vertexCount;
            totalVerts += count;
            float[] bindPos = part.data.bindPositions;
            float[] bindNorm = part.data.bindNormals;
            float[] skinPos = part.skinnedPositions;
            float[] skinNorm = part.skinnedNormals;

            for (int v = 0; v < count * 3; v++) {
                float posDiff = Math.abs(skinPos[v] - bindPos[v]);
                if (posDiff > maxPosErr) maxPosErr = posDiff;
                assertTrue(posDiff < 1e-4f, String.format("P2 pos diff %.6f at idx %d in part %s", posDiff, v, part.data.name));

                float normDiff = Math.abs(skinNorm[v] - bindNorm[v]);
                if (normDiff > maxNormErr) maxNormErr = normDiff;
                assertTrue(normDiff < 1e-4f, String.format("P2 norm diff %.6f at idx %d in part %s", normDiff, v, part.data.name));
            }
        }

        assertTrue(totalVerts >= 6000, "Phase 2 must have at least 6000 vertices");
        System.out.printf("Eye Phase 2 Runtime Rest Pose: %d vertices, maxPosErr = %.2e, maxNormErr = %.2e%n",
                totalVerts, maxPosErr, maxNormErr);
    }

    @Test
    @DisplayName("Negative Test: Pairing P1 mesh with P2 skeleton produces massive deformation error (> 10 units)")
    void testNegativeMismatchedPhaseDeformationError() {
        UUID uuid = UUID.randomUUID();
        EyeRenderState state = EyeRenderState.create(uuid, meshDataP1, meshDataP2);

        // Intentionally skin P1 mesh with P2 skeleton (the prior architectural bug)
        Skeleton skeletonP2 = state.getSkeleton(true);
        TerraSkinnedMeshInstance wrongInstance = meshDataP1.createInstance();
        wrongInstance.skin(skeletonP2);

        float maxPosDiff = 0.0f;
        for (TerraSkinnedMeshInstance.PartInstance part : wrongInstance.getParts()) {
            float[] bindPos = part.data.bindPositions;
            float[] skinPos = part.skinnedPositions;
            for (int v = 0; v < part.data.vertexCount * 3; v++) {
                float diff = Math.abs(skinPos[v] - bindPos[v]);
                if (diff > maxPosDiff) {
                    maxPosDiff = diff;
                }
            }
        }

        System.out.printf("Negative Test (P1 Mesh + P2 Skeleton): maxPosDiff = %.2f units%n", maxPosDiff);
        assertTrue(maxPosDiff > 10.0f,
                String.format("Cross-phase pairing must fail with large error (> 10 units), got %.2f", maxPosDiff));

        // Also test reverse: P2 mesh with P1 skeleton
        Skeleton skeletonP1 = state.getSkeleton(false);
        TerraSkinnedMeshInstance reverseWrongInstance = meshDataP2.createInstance();
        reverseWrongInstance.skin(skeletonP1);

        float reverseMaxPosDiff = 0.0f;
        for (TerraSkinnedMeshInstance.PartInstance part : reverseWrongInstance.getParts()) {
            float[] bindPos = part.data.bindPositions;
            float[] skinPos = part.skinnedPositions;
            for (int v = 0; v < part.data.vertexCount * 3; v++) {
                float diff = Math.abs(skinPos[v] - bindPos[v]);
                if (diff > reverseMaxPosDiff) {
                    reverseMaxPosDiff = diff;
                }
            }
        }

        System.out.printf("Negative Test (P2 Mesh + P1 Skeleton): maxPosDiff = %.2f units%n", reverseMaxPosDiff);
        assertTrue(reverseMaxPosDiff > 10.0f,
                String.format("Cross-phase pairing must fail with large error (> 10 units), got %.2f", reverseMaxPosDiff));
    }

    @Test
    @DisplayName("Verify clean phase transition preserves state isolation and switches skeletons correctly")
    void testPhaseSwitchPreservation() {
        UUID uuid = UUID.randomUUID();
        EyeRenderState state = EyeRenderState.create(uuid, meshDataP1, meshDataP2);

        // 1. In Phase 1: get P1 skeleton, controller, instance
        Skeleton skelP1 = state.getSkeleton(false);
        AnimationController ctrlP1 = state.getAnimController(false);
        TerraSkinnedMeshInstance instP1 = state.getMeshInstance(false, meshDataP1);

        assertEquals("idle", ctrlP1.getCurrentClipName());
        ctrlP1.update(0.1f);
        ctrlP1.apply(skelP1);
        instP1.skin(skelP1);

        // 2. In Phase 2: get P2 skeleton, controller, instance
        Skeleton skelP2 = state.getSkeleton(true);
        AnimationController ctrlP2 = state.getAnimController(true);
        TerraSkinnedMeshInstance instP2 = state.getMeshInstance(true, meshDataP2);

        assertEquals("phase2_idle", ctrlP2.getCurrentClipName());
        ctrlP2.update(0.1f);
        ctrlP2.apply(skelP2);
        instP2.skin(skelP2);

        // 3. Verify complete separation
        assertNotSame(skelP1, skelP2, "P1 and P2 skeletons must be distinct instances");
        assertNotSame(ctrlP1, ctrlP2, "P1 and P2 animation controllers must be distinct instances");
        assertNotSame(instP1, instP2, "P1 and P2 mesh instances must be distinct objects");

        // Verify bind poses are distinct between P1 and P2 skeletons
        Bone jawP1 = skelP1.getBone("upper_jaw");
        Bone jawP2 = skelP2.getBone("upper_jaw");
        assertNotEquals(jawP1.bindLocalMatrix.m31(), jawP2.bindLocalMatrix.m31(),
                "Upper jaw local bind Y must differ between P1 and P2");
    }
}
