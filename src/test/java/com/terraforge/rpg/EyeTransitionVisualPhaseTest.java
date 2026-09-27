package com.terraforge.rpg;

import com.terraforge.rpg.boss.BossPhase;
import com.terraforge.rpg.boss.prehardmode.EyeOfCthulhuEntity;
import com.terraforge.rpg.boss.prehardmode.EyeOfCthulhuEntity.EyeAnimState;
import com.terraforge.rpg.boss.prehardmode.EyeOfCthulhuEntity.EyeVisualPhase;
import com.terraforge.rpg.client.animation.skeletal.Skeleton;
import com.terraforge.rpg.client.render.entity.state.EyeRenderState;
import com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshData;
import com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshInstance;
import com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshLoader;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.Entity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class EyeTransitionVisualPhaseTest {

    private static final Path MODELS_DIR = Path.of("src/main/resources/assets/terraforge_rpg/models/entity/boss");
    private TerraSkinnedMeshData meshDataP1;
    private TerraSkinnedMeshData meshDataP2;

    @BeforeAll
    static void initBootstrap() {
        try {
            Method ofMethod = Class.forName("net.neoforged.fml.loading.LoadingModList")
                    .getMethod("of", java.util.List.class, java.util.List.class, java.util.List.class, java.util.List.class, java.util.Map.class);
            ofMethod.invoke(null, java.util.List.of(), java.util.List.of(), java.util.List.of(), java.util.List.of(), java.util.Map.of());
        } catch (Throwable ignored) {
        }
        try {
            SharedConstants.tryDetectVersion();
            Bootstrap.bootStrap();
        } catch (Throwable ignored) {
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        File fileP1 = MODELS_DIR.resolve("eye_of_cthulhu_p1.skin.json").toFile();
        File fileP2 = MODELS_DIR.resolve("eye_of_cthulhu_p2.skin.json").toFile();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileP1))) {
            meshDataP1 = TerraSkinnedMeshLoader.loadFromReader(reader, fileP1.getName());
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(fileP2))) {
            meshDataP2 = TerraSkinnedMeshLoader.loadFromReader(reader, fileP2.getName());
        }

        assertNotNull(meshDataP1);
        assertNotNull(meshDataP2);
    }

    @SuppressWarnings("unchecked")
    private EyeOfCthulhuEntity createTestEye() {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            Unsafe unsafe = (Unsafe) f.get(null);
            EyeOfCthulhuEntity eye = (EyeOfCthulhuEntity) unsafe.allocateInstance(EyeOfCthulhuEntity.class);

            Field phaseField = com.terraforge.rpg.boss.TerraBaseBoss.class.getDeclaredField("DATA_PHASE_NUMBER");
            phaseField.setAccessible(true);
            net.minecraft.network.syncher.EntityDataAccessor<Integer> dataPhase =
                    (net.minecraft.network.syncher.EntityDataAccessor<Integer>) phaseField.get(null);

            Field animField = EyeOfCthulhuEntity.class.getDeclaredField("DATA_ANIM_STATE");
            animField.setAccessible(true);
            net.minecraft.network.syncher.EntityDataAccessor<Integer> dataAnim =
                    (net.minecraft.network.syncher.EntityDataAccessor<Integer>) animField.get(null);

            Field visualField = EyeOfCthulhuEntity.class.getDeclaredField("DATA_VISUAL_PHASE");
            visualField.setAccessible(true);
            net.minecraft.network.syncher.EntityDataAccessor<Integer> dataVisual =
                    (net.minecraft.network.syncher.EntityDataAccessor<Integer>) visualField.get(null);

            int maxId = Math.max(dataPhase.id(), Math.max(dataAnim.id(), dataVisual.id())) + 10;
            SynchedEntityData.DataItem<?>[] items = new SynchedEntityData.DataItem<?>[maxId];

            items[dataPhase.id()] = new SynchedEntityData.DataItem<>(dataPhase, 1);
            items[dataAnim.id()] = new SynchedEntityData.DataItem<>(dataAnim, EyeAnimState.IDLE.ordinal());
            items[dataVisual.id()] = new SynchedEntityData.DataItem<>(dataVisual, EyeVisualPhase.PHASE_1.ordinal());

            java.lang.reflect.Constructor<SynchedEntityData> ctor = SynchedEntityData.class.getDeclaredConstructor(
                    Class.forName("net.minecraft.network.syncher.SyncedDataHolder"),
                    items.getClass()
            );
            ctor.setAccessible(true);
            SynchedEntityData synchedData = ctor.newInstance(eye, items);

            Field dataField = Entity.class.getDeclaredField("entityData");
            dataField.setAccessible(true);
            dataField.set(eye, synchedData);

            return eye;
        } catch (Exception e) {
            throw new RuntimeException("Failed to construct test EyeOfCthulhuEntity", e);
        }
    }

    @Test
    @DisplayName("Transition Start: gameplay phase = 2, isTransitioning = true, visualPhase = TRANSITIONING_P1, isRenderPhase2 = false")
    void testTransitionStart() {
        EyeOfCthulhuEntity eye = createTestEye();

        assertEquals(BossPhase.PHASE_1, eye.getCurrentPhase());
        assertEquals(EyeVisualPhase.PHASE_1, eye.getVisualPhase());
        assertFalse(eye.isRenderPhase2());
        assertFalse(eye.isTransitioning());

        // Trigger phase transition
        eye.setPhase(BossPhase.PHASE_2);

        // 1. Gameplay phase is 2
        assertEquals(BossPhase.PHASE_2, eye.getCurrentPhase(), "Gameplay phase must be 2");
        assertEquals(2, eye.getCurrentPhase().phaseNumber());

        // 2. Transition state is active
        assertTrue(eye.isTransitioning(), "isTransitioning must be true");
        assertEquals(EyeOfCthulhuEntity.TRANSITION_DURATION_TICKS, eye.getTransitionTicks());
        assertEquals(EyeAnimState.TRANSITIONING, eye.getAnimState());

        // 3. Visual phase remains P1 (TRANSITIONING_P1) — NO immediate pop to P2!
        assertEquals(EyeVisualPhase.TRANSITIONING_P1, eye.getVisualPhase(), "Visual phase must be TRANSITIONING_P1");
        assertFalse(eye.isRenderPhase2(), "isRenderPhase2() must be false at transition start");
    }

    @Test
    @DisplayName("Pre-Swap Ticks (elapsed 1..39): visual strictly uses P1 (isRenderPhase2 == false)")
    void testPreSwapVisualPhase() {
        EyeOfCthulhuEntity eye = createTestEye();
        eye.setPhase(BossPhase.PHASE_2);

        // Tick through pre-swap window: ticks 1 to 39
        for (int elapsed = 1; elapsed < EyeOfCthulhuEntity.TRANSITION_MESH_SWAP_TICK; elapsed++) {
            eye.tickTransition();
            assertTrue(eye.isTransitioning(), "Must still be transitioning at elapsed " + elapsed);
            assertEquals(EyeVisualPhase.TRANSITIONING_P1, eye.getVisualPhase(),
                    "Visual phase must stay TRANSITIONING_P1 at elapsed " + elapsed);
            assertFalse(eye.isRenderPhase2(),
                    "Renderer helper isRenderPhase2() must return false at elapsed " + elapsed);
        }

        // At elapsed = 39 (tick 39), exactly 1 tick before swap:
        assertEquals(EyeOfCthulhuEntity.TRANSITION_DURATION_TICKS - 39, eye.getTransitionTicks());
        assertEquals(EyeVisualPhase.TRANSITIONING_P1, eye.getVisualPhase());
        assertFalse(eye.isRenderPhase2());
    }

    @Test
    @DisplayName("At Swap Tick (elapsed 40): visualPhase switches to PHASE_2 (isRenderPhase2 == true)")
    void testMeshSwapTick() {
        EyeOfCthulhuEntity eye = createTestEye();
        eye.setPhase(BossPhase.PHASE_2);

        // Advance to tick 40
        for (int elapsed = 1; elapsed <= EyeOfCthulhuEntity.TRANSITION_MESH_SWAP_TICK; elapsed++) {
            eye.tickTransition();
        }

        // At swap tick (elapsed = 40):
        assertEquals(EyeVisualPhase.PHASE_2, eye.getVisualPhase(), "Visual phase must swap to PHASE_2 at swap tick");
        assertTrue(eye.isRenderPhase2(), "isRenderPhase2() must return true at swap tick");
        assertTrue(eye.isTransitioning(), "Still transitioning in P2 convulsion pose");
        assertEquals(EyeOfCthulhuEntity.TRANSITION_DURATION_TICKS - 40, eye.getTransitionTicks());
    }

    @Test
    @DisplayName("Post-Swap Ticks (elapsed 41..59): visual remains PHASE_2 with correct renderer pairing")
    void testPostSwapVisualPhaseAndRendererPairing() {
        EyeOfCthulhuEntity eye = createTestEye();
        eye.setPhase(BossPhase.PHASE_2);

        // Advance to elapsed 45
        for (int elapsed = 1; elapsed <= 45; elapsed++) {
            eye.tickTransition();
        }

        assertTrue(eye.isRenderPhase2(), "Post-swap must be renderPhase2");
        assertEquals(EyeVisualPhase.PHASE_2, eye.getVisualPhase());

        // Validate renderer selection with EyeRenderState
        EyeRenderState state = EyeRenderState.create(UUID.randomUUID(), meshDataP1, meshDataP2);
        boolean isP2 = eye.isRenderPhase2();

        Skeleton activeSkeleton = state.getSkeleton(isP2);
        TerraSkinnedMeshInstance activeInstance = state.getMeshInstance(isP2, meshDataP2);

        assertSame(state.getSkeletonP2(), activeSkeleton, "Must select P2 skeleton");
        assertSame(state.getOrCreateInstanceP2(meshDataP2), activeInstance, "Must select P2 instance");
    }

    @Test
    @DisplayName("Transition Complete (elapsed 60): isTransitioning becomes false, animState becomes PHASE2_IDLE")
    void testTransitionEnd() {
        EyeOfCthulhuEntity eye = createTestEye();
        eye.setPhase(BossPhase.PHASE_2);

        // Advance all 60 ticks
        for (int elapsed = 1; elapsed <= EyeOfCthulhuEntity.TRANSITION_DURATION_TICKS; elapsed++) {
            eye.tickTransition();
        }

        assertFalse(eye.isTransitioning(), "Transition must be finished after 60 ticks");
        assertEquals(EyeAnimState.PHASE2_IDLE, eye.getAnimState(), "Anim state must be PHASE2_IDLE");
        assertEquals(EyeVisualPhase.PHASE_2, eye.getVisualPhase(), "Visual phase must be PHASE_2");
        assertTrue(eye.isRenderPhase2());
    }

    @Test
    @DisplayName("Normal Phases without transition: Phase 1 is P1 visual, Phase 2 is P2 visual")
    void testNormalPhasesWithoutTransition() {
        EyeOfCthulhuEntity eye = createTestEye();

        // Normal Phase 1
        assertEquals(EyeVisualPhase.PHASE_1, eye.getVisualPhase());
        assertFalse(eye.isRenderPhase2());

        // Directly set Phase 2 visual
        eye.setVisualPhase(EyeVisualPhase.PHASE_2);
        assertEquals(EyeVisualPhase.PHASE_2, eye.getVisualPhase());
        assertTrue(eye.isRenderPhase2());
    }

    @Test
    @DisplayName("Reload Logic: Phase 2 Eye not transitioning normalizes visualPhase to PHASE_2")
    void testReloadNormalizationLogic() {
        EyeOfCthulhuEntity eye = createTestEye();

        // Simulate save/load in Phase 2
        eye.setPhase(BossPhase.PHASE_2);
        // Complete transition
        for (int i = 0; i < 60; i++) eye.tickTransition();

        // Test NBT serialization
        CompoundTag tag = new CompoundTag();
        eye.addAdditionalSaveData(tag);
        assertEquals("PHASE_2", tag.getString("VisualPhase"));
        assertFalse(tag.getBoolean("IsTransitioning"));

        // Simulate new entity loading NBT
        EyeOfCthulhuEntity loadedEye = createTestEye();
        loadedEye.readAdditionalSaveData(tag);
        assertEquals(EyeVisualPhase.PHASE_2, loadedEye.getVisualPhase());
        assertTrue(loadedEye.isRenderPhase2());

        // Simulate loaded in Phase 2 without NBT tag (legacy chunk reload where not transitioning)
        EyeOfCthulhuEntity legacyEye = createTestEye();
        legacyEye.setPhase(BossPhase.PHASE_2);
        for (int i = 0; i < EyeOfCthulhuEntity.TRANSITION_DURATION_TICKS; i++) {
            legacyEye.tickTransition();
        }
        assertFalse(legacyEye.isTransitioning(), "Legacy eye must not be transitioning");

        // Force visual phase back to PHASE_1 while not transitioning to simulate un-normalized legacy state
        legacyEye.setVisualPhase(EyeVisualPhase.PHASE_1);
        assertEquals(EyeVisualPhase.PHASE_1, legacyEye.getVisualPhase());

        legacyEye.normalizeVisualPhase();
        assertEquals(EyeVisualPhase.PHASE_2, legacyEye.getVisualPhase());
        assertTrue(legacyEye.isRenderPhase2());
    }
}
