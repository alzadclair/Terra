package com.terraforge.rpg.item.accessory;

import com.terraforge.rpg.item.prefix.TerrariaPrefix;
import com.terraforge.rpg.item.rarity.TerrariaRarity;
import com.terraforge.rpg.item.tooltip.TerrariaTooltipHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Base class for standard Terraria accessories.
 */
public class TerrariaAccessoryItem extends Item implements ITerrariaAccessory {

    private final TerrariaRarity baseRarity;
    private final long baseValue;

    public TerrariaAccessoryItem(Properties properties, TerrariaRarity baseRarity, long baseValue) {
        super(properties);
        this.baseRarity = baseRarity;
        this.baseValue = baseValue;
    }

    @Override
    public TerrariaRarity getBaseRarity() {
        return baseRarity;
    }

    @Override
    public long getBaseValue() {
        return baseValue;
    }

    @Override
    public void onEquipped(Player player, ItemStack stack) {
        // Subclasses implement custom equip logic
    }

    @Override
    public void onUnequipped(Player player, ItemStack stack) {
        // Subclasses implement custom unequip logic
    }

    @Override
    public void tickAccessory(Player player, ItemStack stack) {
        // Subclasses implement passive ticking logic
    }

    @Override
    public Component getName(ItemStack stack) {
        TerrariaPrefix prefix = getPrefix(stack);
        MutableComponent name;
        if (!prefix.isNone()) {
            name = Component.literal(prefix.displayName() + " ").append(super.getName(stack));
        } else {
            name = super.getName(stack).copy();
        }
        return name.withStyle(style -> style.withColor(getEffectiveRarity(stack).getTextColor()));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        TerrariaTooltipHelper.buildTooltip(stack, this, tooltipComponents);
    }
}
