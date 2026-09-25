package com.terraforge.rpg.item.weapon;

import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.item.ITerrariaItem;
import com.terraforge.rpg.item.prefix.TerrariaPrefix;
import net.minecraft.world.item.ItemStack;

/**
 * Interface contract for all Terraria weapons.
 */
public interface ITerrariaWeapon extends ITerrariaItem {

    DamageClass getDamageClass();

    double getTerrariaBaseDamage();

    double getBaseCritChance();

    double getKnockback();

    int getUseTime();

    double getManaCost();

    default double calculateEffectiveDamage(TerrariaPrefix prefix) {
        double base = getTerrariaBaseDamage();
        if (prefix == null || prefix.isNone()) return base;
        return base * (1.0 + prefix.damageModifier());
    }

    default double getEffectiveDamage(ItemStack stack) {
        return calculateEffectiveDamage(getPrefix(stack));
    }

    default double calculateEffectiveCritChance(TerrariaPrefix prefix) {
        double base = getBaseCritChance();
        if (prefix == null || prefix.isNone()) return base;
        return base + prefix.critChanceBonus();
    }

    default double getEffectiveCritChance(ItemStack stack) {
        return calculateEffectiveCritChance(getPrefix(stack));
    }

    default double calculateEffectiveKnockback(TerrariaPrefix prefix) {
        double base = getKnockback();
        if (prefix == null || prefix.isNone()) return base;
        return base * (1.0 + prefix.knockbackModifier());
    }

    default double getEffectiveKnockback(ItemStack stack) {
        return calculateEffectiveKnockback(getPrefix(stack));
    }

    default int calculateEffectiveUseTime(TerrariaPrefix prefix) {
        if (prefix == null || prefix.isNone()) return getUseTime();
        double speedMod = 1.0 + prefix.speedModifier();
        if (speedMod <= 0.0) speedMod = 0.1;
        return (int) Math.max(1, Math.round(getUseTime() / speedMod));
    }

    default int getEffectiveUseTime(ItemStack stack) {
        return calculateEffectiveUseTime(getPrefix(stack));
    }

    default double calculateEffectiveManaCost(TerrariaPrefix prefix) {
        double base = getManaCost();
        if (base <= 0.0) return 0.0;
        if (prefix == null || prefix.isNone()) return base;
        return Math.max(1.0, base * (1.0 + prefix.manaCostModifier()));
    }

    default double getEffectiveManaCost(ItemStack stack) {
        return calculateEffectiveManaCost(getPrefix(stack));
    }
}
