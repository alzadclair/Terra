package com.terraforge.rpg;

import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.item.rarity.TerrariaRarity;
import com.terraforge.rpg.item.weapon.*;
import net.minecraft.world.item.Item;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NewWeaponsUnitTest {

    @BeforeAll
    static void setup() {
        try {
            java.lang.reflect.Method ofMethod = Class.forName("net.neoforged.fml.loading.LoadingModList")
                    .getMethod("of", java.util.List.class, java.util.List.class, java.util.List.class, java.util.List.class, java.util.Map.class);
            ofMethod.invoke(null, java.util.List.of(), java.util.List.of(), java.util.List.of(), java.util.List.of(), java.util.Map.of());
        } catch (Throwable ignored) {
        }
        try {
            net.minecraft.SharedConstants.tryDetectVersion();
            net.minecraft.server.Bootstrap.bootStrap();
            java.lang.reflect.Field frozenField = net.minecraft.core.MappedRegistry.class.getDeclaredField("frozen");
            frozenField.setAccessible(true);
            frozenField.setBoolean(net.minecraft.core.registries.BuiltInRegistries.ITEM, false);
        } catch (Throwable ignored) {
        }
    }

    @Test
    @DisplayName("Zenith real item canonical stats (Terraria 1.4.5.8)")
    void testZenithStats() {
        ZenithItem zenith = new ZenithItem(new Item.Properties());
        assertEquals(190.0, zenith.getTerrariaBaseDamage(), 0.001);
        assertEquals(0.14, zenith.getBaseCritChance(), 0.001);
        assertEquals(6.5, zenith.getKnockback(), 0.001);
        assertEquals(12, zenith.getUseTime());
        assertEquals(TerrariaRarity.RED, zenith.getBaseRarity());
        assertEquals(DamageClass.MELEE, zenith.getDamageClass());
        assertTrue(zenith.getTerrariaBaseDamage() > 150.0, "Zenith must be the highest tier sword");
    }

    @Test
    @DisplayName("True Night's Edge real item canonical stats")
    void testTrueNightsEdgeStats() {
        TrueNightsEdgeItem tne = new TrueNightsEdgeItem(new Item.Properties());
        assertEquals(70.0, tne.getTerrariaBaseDamage(), 0.001);
        assertEquals(0.04, tne.getBaseCritChance(), 0.001);
        assertEquals(4.75, tne.getKnockback(), 0.001);
        assertEquals(26, tne.getUseTime());
        assertEquals(TerrariaRarity.PINK, tne.getBaseRarity());
        assertEquals(DamageClass.MELEE, tne.getDamageClass());
    }

    @Test
    @DisplayName("Seedler real item stats")
    void testSeedlerStats() {
        SeedlerItem seedler = new SeedlerItem(new Item.Properties());
        assertEquals(50.0, seedler.getTerrariaBaseDamage(), 0.001);
        assertEquals(0.04, seedler.getBaseCritChance(), 0.001);
        assertEquals(6.0, seedler.getKnockback(), 0.001);
        assertEquals(28, seedler.getUseTime());
        assertEquals(TerrariaRarity.PINK, seedler.getBaseRarity());
        assertEquals(DamageClass.MELEE, seedler.getDamageClass());
    }

    @Test
    @DisplayName("Starfury real item stats")
    void testStarfuryStats() {
        StarfuryItem starfury = new StarfuryItem(new Item.Properties());
        assertEquals(25.0, starfury.getTerrariaBaseDamage(), 0.001);
        assertEquals(0.04, starfury.getBaseCritChance(), 0.001);
        assertEquals(5.0, starfury.getKnockback(), 0.001);
        assertEquals(20, starfury.getUseTime());
        assertEquals(TerrariaRarity.GREEN, starfury.getBaseRarity());
        assertEquals(DamageClass.MELEE, starfury.getDamageClass());
    }

    @Test
    @DisplayName("Vortex Beater real item stats and 66% ammo conservation")
    void testVortexBeaterStats() {
        VortexBeaterItem vortex = new VortexBeaterItem(new Item.Properties());
        assertEquals(50.0, vortex.getTerrariaBaseDamage(), 0.001);
        assertEquals(0.04, vortex.getBaseCritChance(), 0.001);
        assertEquals(2.5, vortex.getKnockback(), 0.001);
        assertEquals(12, vortex.getUseTime());
        assertEquals(0.66, vortex.getAmmoConservationChance(), 0.001);
        assertEquals(TerrariaRarity.RED, vortex.getBaseRarity());
        assertEquals(DamageClass.RANGED, vortex.getDamageClass());
    }

    @Test
    @DisplayName("Uzi real item stats")
    void testUziStats() {
        UziItem uzi = new UziItem(new Item.Properties());
        assertEquals(30.0, uzi.getTerrariaBaseDamage(), 0.001);
        assertEquals(0.04, uzi.getBaseCritChance(), 0.001);
        assertEquals(3.5, uzi.getKnockback(), 0.001);
        assertEquals(9, uzi.getUseTime());
        assertEquals(TerrariaRarity.PINK, uzi.getBaseRarity());
        assertEquals(DamageClass.RANGED, uzi.getDamageClass());
    }

    @Test
    @DisplayName("Boomstick and Phoenix Blaster real item firearm stats")
    void testPreHardmodeFirearms() {
        BoomstickItem boomstick = new BoomstickItem(new Item.Properties());
        PhoenixBlasterItem phoenixBlaster = new PhoenixBlasterItem(new Item.Properties());

        assertEquals(14.0, boomstick.getTerrariaBaseDamage(), 0.001);
        assertEquals(0.04, boomstick.getBaseCritChance(), 0.001);
        assertEquals(5.5, boomstick.getKnockback(), 0.001);
        assertEquals(40, boomstick.getUseTime());
        assertEquals(TerrariaRarity.GREEN, boomstick.getBaseRarity());
        assertEquals(DamageClass.RANGED, boomstick.getDamageClass());

        assertEquals(24.0, phoenixBlaster.getTerrariaBaseDamage(), 0.001);
        assertEquals(0.04, phoenixBlaster.getBaseCritChance(), 0.001);
        assertEquals(4.0, phoenixBlaster.getKnockback(), 0.001);
        assertEquals(17, phoenixBlaster.getUseTime());
        assertEquals(TerrariaRarity.ORANGE, phoenixBlaster.getBaseRarity());
        assertEquals(DamageClass.RANGED, phoenixBlaster.getDamageClass());
    }

    @Test
    @DisplayName("Celebration Mk2 real item stats")
    void testCelebrationMk2Stats() {
        CelebrationMk2Item mk2 = new CelebrationMk2Item(new Item.Properties());
        assertEquals(40.0, mk2.getTerrariaBaseDamage(), 0.001);
        assertEquals(0.10, mk2.getBaseCritChance(), 0.001);
        assertEquals(5.0, mk2.getKnockback(), 0.001);
        assertEquals(30, mk2.getUseTime());
        assertEquals(TerrariaRarity.RED, mk2.getBaseRarity());
        assertEquals(DamageClass.RANGED, mk2.getDamageClass());
    }

    @Test
    @DisplayName("Diamond Staff real item magic stats")
    void testDiamondStaffStats() {
        DiamondStaffItem staff = new DiamondStaffItem(new Item.Properties());
        assertEquals(23.0, staff.getTerrariaBaseDamage(), 0.001);
        assertEquals(0.04, staff.getBaseCritChance(), 0.001);
        assertEquals(5.5, staff.getKnockback(), 0.001);
        assertEquals(26, staff.getUseTime());
        assertEquals(8.0, staff.getManaCost(), 0.001);
        assertEquals(TerrariaRarity.WHITE, staff.getBaseRarity());
        assertEquals(DamageClass.MAGIC, staff.getDamageClass());
    }
}
