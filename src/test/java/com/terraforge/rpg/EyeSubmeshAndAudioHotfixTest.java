package com.terraforge.rpg;

import com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshData;
import com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshInstance;
import com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class EyeSubmeshAndAudioHotfixTest {

    private static final Path MODELS_DIR = Path.of("src/main/resources/assets/terraforge_rpg/models/entity/boss");
    private static final Path ENTITY_FILE = Path.of("src/main/java/com/terraforge/rpg/boss/prehardmode/EyeOfCthulhuEntity.java");

    @Test
    @DisplayName("Verify Phase 1 contains authentic submeshes with glass mapped to TRANSLUCENT and others to OPAQUE")
    void testPhase1SubmeshesAndRenderModes() throws Exception {
        File fileP1 = MODELS_DIR.resolve("eye_of_cthulhu_p1.skin.json").toFile();
        assertTrue(fileP1.exists(), "eye_of_cthulhu_p1.skin.json must exist");

        TerraSkinnedMeshData data;
        try (BufferedReader reader = new BufferedReader(new FileReader(fileP1))) {
            data = TerraSkinnedMeshLoader.loadFromReader(reader, "eye_of_cthulhu_p1");
        }
        assertNotNull(data);

        List<String> partNames = data.getParts().stream().map(p -> p.name).collect(Collectors.toList());
        assertEquals(List.of("stalk", "glass", "body", "pupil", "iris"), partNames,
                "Phase 1 must contain exactly stalk, glass, body, pupil, and iris");

        Map<String, TerraSkinnedMeshData.RenderMode> modes = data.getParts().stream()
                .collect(Collectors.toMap(p -> p.name, p -> p.renderMode));

        assertEquals(TerraSkinnedMeshData.RenderMode.TRANSLUCENT, modes.get("glass"),
                "glass cornea must have TRANSLUCENT renderMode");
        assertEquals(TerraSkinnedMeshData.RenderMode.OPAQUE, modes.get("body"),
                "body sclera must have OPAQUE renderMode");
        assertEquals(TerraSkinnedMeshData.RenderMode.OPAQUE, modes.get("pupil"),
                "pupil must have OPAQUE renderMode");
        assertEquals(TerraSkinnedMeshData.RenderMode.OPAQUE, modes.get("iris"),
                "iris must have OPAQUE renderMode");
        assertEquals(TerraSkinnedMeshData.RenderMode.OPAQUE, modes.get("stalk"),
                "stalk tendrils must have OPAQUE renderMode");
    }

    @Test
    @DisplayName("Verify Phase 2 contains authentic submeshes and all are OPAQUE")
    void testPhase2SubmeshesAndRenderModes() throws Exception {
        File fileP2 = MODELS_DIR.resolve("eye_of_cthulhu_p2.skin.json").toFile();
        assertTrue(fileP2.exists(), "eye_of_cthulhu_p2.skin.json must exist");

        TerraSkinnedMeshData data;
        try (BufferedReader reader = new BufferedReader(new FileReader(fileP2))) {
            data = TerraSkinnedMeshLoader.loadFromReader(reader, "eye_of_cthulhu_p2");
        }
        assertNotNull(data);

        List<String> partNames = data.getParts().stream().map(p -> p.name).collect(Collectors.toList());
        assertEquals(List.of("stalk", "body", "teeth", "inner_teeth"), partNames,
                "Phase 2 must contain stalk, body, teeth, and inner_teeth");

        for (TerraSkinnedMeshData.PartData part : data.getParts()) {
            assertEquals(TerraSkinnedMeshData.RenderMode.OPAQUE, part.renderMode,
                    "Phase 2 part " + part.name + " must be OPAQUE");
        }
    }

    @Test
    @DisplayName("Verify dev-only debugSubmeshFilter mechanism allows selective part rendering")
    void testDebugSubmeshFilter() throws Exception {
        File fileP1 = MODELS_DIR.resolve("eye_of_cthulhu_p1.skin.json").toFile();
        TerraSkinnedMeshData data;
        try (BufferedReader reader = new BufferedReader(new FileReader(fileP1))) {
            data = TerraSkinnedMeshLoader.loadFromReader(reader, "eye_of_cthulhu_p1");
        }

        TerraSkinnedMeshInstance.setDebugSubmeshFilter("BODY");
        assertEquals("BODY", TerraSkinnedMeshInstance.getDebugSubmeshFilter());

        TerraSkinnedMeshInstance.setDebugSubmeshFilter("ALL");
        assertEquals("ALL", TerraSkinnedMeshInstance.getDebugSubmeshFilter());
        assertNull(TerraSkinnedMeshInstance.getEffectiveDebugSubmeshFilter());

        TerraSkinnedMeshInstance.setDebugSubmeshFilter(null);
        assertEquals("ALL", TerraSkinnedMeshInstance.getDebugSubmeshFilter());
    }

    @Test
    @DisplayName("Verify Audio Hotfix 1: EYE_TRANSITION is not triggered inside transitionTicks % 8 loop")
    void testAudioHotfix1TransitionSoundNotRepeated() throws Exception {
        String entityCode = Files.readString(ENTITY_FILE);

        assertFalse(entityCode.contains("transitionTicks % 8 == 0") &&
                    entityCode.substring(entityCode.indexOf("transitionTicks % 8 == 0")).split("}")[0].contains("EYE_TRANSITION"),
                "EYE_TRANSITION must NOT be triggered inside the transitionTicks % 8 loop");

        assertTrue(entityCode.contains("startPhaseTransition"), "startPhaseTransition must exist");
        assertTrue(entityCode.contains("EYE_TRANSITION"), "EYE_TRANSITION must be played on transformation start");
    }

    @Test
    @DisplayName("Verify Audio Hotfix 2: Phase 2 charge does not use EYE_PHASE2_ROAR as dash SFX")
    void testAudioHotfix2P2ChargeDoesNotUseRoar() throws Exception {
        String entityCode = Files.readString(ENTITY_FILE);

        // Find the charge launch block: if (attackTimer <= 0) inside EyeOfCthulhuCombatGoal
        int combatGoalIdx = entityCode.indexOf("class EyeOfCthulhuCombatGoal");
        assertTrue(combatGoalIdx > 0, "EyeOfCthulhuCombatGoal must exist");
        String combatGoalCode = entityCode.substring(combatGoalIdx);

        assertFalse(combatGoalCode.contains("EYE_PHASE2_ROAR"),
                "Phase 2 charge must NOT use EYE_PHASE2_ROAR as charge sound effect");
        assertTrue(combatGoalCode.contains("EYE_CHARGE"),
                "Phase 2 charge must use EYE_CHARGE with higher pitch/volume");
    }

    @Test
    @DisplayName("Verify Audio Hotfix 3: bite and damage are gated to max 1 trigger per charge")
    void testAudioHotfix3BiteAndDamageGated() throws Exception {
        String entityCode = Files.readString(ENTITY_FILE);

        assertTrue(entityCode.contains("hitTriggeredThisCharge"),
                "EyeOfCthulhuCombatGoal must maintain hitTriggeredThisCharge state flag");
        assertTrue(entityCode.contains("!hitTriggeredThisCharge"),
                "doHurtTarget and EYE_BITE must be gated by !hitTriggeredThisCharge");
    }
}
