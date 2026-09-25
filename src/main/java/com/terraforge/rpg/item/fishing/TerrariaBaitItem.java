package com.terraforge.rpg.item.fishing;

import com.terraforge.rpg.item.ITerrariaItem;
import com.terraforge.rpg.item.prefix.TerrariaPrefixCategory;
import com.terraforge.rpg.item.rarity.TerrariaRarity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Bait item providing bait power for fishing catches.
 */
public class TerrariaBaitItem extends Item implements ITerrariaItem {

    private final int baitPower;
    private final TerrariaRarity rarity;
    private final long value;

    public TerrariaBaitItem(Properties properties, int baitPower, TerrariaRarity rarity, long value) {
        super(properties);
        this.baitPower = baitPower;
        this.rarity = rarity;
        this.value = value;
    }

    public int getBaitPower() {
        return baitPower;
    }

    @Override
    public TerrariaRarity getBaseRarity() {
        return rarity;
    }

    @Override
    public long getBaseValue() {
        return value;
    }

    @Override
    public TerrariaPrefixCategory getPrefixCategory() {
        return TerrariaPrefixCategory.UNIVERSAL;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.literal(baitPower + "% bait power").withStyle(ChatFormatting.BLUE));
    }
}
