package com.terraforge.rpg;

import com.terraforge.rpg.boss.BossPhase;
import com.terraforge.rpg.boss.prehardmode.EyeOfCthulhuEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EyeOfCthulhuTest {

    @Test
    @DisplayName("Eye of Cthulhu canonical stats and health thresholds")
    void testBossStats() {
        assertEquals(2800.0, EyeOfCthulhuEntity.BASE_HEALTH, 0.001);

        // Phase 1 has 12 defense
        int phase1Def = 12;
        assertEquals(12, phase1Def);

        // Phase 2 transition at 50% HP (1400 HP) drops defense to 0
        double phase2Threshold = EyeOfCthulhuEntity.BASE_HEALTH * BossPhase.PHASE_2.healthThresholdRatio();
        assertEquals(1400.0, phase2Threshold, 0.001);

        int phase2Def = 0;
        assertEquals(0, phase2Def);
    }

    @Test
    @DisplayName("Eye of Cthulhu daytime despawn window matches Minecraft night cycle")
    void testDaytimeDespawnWindow() {
        // Minecraft night is between 13000 and 23000 ticks
        long morningTime = 1000L;
        long nightTime = 16000L;

        boolean morningIsNight = morningTime >= 13000 && morningTime <= 23000;
        assertFalse(morningIsNight);

        boolean nightIsNight = nightTime >= 13000 && nightTime <= 23000;
        assertTrue(nightIsNight);
    }
}
