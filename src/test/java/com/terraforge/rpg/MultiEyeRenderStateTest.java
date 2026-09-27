package com.terraforge.rpg;

import com.terraforge.rpg.client.animation.skeletal.Bone;
import com.terraforge.rpg.client.animation.skeletal.Skeleton;
import com.terraforge.rpg.client.render.entity.state.EyeRenderState;
import com.terraforge.rpg.client.render.entity.state.EyeRenderStateManager;
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

public class MultiEyeRenderStateTest {

    private static final Path MODELS_DIR = Path.of("src/main/resources/assets/terraforge_rpg/models/entity/boss");
    private TerraSkinnedMeshData meshDataP1;
    private TerraSkinnedMeshData meshDataP2;

    @BeforeEach
    void setUp() throws Exception {
        EyeRenderStateManager.clear();
        File fileP1 = MODELS_DIR.resolve("eye_of_cthulhu_p1.skin.json").toFile();
        File fileP2 = MODELS_DIR.resolve("eye_of_cthulhu_p2.skin.json").toFile();
        assertTrue(fileP1.exists());
        assertTrue(fileP2.exists());

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
    @DisplayName("Verify 5 mixed-phase simultaneous Eyes (P1 idle, P1 charge, transitioning, P2 bite, P2 enrage) have full state & skeleton isolation")
    void testMultiEyeMixedPhaseStateIsolation() {
        UUID idA = UUID.randomUUID(); // P1 idle
        UUID idB = UUID.randomUUID(); // P1 charge
        UUID idC = UUID.randomUUID(); // Transitioning (P1 transition)
        UUID idD = UUID.randomUUID(); // P2 bite
        UUID idE = UUID.randomUUID(); // P2 enrage

        EyeRenderState stateA = EyeRenderState.create(idA, meshDataP1, meshDataP2);
        EyeRenderState stateB = EyeRenderState.create(idB, meshDataP1, meshDataP2);
        EyeRenderState stateC = EyeRenderState.create(idC, meshDataP1, meshDataP2);
        EyeRenderState stateD = EyeRenderState.create(idD, meshDataP1, meshDataP2);
        EyeRenderState stateE = EyeRenderState.create(idE, meshDataP1, meshDataP2);

        EyeRenderStateManager.put(idA, stateA);
        EyeRenderStateManager.put(idB, stateB);
        EyeRenderStateManager.put(idC, stateC);
        EyeRenderStateManager.put(idD, stateD);
        EyeRenderStateManager.put(idE, stateE);

        assertEquals(5, EyeRenderStateManager.getActiveCount());

        // Eyes A, B, C are Phase 1 (isPhase2 = false)
        // Eyes D, E are Phase 2 (isPhase2 = true)
        stateA.getAnimController(false).play("idle", true);
        stateB.getAnimController(false).play("charge", true);
        stateC.getAnimController(false).play("phase_transition", false);
        stateD.getAnimController(true).play("phase2_bite", false);
        stateE.getAnimController(true).play("enrage", true);

        // Simulate 40 animation update steps in interleaved, scrambled order
        float[] times = {0.05f, 0.10f, 0.05f, 0.08f, 0.06f};
        for (int step = 0; step < 40; step++) {
            // Update in scrambled order: C, A, E, B, D
            stateC.getAnimController(false).update(times[2]);
            stateC.getAnimController(false).apply(stateC.getSkeleton(false));

            stateA.getAnimController(false).update(times[0]);
            stateA.getAnimController(false).apply(stateA.getSkeleton(false));

            stateE.getAnimController(true).update(times[4]);
            stateE.getAnimController(true).apply(stateE.getSkeleton(true));

            stateB.getAnimController(false).update(times[1]);
            stateB.getAnimController(false).apply(stateB.getSkeleton(false));

            stateD.getAnimController(true).update(times[3]);
            stateD.getAnimController(true).apply(stateD.getSkeleton(true));
        }

        // 1. Verify clip names remained completely distinct
        assertEquals("idle", stateA.getAnimController(false).getCurrentClipName());
        assertEquals("charge", stateB.getAnimController(false).getCurrentClipName());
        assertEquals("phase_transition", stateC.getAnimController(false).getCurrentClipName());
        assertEquals("phase2_bite", stateD.getAnimController(true).getCurrentClipName());
        assertEquals("enrage", stateE.getAnimController(true).getCurrentClipName());

        // 2. Verify playback times are completely independent
        assertEquals(40 * times[0], stateA.getAnimController(false).getCurrentTime(), 1e-4f);
        assertEquals(40 * times[1], stateB.getAnimController(false).getCurrentTime(), 1e-4f);
        assertEquals(40 * times[2], stateC.getAnimController(false).getCurrentTime(), 1e-4f);
        assertEquals(40 * times[3], stateD.getAnimController(true).getCurrentTime(), 1e-4f);
        assertEquals(40 * times[4], stateE.getAnimController(true).getCurrentTime(), 1e-4f);

        // 3. Verify skeletons hold distinct matrices and phase-specific origins
        Skeleton skelA = stateA.getSkeleton(false);
        Skeleton skelB = stateB.getSkeleton(false);
        Skeleton skelC = stateC.getSkeleton(false);
        Skeleton skelD = stateD.getSkeleton(true);
        Skeleton skelE = stateE.getSkeleton(true);

        // A, B, C are Phase 1 skeletons (upper jaw bound near origin)
        assertEquals(0.0f, skelA.getBone("upper_jaw").bindLocalMatrix.m31(), 0.01f);
        assertEquals(0.0f, skelB.getBone("upper_jaw").bindLocalMatrix.m31(), 0.01f);
        assertEquals(0.0f, skelC.getBone("upper_jaw").bindLocalMatrix.m31(), 0.01f);

        // D, E are Phase 2 skeletons (upper jaw bound at hinge pivot ~ 12.302)
        assertEquals(12.302f, Math.abs(skelD.getBone("upper_jaw").bindLocalMatrix.m31()), 0.05f);
        assertEquals(12.302f, Math.abs(skelE.getBone("upper_jaw").bindLocalMatrix.m31()), 0.05f);

        // Body and jaw animation rotations differ across all instances
        assertNotEquals(skelA.getBone("body").animRot.y, skelC.getBone("body").animRot.y, "P1 idle and transition bodies must differ");
        Bone jawA = skelA.getBone("upper_jaw");
        Bone jawC = skelC.getBone("upper_jaw");
        Bone jawD = skelD.getBone("upper_jaw");
        Bone jawE = skelE.getBone("upper_jaw");

        assertNotEquals(jawA.animRot.x, jawC.animRot.x, "P1 idle and transition upper jaws must differ");
        assertNotEquals(jawD.animRot.x, jawE.animRot.x, "P2 bite and enrage upper jaws must differ");

        // 4. Verify skinned mesh instances compute independent vertex buffers for their respective phases
        TerraSkinnedMeshInstance instA = stateA.getMeshInstance(false, meshDataP1);
        TerraSkinnedMeshInstance instD = stateD.getMeshInstance(true, meshDataP2);

        instA.skin(skelA);
        instD.skin(skelD);

        assertNotSame(instA, instD, "Instances must be separate objects per state and phase");
        assertEquals(meshDataP1.getParts().get(0).vertexCount, instA.getParts().get(0).data.vertexCount);
        assertEquals(meshDataP2.getParts().get(0).vertexCount, instD.getParts().get(0).data.vertexCount);

        // 5. Verify cleanup removes state without leak
        EyeRenderStateManager.remove(idA);
        assertEquals(4, EyeRenderStateManager.getActiveCount());
        assertNull(EyeRenderStateManager.get(idA));

        EyeRenderStateManager.clear();
        assertEquals(0, EyeRenderStateManager.getActiveCount());
    }
}
