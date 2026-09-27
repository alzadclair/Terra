package com.terraforge.rpg;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.terraforge.rpg.registry.ModSoundEvents;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class EyeSoundRegistryTest {

    private static final Path SOUNDS_DIR = Path.of("src/main/resources/assets/terraforge_rpg/sounds/boss/eye_of_cthulhu");
    private static final Path SOUNDS_JSON = Path.of("src/main/resources/assets/terraforge_rpg/sounds.json");

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

    private static final String[] EYE_SOUNDS = {
            "ambient",
            "roar",
            "prepare",
            "charge",
            "bite",
            "hurt",
            "transition",
            "phase2_roar",
            "death",
            "servant_summon"
    };

    @Test
    @DisplayName("Verify all 10 Eye of Cthulhu custom .ogg audio files exist on disk with non-trivial size")
    void testAudioFilesExist() {
        for (String soundName : EYE_SOUNDS) {
            Path oggPath = SOUNDS_DIR.resolve(soundName + ".ogg");
            assertTrue(Files.isRegularFile(oggPath), "Sound file must exist: " + oggPath);
            try {
                long size = Files.size(oggPath);
                assertTrue(size > 1024, "Sound file " + oggPath + " must be > 1KB (found " + size + " bytes)");
            } catch (Exception e) {
                fail("Failed to check size for " + oggPath + ": " + e.getMessage());
            }
        }
    }

    @Test
    @DisplayName("Verify all 10 sound events are defined in sounds.json pointing to valid paths")
    void testSoundsJsonMapping() throws Exception {
        assertTrue(Files.isRegularFile(SOUNDS_JSON), "sounds.json must exist");
        try (FileReader reader = new FileReader(SOUNDS_JSON.toFile())) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            for (String soundName : EYE_SOUNDS) {
                String eventKey = "eye_of_cthulhu." + soundName;
                assertTrue(root.has(eventKey), "sounds.json must contain key: " + eventKey);
                JsonObject eventObj = root.getAsJsonObject(eventKey);
                assertTrue(eventObj.has("sounds"), eventKey + " must define sounds array");
                String expectedSoundPath = "terraforge_rpg:boss/eye_of_cthulhu/" + soundName;
                assertEquals(expectedSoundPath, eventObj.getAsJsonArray("sounds").get(0).getAsString());
            }
        }
    }

    @Test
    @DisplayName("Verify ModSoundEvents defines all DeferredHolders for Eye of Cthulhu")
    void testDeferredHoldersDefined() {
        assertNotNull(ModSoundEvents.EYE_AMBIENT);
        assertNotNull(ModSoundEvents.EYE_ROAR);
        assertNotNull(ModSoundEvents.EYE_PREPARE);
        assertNotNull(ModSoundEvents.EYE_CHARGE);
        assertNotNull(ModSoundEvents.EYE_BITE);
        assertNotNull(ModSoundEvents.EYE_HURT);
        assertNotNull(ModSoundEvents.EYE_TRANSITION);
        assertNotNull(ModSoundEvents.EYE_PHASE2_ROAR);
        assertNotNull(ModSoundEvents.EYE_DEATH);
        assertNotNull(ModSoundEvents.EYE_SERVANT_SUMMON);
    }
}
