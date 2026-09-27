package com.terraforge.rpg;

import com.terraforge.rpg.client.animation.skeletal.Bone;
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
    private TerraSkinnedMeshData meshData;

    @BeforeEach
    void setUp() throws Exception {
        EyeRenderStateManager.clear();
        File file = MODELS_DIR.resolve("eye_of_cthulhu_p2.skin.json").toFile();
        assertTrue(file.exists());
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            meshData = TerraSkinnedMeshLoader.loadFromReader(reader, file.getName());
        }
        assertNotNull(meshData);
    }

    @Test
    @DisplayName("Verify 5 simultaneous Eyes have complete render & animation state isolation with zero cross-contamination")
    void testMultiEyeStateIsolation() {
        UUID idIdle = UUID.randomUUID();
        UUID idCharge = UUID.randomUUID();
        UUID idTrans = UUID.randomUUID();
        UUID idBite = UUID.randomUUID();
        UUID idDeath = UUID.randomUUID();

        EyeRenderState stateIdle = EyeRenderState.create(idIdle, meshData);
        EyeRenderState stateCharge = EyeRenderState.create(idCharge, meshData);
        EyeRenderState stateTrans = EyeRenderState.create(idTrans, meshData);
        EyeRenderState stateBite = EyeRenderState.create(idBite, meshData);
        EyeRenderState stateDeath = EyeRenderState.create(idDeath, meshData);

        EyeRenderStateManager.put(idIdle, stateIdle);
        EyeRenderStateManager.put(idCharge, stateCharge);
        EyeRenderStateManager.put(idTrans, stateTrans);
        EyeRenderStateManager.put(idBite, stateBite);
        EyeRenderStateManager.put(idDeath, stateDeath);

        assertEquals(5, EyeRenderStateManager.getActiveCount());

        // Play different clips
        stateIdle.getAnimController().play("phase2_idle", true);
        stateCharge.getAnimController().play("phase2_charge", true);
        stateTrans.getAnimController().play("phase_transition", true);
        stateBite.getAnimController().play("phase2_bite", true);
        stateDeath.getAnimController().play("death", true);

        // Simulate 40 animation update steps in interleaved, random order
        float[] times = {0.05f, 0.10f, 0.03f, 0.08f, 0.06f};
        for (int step = 0; step < 40; step++) {
            // Update in scrambled order: 3, 0, 4, 1, 2
            stateTrans.getAnimController().update(times[2]);
            stateTrans.getAnimController().apply(stateTrans.getSkeleton());

            stateIdle.getAnimController().update(times[0]);
            stateIdle.getAnimController().apply(stateIdle.getSkeleton());

            stateDeath.getAnimController().update(times[4]);
            stateDeath.getAnimController().apply(stateDeath.getSkeleton());

            stateCharge.getAnimController().update(times[1]);
            stateCharge.getAnimController().apply(stateCharge.getSkeleton());

            stateBite.getAnimController().update(times[3]);
            stateBite.getAnimController().apply(stateBite.getSkeleton());
        }

        // 1. Verify clip names remained completely distinct
        assertEquals("phase2_idle", stateIdle.getAnimController().getCurrentClipName());
        assertEquals("phase2_charge", stateCharge.getAnimController().getCurrentClipName());
        assertEquals("phase_transition", stateTrans.getAnimController().getCurrentClipName());
        assertEquals("phase2_bite", stateBite.getAnimController().getCurrentClipName());
        assertEquals("death", stateDeath.getAnimController().getCurrentClipName());

        // 2. Verify playback times are completely independent
        assertEquals(40 * times[0], stateIdle.getAnimController().getCurrentTime(), 1e-4f);
        assertEquals(40 * times[1], stateCharge.getAnimController().getCurrentTime(), 1e-4f);
        assertEquals(40 * times[2], stateTrans.getAnimController().getCurrentTime(), 1e-4f);
        assertEquals(40 * times[3], stateBite.getAnimController().getCurrentTime(), 1e-4f);
        assertEquals(40 * times[4], stateDeath.getAnimController().getCurrentTime(), 1e-4f);

        // 3. Verify skeletons hold distinct matrices
        Bone jawIdle = stateIdle.getSkeleton().getBone("upper_jaw");
        Bone jawBite = stateBite.getSkeleton().getBone("upper_jaw");
        Bone jawCharge = stateCharge.getSkeleton().getBone("upper_jaw");

        assertNotEquals(jawIdle.animRot.x, jawBite.animRot.x, "Idle and bite jaws must have distinct rotations");
        assertNotEquals(jawIdle.animRot.x, jawCharge.animRot.x, "Idle and charge jaws must have distinct rotations");

        // 4. Verify skinned mesh instances compute independent vertex buffers
        TerraSkinnedMeshInstance instIdle = stateIdle.getOrCreateInstanceP2(meshData);
        TerraSkinnedMeshInstance instBite = stateBite.getOrCreateInstanceP2(meshData);

        instIdle.skin(stateIdle.getSkeleton());
        instBite.skin(stateBite.getSkeleton());

        assertNotSame(instIdle, instBite, "Instances must be separate objects per state");

        float[] posIdle = instIdle.getParts().get(0).skinnedPositions;
        float[] posBite = instBite.getParts().get(0).skinnedPositions;

        boolean foundVertexDifference = false;
        for (int i = 0; i < posIdle.length; i++) {
            if (Math.abs(posIdle[i] - posBite[i]) > 0.05f) {
                foundVertexDifference = true;
                break;
            }
        }
        assertTrue(foundVertexDifference, "Deformed vertex positions must differ between idle and bite states");

        // 5. Verify cleanup removes state without leak
        EyeRenderStateManager.remove(idIdle);
        assertEquals(4, EyeRenderStateManager.getActiveCount());
        assertNull(EyeRenderStateManager.get(idIdle));

        EyeRenderStateManager.clear();
        assertEquals(0, EyeRenderStateManager.getActiveCount());
    }
}
