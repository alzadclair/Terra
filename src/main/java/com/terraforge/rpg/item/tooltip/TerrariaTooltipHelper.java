package com.terraforge.rpg.item.tooltip;

import com.terraforge.rpg.economy.CoinHelper;
import com.terraforge.rpg.item.ITerrariaItem;
import com.terraforge.rpg.item.prefix.TerrariaPrefix;
import com.terraforge.rpg.item.rarity.TerrariaRarity;
import com.terraforge.rpg.item.weapon.ITerrariaWeapon;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Generates Terraria 1.4.5.8 canonical tooltips for weapons, accessories, and items.
 */
public final class TerrariaTooltipHelper {

    private TerrariaTooltipHelper() {}

    public static void buildTooltip(ItemStack stack, ITerrariaItem item, List<Component> lines) {
        TerrariaPrefix prefix = item.getPrefix(stack);
        TerrariaRarity rarity = item.getEffectiveRarity(stack);

        // Weapon stats breakdown
        if (item instanceof ITerrariaWeapon weapon) {
            double effectiveDamage = weapon.getEffectiveDamage(stack);
            String dmgClass = weapon.getDamageClass().getDisplayName();
            lines.add(Component.literal((int) Math.round(effectiveDamage) + " " + dmgClass + " damage")
                    .withStyle(ChatFormatting.WHITE));

            double effectiveCrit = weapon.getEffectiveCritChance(stack);
            lines.add(Component.literal((int) Math.round(effectiveCrit) + "% critical strike chance")
                    .withStyle(ChatFormatting.WHITE));

            if (weapon.getManaCost() > 0) {
                double mana = weapon.getEffectiveManaCost(stack);
                lines.add(Component.literal("Uses " + (int) Math.round(mana) + " mana")
                        .withStyle(ChatFormatting.AQUA));
            }

            lines.add(Component.literal("Use time: " + weapon.getEffectiveUseTime(stack) + " ticks")
                    .withStyle(ChatFormatting.GRAY));

            lines.add(Component.literal("Knockback: " + String.format("%.1f", weapon.getEffectiveKnockback(stack)))
                    .withStyle(ChatFormatting.GRAY));
        }

        // Prefix stat modifiers breakdown
        if (!prefix.isNone()) {
            lines.add(Component.literal(""));
            if (prefix.damageModifier() != 0.0) {
                appendModifierLine(lines, prefix.damageModifier() * 100.0, "% damage");
            }
            if (prefix.speedModifier() != 0.0) {
                appendModifierLine(lines, prefix.speedModifier() * 100.0, "% speed");
            }
            if (prefix.critChanceBonus() != 0.0) {
                appendModifierLine(lines, prefix.critChanceBonus(), "% critical strike chance");
            }
            if (prefix.manaCostModifier() != 0.0) {
                // For mana cost, negative is beneficial (less mana)
                appendManaCostLine(lines, prefix.manaCostModifier() * 100.0, "% mana cost");
            }
            if (prefix.knockbackModifier() != 0.0) {
                appendModifierLine(lines, prefix.knockbackModifier() * 100.0, "% knockback");
            }
            if (prefix.sizeModifier() != 0.0) {
                appendModifierLine(lines, prefix.sizeModifier() * 100.0, "% size");
            }
            if (prefix.velocityModifier() != 0.0) {
                appendModifierLine(lines, prefix.velocityModifier() * 100.0, "% velocity");
            }
            if (prefix.defenseBonus() != 0) {
                appendModifierLine(lines, prefix.defenseBonus(), " defense");
            }
        }

        // Value in coins
        lines.add(Component.literal(""));
        long value = Math.round(item.getBaseValue() * prefix.valueMultiplier());
        lines.add(Component.literal("Price: " + CoinHelper.formatCoins(value))
                .withStyle(ChatFormatting.GOLD));
    }

    private static void appendModifierLine(List<Component> lines, double amount, String suffix) {
        boolean positive = amount > 0;
        String sign = positive ? "+" : "";
        ChatFormatting color = positive ? ChatFormatting.BLUE : ChatFormatting.RED;
        lines.add(Component.literal(sign + (int) Math.round(amount) + suffix).withStyle(color));
    }

    private static void appendManaCostLine(List<Component> lines, double amount, String suffix) {
        boolean beneficial = amount < 0;
        String sign = amount > 0 ? "+" : "";
        ChatFormatting color = beneficial ? ChatFormatting.BLUE : ChatFormatting.RED;
        lines.add(Component.literal(sign + (int) Math.round(amount) + suffix).withStyle(color));
    }
}
