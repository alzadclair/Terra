package com.terraforge.rpg;

import com.terraforge.rpg.boss.BossPhase;
import com.terraforge.rpg.boss.BossScalingService;
import com.terraforge.rpg.boss.prehardmode.TheHungryEntity;
import com.terraforge.rpg.boss.prehardmode.WallOfFleshEntity;
import com.terraforge.rpg.item.weapon.PwnhammerItem;
import com.terraforge.rpg.world.progression.WorldProgressionData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HardmodeProgressionTest {

    @Test
    @DisplayName("Wall of Flesh canonical stats and phase thresholds")
    void testWallOfFleshStats() {
        assertEquals(8000.0, WallOfFleshEntity.BASE_HEALTH, 0.001);
        assertEquals(12, WallOfFleshEntity.BASE_DEFENSE);
        assertEquals(50.0, WallOfFleshEntity.BASE_ATTACK_DAMAGE, 0.001);

        // Phase 2 threshold at 50% HP
        double phase2Threshold = WallOfFleshEntity.BASE_HEALTH * BossPhase.PHASE_2.healthThresholdRatio();
        assertEquals(4000.0, phase2Threshold, 0.001);

        // Enraged threshold at 20% HP
        double enragedThreshold = WallOfFleshEntity.BASE_HEALTH * 0.20;
        assertEquals(1600.0, enragedThreshold, 0.001);
    }

    @Test
    @DisplayName("Wall of Flesh multiplayer health scaling")
    void testMultiplayerScaling() {
        assertEquals(8000.0, BossScalingService.calculateScaledHealth(WallOfFleshEntity.BASE_HEALTH, 1), 0.001);
        // 2 players: 8000 * (1 + 0.35) = 10800
        assertEquals(10800.0, BossScalingService.calculateScaledHealth(WallOfFleshEntity.BASE_HEALTH, 2), 0.001);
        // 4 players: 8000 * (1 + 3 * 0.35) = 8000 * 2.05 = 16400
        assertEquals(16400.0, BossScalingService.calculateScaledHealth(WallOfFleshEntity.BASE_HEALTH, 4), 0.001);
    }

    @Test
    @DisplayName("The Hungry minion canonical stats")
    void testTheHungryStats() {
        assertEquals(240.0, TheHungryEntity.BASE_HEALTH, 0.001);
        assertEquals(10, TheHungryEntity.DEFENSE);
        assertEquals(30.0, TheHungryEntity.ATTACK_DAMAGE, 0.001);
    }

    @Test
    @DisplayName("Pwnhammer 80% hammer power")
    void testPwnhammerProperties() {
        assertEquals(80, PwnhammerItem.HAMMER_POWER);
    }

    @Test
    @DisplayName("Hardmode world progression and altar smash cycle")
    void testAltarSmashCycle() {
        WorldProgressionData data = new WorldProgressionData();
        assertFalse(data.isHardmode());

        // Attempting to smash altar before Hardmode fails
        assertNull(data.smashAltar(null, null));
        assertEquals(0, data.getAltarsSmashed());

        // Activate Hardmode
        data.setHardmode(true);
        assertTrue(data.isHardmode());

        // 1st altar -> Cobalt
        String ore1 = data.smashAltar(null, null);
        assertEquals("Cobalt", ore1);
        assertEquals(1, data.getAltarsSmashed());

        // 2nd altar -> Mythril
        String ore2 = data.smashAltar(null, null);
        assertEquals("Mythril", ore2);
        assertEquals(2, data.getAltarsSmashed());

        // 3rd altar -> Titanium
        String ore3 = data.smashAltar(null, null);
        assertEquals("Titanium", ore3);
        assertEquals(3, data.getAltarsSmashed());

        // 4th altar -> cycle restarts at Cobalt
        String ore4 = data.smashAltar(null, null);
        assertEquals("Cobalt", ore4);
        assertEquals(4, data.getAltarsSmashed());
    }
}
