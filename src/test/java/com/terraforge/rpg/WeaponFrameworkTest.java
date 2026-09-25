package com.terraforge.rpg;

import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.item.ammo.AmmoType;
import com.terraforge.rpg.item.ammo.ITerrariaAmmo;
import com.terraforge.rpg.item.prefix.TerrariaPrefix;
import com.terraforge.rpg.item.prefix.TerrariaPrefixCategory;
import com.terraforge.rpg.item.rarity.TerrariaRarity;
import com.terraforge.rpg.item.weapon.whip.ITerrariaWhip;
import com.terraforge.rpg.item.weapon.yoyo.ITerrariaYoyo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WeaponFrameworkTest {

    static class DummyYoyo implements ITerrariaYoyo {
        @Override public double getReach() { return 9.0; }
        @Override public int getFlightTime() { return 80; }
        @Override public int getStringColor() { return 0x8B5A2B; }
        @Override public DamageClass getDamageClass() { return DamageClass.MELEE; }
        @Override public double getTerrariaBaseDamage() { return 9.0; }
        @Override public double getBaseCritChance() { return 4.0; }
        @Override public double getKnockback() { return 3.0; }
        @Override public int getUseTime() { return 25; }
        @Override public double getManaCost() { return 0.0; }
        @Override public TerrariaRarity getBaseRarity() { return TerrariaRarity.WHITE; }
        @Override public long getBaseValue() { return 100L; }
        @Override public TerrariaPrefixCategory getPrefixCategory() { return TerrariaPrefixCategory.MELEE; }
    }

    static class DummyAmmo implements ITerrariaAmmo {
        private final AmmoType type;
        private final double bonusDmg;
        private final int pierce;
        private final int bounce;

        DummyAmmo(AmmoType type, double bonusDmg, int pierce, int bounce) {
            this.type = type;
            this.bonusDmg = bonusDmg;
            this.pierce = pierce;
            this.bounce = bounce;
        }

        @Override public AmmoType getAmmoType() { return type; }
        @Override public double getBonusDamage() { return bonusDmg; }
        @Override public double getBonusVelocity() { return 1.0; }
        @Override public double getBonusKnockback() { return 2.0; }
        @Override public int getPiercingCount() { return pierce; }
        @Override public int getBounceCount() { return bounce; }
        @Override public TerrariaRarity getBaseRarity() { return TerrariaRarity.WHITE; }
        @Override public long getBaseValue() { return 10L; }
    }

    static class DummyWhip implements ITerrariaWhip {
        @Override public int getSummonTagDamage() { return 4; }
        @Override public double getReach() { return 4.5; }
        @Override public DamageClass getDamageClass() { return DamageClass.SUMMON; }
        @Override public double getTerrariaBaseDamage() { return 14.0; }
        @Override public double getBaseCritChance() { return 4.0; }
        @Override public double getKnockback() { return 1.0; }
        @Override public int getUseTime() { return 30; }
        @Override public double getManaCost() { return 0.0; }
        @Override public TerrariaRarity getBaseRarity() { return TerrariaRarity.WHITE; }
        @Override public long getBaseValue() { return 10_000L; }
    }

    @Test
    @DisplayName("Yoyo interface enforces reach, flight time limits, and melee classification")
    void testYoyoMechanics() {
        DummyYoyo yoyo = new DummyYoyo();
        assertEquals(DamageClass.MELEE, yoyo.getDamageClass());
        assertEquals(9.0, yoyo.getReach(), 0.001);
        assertEquals(80, yoyo.getFlightTime());
        assertEquals(9.0, yoyo.getTerrariaBaseDamage(), 0.001);

        // Effective stats with Godly prefix (+15% dmg, +5% crit, +15% kb)
        TerrariaPrefix godly = new TerrariaPrefix("godly", "Godly", TerrariaPrefixCategory.COMMON,
                0.15, 0.0, 5.0, 0.15, 0.0, 0.0, 0.0, 0, 1.70, 2);

        assertEquals(10.35, yoyo.calculateEffectiveDamage(godly), 0.001);
        assertEquals(9.0, yoyo.calculateEffectiveCritChance(godly), 0.001);
        assertEquals(3.45, yoyo.calculateEffectiveKnockback(godly), 0.001);
    }

    @Test
    @DisplayName("Ammunition framework differentiates arrows, bullets, piercing, and bouncing")
    void testAmmunitionProperties() {
        DummyAmmo woodenArrow = new DummyAmmo(AmmoType.ARROW, 4.0, 0, 0);
        assertEquals(AmmoType.ARROW, woodenArrow.getAmmoType());
        assertEquals(4.0, woodenArrow.getBonusDamage(), 0.001);
        assertEquals(0, woodenArrow.getPiercingCount());

        DummyAmmo jesterArrow = new DummyAmmo(AmmoType.ARROW, 9.0, 999, 0);
        assertEquals(999, jesterArrow.getPiercingCount());

        DummyAmmo meteorShot = new DummyAmmo(AmmoType.BULLET, 9.0, 1, 1);
        assertEquals(AmmoType.BULLET, meteorShot.getAmmoType());
        assertEquals(1, meteorShot.getPiercingCount());
        assertEquals(1, meteorShot.getBounceCount());
    }

    @Test
    @DisplayName("Whip summon tag damage attributes are preserved")
    void testWhipAttributes() {
        DummyWhip whip = new DummyWhip();

        assertEquals(DamageClass.SUMMON, whip.getDamageClass());
        assertEquals(14.0, whip.getTerrariaBaseDamage(), 0.001);
        assertEquals(4.5, whip.getReach(), 0.001);
        assertEquals(4, whip.getSummonTagDamage());
    }
}
