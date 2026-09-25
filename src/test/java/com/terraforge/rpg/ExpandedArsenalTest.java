package com.terraforge.rpg;

import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.item.prefix.PrefixRegistry;
import com.terraforge.rpg.item.prefix.TerrariaPrefix;
import com.terraforge.rpg.item.prefix.TerrariaPrefixCategory;
import com.terraforge.rpg.item.rarity.TerrariaRarity;
import com.terraforge.rpg.item.weapon.ITerrariaWeapon;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Fase 22: Expanded Arsenal (Beam swords, firearms, spellbooks, and accessories).
 */
public class ExpandedArsenalTest {

    static class MockWeapon implements ITerrariaWeapon {
        private final DamageClass damageClass;
        private final double damage;
        private final double crit;
        private final double kb;
        private final int useTime;
        private final double mana;
        private final TerrariaRarity rarity;
        private final long value;
        private final TerrariaPrefixCategory category;

        MockWeapon(DamageClass damageClass, double damage, double crit, double kb, int useTime, double mana, TerrariaRarity rarity, long value, TerrariaPrefixCategory category) {
            this.damageClass = damageClass;
            this.damage = damage;
            this.crit = crit;
            this.kb = kb;
            this.useTime = useTime;
            this.mana = mana;
            this.rarity = rarity;
            this.value = value;
            this.category = category;
        }

        @Override public DamageClass getDamageClass() { return damageClass; }
        @Override public double getTerrariaBaseDamage() { return damage; }
        @Override public double getBaseCritChance() { return crit; }
        @Override public double getKnockback() { return kb; }
        @Override public int getUseTime() { return useTime; }
        @Override public double getManaCost() { return mana; }
        @Override public TerrariaRarity getBaseRarity() { return rarity; }
        @Override public long getBaseValue() { return value; }
        @Override public TerrariaPrefixCategory getPrefixCategory() { return category; }
    }

    @Test
    @DisplayName("Nights Edge base stats & legendary prefix scaling")
    void testNightsEdgeCanonicalStats() {
        MockWeapon nightsEdge = new MockWeapon(DamageClass.MELEE, 40.0, 4.0, 4.5, 21, 0.0, TerrariaRarity.ORANGE, 54_000L, TerrariaPrefixCategory.MELEE);
        assertEquals(40.0, nightsEdge.getTerrariaBaseDamage());
        assertEquals(4.0, nightsEdge.getBaseCritChance());
        assertEquals(DamageClass.MELEE, nightsEdge.getDamageClass());

        TerrariaPrefix legendary = PrefixRegistry.get("legendary").orElseThrow();
        assertEquals(46.0, nightsEdge.calculateEffectiveDamage(legendary), 0.001);
        assertEquals(9.0, nightsEdge.calculateEffectiveCritChance(legendary), 0.001);
    }

    @Test
    @DisplayName("Terra Blade base stats and Unreal/Godly scaling")
    void testTerraBladeCanonicalStats() {
        MockWeapon terraBlade = new MockWeapon(DamageClass.MELEE, 115.0, 4.0, 6.5, 14, 0.0, TerrariaRarity.YELLOW, 1_000_000L, TerrariaPrefixCategory.MELEE);
        assertEquals(115.0, terraBlade.getTerrariaBaseDamage());
        assertEquals(6.5, terraBlade.getKnockback());
        assertEquals(14, terraBlade.getUseTime());

        TerrariaPrefix godly = PrefixRegistry.get("godly").orElseThrow();
        assertEquals(132.25, terraBlade.calculateEffectiveDamage(godly), 0.001);
        assertEquals(9.0, terraBlade.calculateEffectiveCritChance(godly), 0.001);
    }

    @Test
    @DisplayName("Megashark & Minishark canonical stats & ammo conservation")
    void testFirearmsStatsAndConservation() {
        MockWeapon minishark = new MockWeapon(DamageClass.RANGED, 6.0, 4.0, 0.0, 8, 0.0, TerrariaRarity.GREEN, 350_000L, TerrariaPrefixCategory.RANGED);
        MockWeapon megashark = new MockWeapon(DamageClass.RANGED, 25.0, 4.0, 1.0, 6, 0.0, TerrariaRarity.PINK, 600_000L, TerrariaPrefixCategory.RANGED);

        assertEquals(6.0, minishark.getTerrariaBaseDamage());
        assertEquals(8, minishark.getUseTime());
        assertEquals(25.0, megashark.getTerrariaBaseDamage());
        assertEquals(6, megashark.getUseTime());

        double minisharkConservation = 0.33;
        double megasharkConservation = 0.50;
        assertTrue(minisharkConservation > 0.30 && minisharkConservation < 0.35);
        assertEquals(0.50, megasharkConservation);
    }

    @Test
    @DisplayName("Water Bolt & Space Gun canonical spell statistics")
    void testMagicSpellsCanonicalStats() {
        MockWeapon waterBolt = new MockWeapon(DamageClass.MAGIC, 19.0, 4.0, 5.0, 17, 10.0, TerrariaRarity.GREEN, 50_000L, TerrariaPrefixCategory.MAGIC);
        MockWeapon spaceGun = new MockWeapon(DamageClass.MAGIC, 17.0, 4.0, 0.75, 17, 6.0, TerrariaRarity.GREEN, 40_000L, TerrariaPrefixCategory.MAGIC);

        assertEquals(19.0, waterBolt.getTerrariaBaseDamage());
        assertEquals(10.0, waterBolt.getManaCost());
        assertEquals(17.0, spaceGun.getTerrariaBaseDamage());
        assertEquals(6.0, spaceGun.getManaCost());

        TerrariaPrefix mythical = PrefixRegistry.get("mythical").orElseThrow();
        assertEquals(19.0 * 1.15, waterBolt.calculateEffectiveDamage(mythical), 0.001);
        assertEquals(10.0 * 0.90, waterBolt.calculateEffectiveManaCost(mythical), 0.001);
        assertEquals(6.0 * 0.90, spaceGun.calculateEffectiveManaCost(mythical), 0.001);
    }
}
