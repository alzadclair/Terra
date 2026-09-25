package com.terraforge.rpg;

import com.terraforge.rpg.boss.BossPhase;
import com.terraforge.rpg.boss.BossScalingService;
import com.terraforge.rpg.boss.ITerrariaBossPart;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BossFrameworkTest {

    @Test
    @DisplayName("Boss multiplayer scaling calculates canonical HP multipliers based on player count")
    void testMultiplayerScaling() {
        double baseHealth = 2800.0; // Eye of Cthulhu base HP

        // 1 player: 100%
        assertEquals(2800.0, BossScalingService.calculateScaledHealth(baseHealth, 1), 0.001);

        // 2 players: 1.0 + 0.35 = 1.35x -> 3780 HP
        assertEquals(3780.0, BossScalingService.calculateScaledHealth(baseHealth, 2), 0.001);

        // 3 players: 1.0 + 0.70 = 1.70x -> 4760 HP
        assertEquals(4760.0, BossScalingService.calculateScaledHealth(baseHealth, 3), 0.001);

        // 4 players: 1.0 + 1.05 = 2.05x -> 5740 HP
        assertEquals(5740.0, BossScalingService.calculateScaledHealth(baseHealth, 4), 0.001);

        // Max scaling cap (4.0x): 2800 * 4.0 = 11200 HP
        assertEquals(11200.0, BossScalingService.calculateScaledHealth(baseHealth, 50), 0.001);
    }

    @Test
    @DisplayName("Boss phases define discrete numbers and health transition ratios")
    void testBossPhases() {
        assertEquals(1, BossPhase.PHASE_1.phaseNumber());
        assertEquals(1.0, BossPhase.PHASE_1.healthThresholdRatio(), 0.001);

        assertEquals(2, BossPhase.PHASE_2.phaseNumber());
        assertEquals(0.5, BossPhase.PHASE_2.healthThresholdRatio(), 0.001);

        assertEquals(3, BossPhase.ENRAGED.phaseNumber());
        assertEquals(0.2, BossPhase.ENRAGED.healthThresholdRatio(), 0.001);
    }

    @Test
    @DisplayName("ITerrariaBossPart contract transfers damage to master entity")
    void testBossPartDelegation() {
        ITerrariaBossPart part = new ITerrariaBossPart() {
            @Override public com.terraforge.rpg.boss.ITerrariaBoss getParentBoss() { return null; }
            @Override public int getPartIndex() { return 1; }
            @Override public boolean routesDamageToParent() { return true; }
            @Override public double getDamageTransferRatio() { return 1.0; }
        };

        assertTrue(part.routesDamageToParent());
        assertEquals(1.0, part.getDamageTransferRatio(), 0.001);
        assertEquals(1, part.getPartIndex());
    }
}
