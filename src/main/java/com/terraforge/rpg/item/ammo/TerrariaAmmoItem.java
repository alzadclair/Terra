package com.terraforge.rpg.item.ammo;

import com.terraforge.rpg.economy.CoinHelper;
import com.terraforge.rpg.item.rarity.TerrariaRarity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Base item class for ammunition (Arrows, Bullets, Rockets).
 */
public class TerrariaAmmoItem extends Item implements ITerrariaAmmo {

    private final AmmoType ammoType;
    private final TerrariaRarity rarity;
    private final long baseValue;
    private final double bonusDamage;
    private final double bonusVelocity;
    private final double bonusKnockback;
    private final int piercingCount;
    private final int bounceCount;

    public TerrariaAmmoItem(
            Properties properties,
            AmmoType ammoType,
            TerrariaRarity rarity,
            long baseValue,
            double bonusDamage,
            double bonusVelocity,
            double bonusKnockback,
            int piercingCount,
            int bounceCount
    ) {
        super(properties);
        this.ammoType = ammoType;
        this.rarity = rarity;
        this.baseValue = baseValue;
        this.bonusDamage = bonusDamage;
        this.bonusVelocity = bonusVelocity;
        this.bonusKnockback = bonusKnockback;
        this.piercingCount = piercingCount;
        this.bounceCount = bounceCount;
    }

    @Override public AmmoType getAmmoType() { return ammoType; }
    @Override public TerrariaRarity getBaseRarity() { return rarity; }
    @Override public long getBaseValue() { return baseValue; }
    @Override public double getBonusDamage() { return bonusDamage; }
    @Override public double getBonusVelocity() { return bonusVelocity; }
    @Override public double getBonusKnockback() { return bonusKnockback; }
    @Override public int getPiercingCount() { return piercingCount; }
    @Override public int getBounceCount() { return bounceCount; }

    @Override
    public Component getName(ItemStack stack) {
        return super.getName(stack).copy().withStyle(style -> style.withColor(rarity.getTextColor()));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        tooltipComponents.add(Component.literal((int) bonusDamage + " Ranged damage").withStyle(ChatFormatting.WHITE));
        tooltipComponents.add(Component.literal("Ammo: " + ammoType.name().toLowerCase()).withStyle(ChatFormatting.GRAY));

        if (piercingCount > 0) {
            tooltipComponents.add(Component.literal("Pierces " + piercingCount + " enemies").withStyle(ChatFormatting.BLUE));
        }
        if (bounceCount > 0) {
            tooltipComponents.add(Component.literal("Bounces " + bounceCount + " times").withStyle(ChatFormatting.BLUE));
        }

        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.literal("Price: " + CoinHelper.formatCoins(baseValue)).withStyle(ChatFormatting.GOLD));
    }
}
