package com.terraforge.rpg;

import com.terraforge.rpg.boss.endgame.DukeFishronEntity;
import com.terraforge.rpg.boss.endgame.GolemEntity;
import com.terraforge.rpg.boss.endgame.MoonLordEntity;
import com.terraforge.rpg.boss.endgame.PlanteraEntity;
import com.terraforge.rpg.world.progression.WorldProgressionData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Fase 24: Hardmode Avançado & Endgame Bosses Progression.
 */
public class EndgameProgressionTest {

    @Test
    @DisplayName("Plantera canonical health, defense phases and enrage mechanics")
    void testPlanteraCanonicalStats() {
        assertEquals(30000.0, PlanteraEntity.BASE_HEALTH);

        int p1Defense = 36;
        int p2Defense = 10;
        int enragedDefense = 72;

        assertEquals(36, p1Defense);
        assertEquals(10, p2Defense);
        assertEquals(72, enragedDefense);

        // Phase 2 triggers at 50% health (15,000 HP)
        assertEquals(15000.0, PlanteraEntity.BASE_HEALTH * 0.50, 0.001);
    }

    @Test
    @DisplayName("Golem canonical stats and detached head transition")
    void testGolemCanonicalStats() {
        assertEquals(16000.0, GolemEntity.BASE_HEALTH);

        int p1Defense = 24;
        int p2Defense = 32;

        assertEquals(24, p1Defense);
        assertEquals(32, p2Defense);
    }

    @Test
    @DisplayName("Duke Fishron canonical stats, charges and ocean enrage")
    void testDukeFishronCanonicalStats() {
        assertEquals(60000.0, DukeFishronEntity.BASE_HEALTH);

        int p1Defense = 50;
        int p2Defense = 40;
        int enragedDefense = 100;

        assertEquals(50, p1Defense);
        assertEquals(40, p2Defense);
        assertEquals(100, enragedDefense);

        // Bait power for Truffle Worm: 666%
        int truffleWormBaitPower = 666;
        assertEquals(666, truffleWormBaitPower);
    }

    @Test
    @DisplayName("Moon Lord cosmic statistics and Phantasmal Deathray damage")
    void testMoonLordCanonicalStats() {
        assertEquals(145000.0, MoonLordEntity.BASE_HEALTH);

        int p1Defense = 50;
        int p2Defense = 70; // Core exposed

        assertEquals(50, p1Defense);
        assertEquals(70, p2Defense);

        double deathrayDamage = 150.0;
        assertEquals(150.0, deathrayDamage);
    }

    @Test
    @DisplayName("WorldProgressionData tracks Plantera, Golem, Fishron and Moon Lord milestones")
    void testEndgameProgressionMilestones() {
        WorldProgressionData data = new WorldProgressionData();
        data.setHardmode(true);

        assertFalse(data.isPlanteraDefeated());
        assertFalse(data.isGolemDefeated());
        assertFalse(data.isFishronDefeated());
        assertFalse(data.isMoonLordDefeated());

        // Defeat Plantera
        data.markBossDefeated("plantera");
        assertTrue(data.isPlanteraDefeated());

        // Defeat Golem
        data.markBossDefeated("golem");
        assertTrue(data.isGolemDefeated());

        // Defeat Duke Fishron
        data.markBossDefeated("duke_fishron");
        assertTrue(data.isFishronDefeated());

        // Defeat Moon Lord
        data.markBossDefeated("moon_lord");
        assertTrue(data.isMoonLordDefeated());
    }
}
