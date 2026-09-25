package com.terraforge.rpg.item.fishing;

import com.terraforge.rpg.item.ITerrariaItem;
import com.terraforge.rpg.item.prefix.TerrariaPrefixCategory;
import com.terraforge.rpg.item.rarity.TerrariaRarity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Base class for all Terraria fishing rods with custom fishing power.
 */
public class TerrariaFishingPoleItem extends FishingRodItem implements ITerrariaItem {

    private final int fishingPower;
    private final TerrariaRarity rarity;
    private final long value;

    public TerrariaFishingPoleItem(Properties properties, int fishingPower, TerrariaRarity rarity, long value) {
        super(properties);
        this.fishingPower = fishingPower;
        this.rarity = rarity;
        this.value = value;
    }

    public int getFishingPower() {
        return fishingPower;
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
        tooltipComponents.add(Component.literal(fishingPower + "% fishing power").withStyle(ChatFormatting.BLUE));
    }
}
