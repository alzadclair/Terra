package com.terraforge.rpg.accessory.special;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Base item class for the 10 craftable RPG Special Accessories.
 */
public class SpecialAccessoryItem extends Item {
    private final Component effectDescription;

    public SpecialAccessoryItem(Properties properties, Component effectDescription) {
        super(properties);
        this.effectDescription = effectDescription;
    }

    public Component getEffectDescription() {
        return effectDescription;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("§6§l[Artefato Especial]"));
        tooltipComponents.add(Component.literal("§7Equipável no slot exclusivo do Menu K."));
        tooltipComponents.add(Component.literal("§eEfeito: ").append(effectDescription));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
