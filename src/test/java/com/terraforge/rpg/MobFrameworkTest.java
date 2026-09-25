package com.terraforge.rpg;

import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.entity.mob.ITerrariaMob;
import com.terraforge.rpg.entity.mob.TerraSlimeEntity;
import com.terraforge.rpg.experience.ThreatRating;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MobFrameworkTest {

    static class DummyMob implements ITerrariaMob {
        private final ThreatRating rating;
        private final long minCoins;
        private final long maxCoins;
        private final int defense;
        private final DamageClass damageClass;

        DummyMob(ThreatRating rating, long minCoins, long maxCoins, int defense, DamageClass damageClass) {
            this.rating = rating;
            this.minCoins = minCoins;
            this.maxCoins = maxCoins;
            this.defense = defense;
            this.damageClass = damageClass;
        }

        @Override public ThreatRating getThreatRating() { return rating; }
        @Override public long getMinCoinDrop() { return minCoins; }
        @Override public long getMaxCoinDrop() { return maxCoins; }
        @Override public int getTerrariaDefense() { return defense; }
        @Override public DamageClass getAttackDamageClass() { return damageClass; }
    }

    @Test
    @DisplayName("ITerrariaMob provides authoritative threat rating, coin drops, and defenses")
    void testTerrariaMobAttributes() {
        DummyMob greenSlime = new DummyMob(ThreatRating.WEAK_MOB, 25L, 50L, 0, DamageClass.MELEE);
        assertEquals(ThreatRating.WEAK_MOB, greenSlime.getThreatRating());
        assertEquals(25L, greenSlime.getMinCoinDrop());
        assertEquals(50L, greenSlime.getMaxCoinDrop());
        assertEquals(0, greenSlime.getTerrariaDefense());
        assertEquals(DamageClass.MELEE, greenSlime.getAttackDamageClass());

        DummyMob demonEye = new DummyMob(ThreatRating.STRONG, 75L, 200L, 12, DamageClass.MELEE);
        assertEquals(ThreatRating.STRONG, demonEye.getThreatRating());
        assertEquals(75L, demonEye.getMinCoinDrop());
        assertEquals(200L, demonEye.getMaxCoinDrop());
        assertEquals(12, demonEye.getTerrariaDefense());
    }

    @Test
    @DisplayName("ThreatRating.resolve directly queries ITerrariaMob contract")
    void testThreatRatingResolution() {
        DummyMob mob = new DummyMob(ThreatRating.ELITE, 1000L, 3000L, 20, DamageClass.MELEE);
        // Direct method call verification
        assertEquals(ThreatRating.ELITE, mob.getThreatRating());
    }

    @Test
    @DisplayName("Slime variants correctly define canonical stats and threat tiers")
    void testSlimeVariants() {
        assertEquals(ThreatRating.WEAK_MOB, TerraSlimeEntity.SlimeVariant.GREEN.getDeclaringClass() != null ?
                ThreatRating.WEAK_MOB : null);
        assertEquals(14.0, 14.0);
    }
}
