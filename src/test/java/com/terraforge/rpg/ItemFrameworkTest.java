package com.terraforge.rpg;

import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.economy.CoinHelper;
import com.terraforge.rpg.item.prefix.PrefixRegistry;
import com.terraforge.rpg.item.prefix.TerrariaPrefix;
import com.terraforge.rpg.item.prefix.TerrariaPrefixCategory;
import com.terraforge.rpg.item.rarity.TerrariaRarity;
import com.terraforge.rpg.item.reforge.ReforgeService;
import com.terraforge.rpg.item.weapon.ITerrariaWeapon;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ItemFrameworkTest {

    static class TestWeapon implements ITerrariaWeapon {
        private final TerrariaRarity rarity;
        private final long baseValue;
        private final double damage;
        private final double crit;
        private final double kb;
        private final int useTime;

        TestWeapon(TerrariaRarity rarity, long baseValue, double damage, double crit, double kb, int useTime) {
            this.rarity = rarity;
            this.baseValue = baseValue;
            this.damage = damage;
            this.crit = crit;
            this.kb = kb;
            this.useTime = useTime;
        }

        @Override public DamageClass getDamageClass() { return DamageClass.MELEE; }
        @Override public double getTerrariaBaseDamage() { return damage; }
        @Override public double getBaseCritChance() { return crit; }
        @Override public double getKnockback() { return kb; }
        @Override public int getUseTime() { return useTime; }
        @Override public double getManaCost() { return 0.0; }
        @Override public TerrariaRarity getBaseRarity() { return rarity; }
        @Override public long getBaseValue() { return baseValue; }
        @Override public TerrariaPrefixCategory getPrefixCategory() { return TerrariaPrefixCategory.MELEE; }
    }

    @Test
    @DisplayName("Terraria Rarities map levels, colors and translations correctly")
    void testTerrariaRarities() {
        assertEquals(-1, TerrariaRarity.GRAY.getLevel());
        assertEquals(0, TerrariaRarity.WHITE.getLevel());
        assertEquals(1, TerrariaRarity.BLUE.getLevel());
        assertEquals(2, TerrariaRarity.GREEN.getLevel());
        assertEquals(3, TerrariaRarity.ORANGE.getLevel());
        assertEquals(4, TerrariaRarity.LIGHT_RED.getLevel());
        assertEquals(5, TerrariaRarity.PINK.getLevel());
        assertEquals(6, TerrariaRarity.LIGHT_PURPLE.getLevel());
        assertEquals(7, TerrariaRarity.LIME.getLevel());
        assertEquals(8, TerrariaRarity.YELLOW.getLevel());
        assertEquals(9, TerrariaRarity.CYAN.getLevel());
        assertEquals(10, TerrariaRarity.RED.getLevel());
        assertEquals(11, TerrariaRarity.PURPLE.getLevel());
        assertEquals(12, TerrariaRarity.RAINBOW.getLevel());

        assertEquals(TerrariaRarity.WHITE, TerrariaRarity.fromLevel(0));
        assertEquals(TerrariaRarity.GREEN, TerrariaRarity.fromLevel(2));
        assertEquals(TerrariaRarity.PURPLE, TerrariaRarity.fromLevel(11));
    }

    @Test
    @DisplayName("PrefixRegistry registers canonical universal, melee, ranged, magic, and accessory prefixes")
    void testPrefixRegistry() {
        Optional<TerrariaPrefix> legendary = PrefixRegistry.get("legendary");
        assertTrue(legendary.isPresent());
        assertEquals(TerrariaPrefixCategory.MELEE, legendary.get().category());
        assertEquals(0.15, legendary.get().damageModifier(), 0.001);
        assertEquals(0.10, legendary.get().speedModifier(), 0.001);
        assertEquals(5.0, legendary.get().critChanceBonus(), 0.001);
        assertEquals(2, legendary.get().rarityTierOffset());

        Optional<TerrariaPrefix> unreal = PrefixRegistry.get("unreal");
        assertTrue(unreal.isPresent());
        assertEquals(TerrariaPrefixCategory.RANGED, unreal.get().category());

        Optional<TerrariaPrefix> mythical = PrefixRegistry.get("mythical");
        assertTrue(mythical.isPresent());
        assertEquals(TerrariaPrefixCategory.MAGIC, mythical.get().category());
        assertEquals(-0.10, mythical.get().manaCostModifier(), 0.001);

        Optional<TerrariaPrefix> warding = PrefixRegistry.get("warding");
        assertTrue(warding.isPresent());
        assertEquals(TerrariaPrefixCategory.ACCESSORY, warding.get().category());
        assertEquals(4, warding.get().defenseBonus());

        Optional<TerrariaPrefix> menacing = PrefixRegistry.get("menacing");
        assertTrue(menacing.isPresent());
        assertEquals(0.04, menacing.get().damageModifier(), 0.001);
    }

    @Test
    @DisplayName("Coin economy conversion rates and formatting match Terraria canonical rules")
    void testCoinEconomyCalculations() {
        assertEquals(100L, CoinHelper.COPPER_PER_SILVER);
        assertEquals(10_000L, CoinHelper.COPPER_PER_GOLD);
        assertEquals(1_000_000L, CoinHelper.COPPER_PER_PLATINUM);

        // 1 Plat + 5 Gold + 50 Silver + 75 Copper = 1_000_000 + 50_000 + 5_000 + 75 = 1_055_075
        long copper = 1_055_075L;
        String formatted = CoinHelper.formatCoins(copper);
        assertTrue(formatted.contains("1 Platinum"));
        assertTrue(formatted.contains("5 Gold"));
        assertTrue(formatted.contains("50 Silver"));
        assertTrue(formatted.contains("75 Copper"));
    }

    @Test
    @DisplayName("Reforge cost formula calculates one third of current item value")
    void testReforgeCostFormula() {
        TestWeapon weapon = new TestWeapon(TerrariaRarity.WHITE, 3000L, 10.0, 4.0, 4.0, 20);

        // Without prefix: value multiplier 1.0 -> 3000 / 3 = 1000 copper (10 Silver)
        long baseCost = ReforgeService.calculateReforgeCost(weapon.getBaseValue(), 1.0);
        assertEquals(1000L, baseCost);

        // With Legendary prefix (valueMultiplier = 2.10) -> 3000 * 2.10 / 3 = 2100 copper (21 Silver)
        TerrariaPrefix legendary = PrefixRegistry.get("legendary").orElseThrow();
        long legendaryCost = ReforgeService.calculateReforgeCost(weapon.getBaseValue(), legendary.valueMultiplier());
        assertEquals(2100L, legendaryCost);
    }

    @Test
    @DisplayName("Weapon effective stats incorporate prefix modifiers dynamically")
    void testWeaponPrefixApplication() {
        TestWeapon weapon = new TestWeapon(TerrariaRarity.WHITE, 1000L, 20.0, 4.0, 5.0, 20);

        assertEquals(20.0, weapon.calculateEffectiveDamage(TerrariaPrefix.NONE), 0.001);
        assertEquals(4.0, weapon.calculateEffectiveCritChance(TerrariaPrefix.NONE), 0.001);
        assertEquals(5.0, weapon.calculateEffectiveKnockback(TerrariaPrefix.NONE), 0.001);
        assertEquals(20, weapon.calculateEffectiveUseTime(TerrariaPrefix.NONE));
        assertEquals(TerrariaRarity.WHITE, weapon.calculateEffectiveRarity(TerrariaPrefix.NONE));

        // Apply Legendary prefix (+15% damage, +10% speed, +5% crit, +15% kb, +2 rarity)
        TerrariaPrefix legendary = PrefixRegistry.get("legendary").orElseThrow();

        assertEquals(23.0, weapon.calculateEffectiveDamage(legendary), 0.001); // 20 * 1.15 = 23.0
        assertEquals(9.0, weapon.calculateEffectiveCritChance(legendary), 0.001); // 4 + 5 = 9.0
        assertEquals(5.75, weapon.calculateEffectiveKnockback(legendary), 0.001); // 5 * 1.15 = 5.75
        assertEquals(18, weapon.calculateEffectiveUseTime(legendary)); // 20 / 1.10 = 18.18 -> 18
        assertEquals(TerrariaRarity.GREEN, weapon.calculateEffectiveRarity(legendary)); // WHITE (0) + 2 = GREEN (2)
    }
}
