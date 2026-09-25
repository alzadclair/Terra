package com.terraforge.rpg.item.weapon;

import com.terraforge.rpg.item.rarity.TerrariaRarity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * The iconic Pwnhammer dropped exclusively by Wall of Flesh upon entering Hardmode.
 * Possesses 80% hammer power capable of smashing Demon and Crimson Altars.
 */
public class PwnhammerItem extends TerrariaSwordItem {

    public static final int HAMMER_POWER = 80;

    public PwnhammerItem(Properties properties) {
        super(properties, TerrariaRarity.LIGHT_RED, 16_000L, 26.0, 4.0, 7.5, 27);
    }

    public int getHammerPower() {
        return HAMMER_POWER;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.literal("80% Hammer Power").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal("Able to destroy demon altars").withStyle(ChatFormatting.BLUE));
    }
}
