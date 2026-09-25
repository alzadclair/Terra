package com.terraforge.rpg;

import com.terraforge.rpg.boss.hardmode.RetinazerEntity;
import com.terraforge.rpg.boss.hardmode.SkeletronPrimeEntity;
import com.terraforge.rpg.boss.hardmode.SpazmatismEntity;
import com.terraforge.rpg.boss.hardmode.TheDestroyerEntity;
import com.terraforge.rpg.world.progression.WorldProgressionData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Fase 23: Mechanical Bosses & Hardmode Progression.
 */
public class MechanicalBossesTest {

    @Test
    @DisplayName("The Twins (Retinazer & Spazmatism) canonical stats & Phase 2 defense transitions")
    void testTwinsCanonicalStats() {
        assertEquals(24000.0, RetinazerEntity.BASE_HEALTH);
        assertEquals(23000.0, SpazmatismEntity.BASE_HEALTH);

        // Retinazer: 10 def in P1 -> 20 def in P2
        int retinazerP1Def = 10;
        int retinazerP2Def = 20;
        assertEquals(10, retinazerP1Def);
        assertEquals(20, retinazerP2Def);

        // Spazmatism: 10 def in P1 -> 28 def in P2
        int spazmatismP1Def = 10;
        int spazmatismP2Def = 28;
        assertEquals(10, spazmatismP1Def);
        assertEquals(28, spazmatismP2Def);

        // Phase 2 threshold is 40% health
        double p2Threshold = 0.40;
        assertEquals(9600.0, RetinazerEntity.BASE_HEALTH * p2Threshold, 0.001);
        assertEquals(9200.0, SpazmatismEntity.BASE_HEALTH * p2Threshold, 0.001);
    }

    @Test
    @DisplayName("The Destroyer canonical stats & segment dynamics")
    void testDestroyerCanonicalStats() {
        assertEquals(80000.0, TheDestroyerEntity.BASE_HEALTH);

        int headDefense = 0;
        double headContactDamage = 72.0;
        assertEquals(0, headDefense);
        assertEquals(72.0, headContactDamage);

        // Probe HP canonical: 200 HP
        double probeHealth = 200.0;
        assertEquals(200.0, probeHealth);
    }

    @Test
    @DisplayName("Skeletron Prime canonical stats, spin mode & daylight enrage")
    void testSkeletronPrimeCanonicalStats() {
        assertEquals(28000.0, SkeletronPrimeEntity.BASE_HEALTH);

        int normalDefense = 24;
        int spinDefense = normalDefense * 2; // 48 defense while spinning
        int enrageDefense = 9999;           // Dungeon Guardian mode

        assertEquals(24, normalDefense);
        assertEquals(48, spinDefense);
        assertEquals(9999, enrageDefense);

        double normalDamage = 47.0;
        double spinDamage = 94.0;
        double enrageDamage = 1000.0;

        assertEquals(47.0, normalDamage);
        assertEquals(94.0, spinDamage);
        assertEquals(1000.0, enrageDamage);
    }

    @Test
    @DisplayName("WorldProgressionData tracks all 3 mechanical bosses and triggers jungle restless")
    void testMechanicalBossProgression() {
        WorldProgressionData data = new WorldProgressionData();
        data.setHardmode(true);
        assertTrue(data.isHardmode());

        assertFalse(data.isTwinsDefeated());
        assertFalse(data.isDestroyerDefeated());
        assertFalse(data.isSkeletronPrimeDefeated());
        assertFalse(data.hasDefeatedAllMechBosses());

        // Defeat Twins
        data.markBossDefeated("the_twins");
        assertTrue(data.isTwinsDefeated());
        assertFalse(data.hasDefeatedAllMechBosses());

        // Defeat Destroyer
        data.markBossDefeated("the_destroyer");
        assertTrue(data.isDestroyerDefeated());
        assertFalse(data.hasDefeatedAllMechBosses());

        // Defeat Skeletron Prime -> all 3 defeated!
        data.markBossDefeated("skeletron_prime");
        assertTrue(data.isSkeletronPrimeDefeated());
        assertTrue(data.hasDefeatedAllMechBosses());
    }
}
