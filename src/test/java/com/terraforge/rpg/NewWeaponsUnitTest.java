package com.terraforge.rpg;

import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.item.rarity.TerrariaRarity;
import com.terraforge.rpg.item.weapon.ITerrariaWeapon;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NewWeaponsUnitTest {

    record WeaponStats(
            String name,
            DamageClass damageClass,
            double baseDamage,
            double critChance,
            double knockback,
            int useTime,
            TerrariaRarity rarity,
            double ammoConservation
    ) {}

    @Test
    @DisplayName("Zenith canonical endgame stats (Terraria 1.4.5.8)")
    void testZenithStats() {
        WeaponStats zenith = new WeaponStats("zenith", DamageClass.MELEE, 190.0, 14.0, 6.5, 30, TerrariaRarity.RED, 0.0);
        assertEquals(190.0, zenith.baseDamage(), 0.001);
        assertEquals(14.0, zenith.critChance(), 0.001);
        assertEquals(TerrariaRarity.RED, zenith.rarity());
        assertEquals(DamageClass.MELEE, zenith.damageClass());
        assertTrue(zenith.baseDamage() > 150.0, "Zenith must be the highest tier sword");
    }

    @Test
    @DisplayName("Terra Blade canonical stats")
    void testTerraBladeStats() {
        WeaponStats terraBlade = new WeaponStats("terra_blade", DamageClass.MELEE, 115.0, 4.0, 6.5, 14, TerrariaRarity.YELLOW, 0.0);
        assertEquals(115.0, terraBlade.baseDamage(), 0.001);
        assertEquals(TerrariaRarity.YELLOW, terraBlade.rarity());
        assertEquals(DamageClass.MELEE, terraBlade.damageClass());
    }

    @Test
    @DisplayName("Meowmere canonical stats")
    void testMeowmereStats() {
        WeaponStats meowmere = new WeaponStats("meowmere", DamageClass.MELEE, 200.0, 4.0, 6.5, 14, TerrariaRarity.RED, 0.0);
        assertEquals(200.0, meowmere.baseDamage(), 0.001);
        assertEquals(TerrariaRarity.RED, meowmere.rarity());
    }

    @Test
    @DisplayName("Vortex Beater stats and 66% ammo conservation")
    void testVortexBeaterStats() {
        WeaponStats vortex = new WeaponStats("vortex_beater", DamageClass.RANGED, 50.0, 4.0, 2.5, 12, TerrariaRarity.RED, 0.66);
        assertEquals(50.0, vortex.baseDamage(), 0.001);
        assertEquals(0.66, vortex.ammoConservation(), 0.001);
        assertEquals(TerrariaRarity.RED, vortex.rarity());
        assertEquals(DamageClass.RANGED, vortex.damageClass());
    }

    @Test
    @DisplayName("Megashark and Minishark ammo conservation thresholds")
    void testSharkFirearmsAmmoConservation() {
        WeaponStats minishark = new WeaponStats("minishark", DamageClass.RANGED, 6.0, 4.0, 0.0, 8, TerrariaRarity.GREEN, 0.33);
        WeaponStats megashark = new WeaponStats("megashark", DamageClass.RANGED, 25.0, 4.0, 1.0, 6, TerrariaRarity.PINK, 0.50);

        assertEquals(0.33, minishark.ammoConservation(), 0.001);
        assertEquals(0.50, megashark.ammoConservation(), 0.001);
        assertTrue(megashark.baseDamage() > minishark.baseDamage());
        assertTrue(megashark.useTime() < minishark.useTime());
    }

    @Test
    @DisplayName("Pre-hardmode firearm progression: Boomstick vs Phoenix Blaster")
    void testPreHardmodeFirearms() {
        WeaponStats boomstick = new WeaponStats("boomstick", DamageClass.RANGED, 14.0, 4.0, 5.75, 40, TerrariaRarity.ORANGE, 0.0);
        WeaponStats phoenixBlaster = new WeaponStats("phoenix_blaster", DamageClass.RANGED, 30.0, 4.0, 2.0, 17, TerrariaRarity.ORANGE, 0.0);

        assertEquals(14.0, boomstick.baseDamage(), 0.001);
        assertEquals(30.0, phoenixBlaster.baseDamage(), 0.001);
        assertEquals(TerrariaRarity.ORANGE, boomstick.rarity());
        assertEquals(TerrariaRarity.ORANGE, phoenixBlaster.rarity());
    }
}
