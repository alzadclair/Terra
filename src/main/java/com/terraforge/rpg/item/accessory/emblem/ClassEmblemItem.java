package com.terraforge.rpg.item.accessory.emblem;

import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.item.accessory.TerrariaAccessoryItem;
import com.terraforge.rpg.item.rarity.TerrariaRarity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Wall of Flesh class-specific emblem accessory.
 * Grants canonical +15% damage bonus to its corresponding damage class.
 */
public class ClassEmblemItem extends TerrariaAccessoryItem {

    private final DamageClass damageClass;

    public ClassEmblemItem(Properties properties, DamageClass damageClass) {
        super(properties, TerrariaRarity.LIGHT_RED, 20_000L); // 2 Gold Coins
        this.damageClass = damageClass;
    }

    public DamageClass getTargetDamageClass() {
        return damageClass;
    }

    public double getDamageBonus() {
        return 0.15; // +15%
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.literal("15% increased " + damageClass.getDisplayName().toLowerCase() + " damage")
                .withStyle(ChatFormatting.BLUE));
    }
}
