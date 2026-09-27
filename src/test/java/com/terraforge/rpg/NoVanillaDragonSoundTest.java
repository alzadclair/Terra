package com.terraforge.rpg;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NoVanillaDragonSoundTest {

    @Test
    @DisplayName("Verify EyeOfCthulhuEntity has zero references to Ender Dragon, Phantom, or Slime sounds")
    void testNoVanillaDragonOrPhantomSoundsInEye() throws Exception {
        Path entityPath = Path.of("src/main/java/com/terraforge/rpg/boss/prehardmode/EyeOfCthulhuEntity.java");
        assertTrue(Files.isRegularFile(entityPath), "EyeOfCthulhuEntity source file must exist");

        String content = Files.readString(entityPath);

        assertFalse(content.contains("ENDER_DRAGON"), "EyeOfCthulhuEntity must not use ENDER_DRAGON sounds");
        assertFalse(content.contains("PHANTOM"), "EyeOfCthulhuEntity must not use PHANTOM sounds");
        assertFalse(content.contains("SLIME_BLOCK_BREAK"), "EyeOfCthulhuEntity must not use SLIME_BLOCK_BREAK sound");
        assertFalse(content.contains("SLIME_SQUISH"), "EyeOfCthulhuEntity must not use SLIME_SQUISH sound");
        assertFalse(content.contains("net.minecraft.sounds.SoundEvents"), "EyeOfCthulhuEntity must not import vanilla SoundEvents");
    }
}
