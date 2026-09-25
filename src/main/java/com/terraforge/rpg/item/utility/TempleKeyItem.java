package com.terraforge.rpg.item.utility;

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
 * Temple Key - Dropped by Plantera, used to unlock the Lihzahrd Temple (Terraria 1.4.5.8 canonical).
 */
public class TempleKeyItem extends Item implements ITerrariaItem {

    public TempleKeyItem(Properties properties) {
        super(properties);
    }

    @Override public TerrariaRarity getBaseRarity() { return TerrariaRarity.LIME; }
    @Override public long getBaseValue() { return 50_000L; } // 5 Gold
    @Override public TerrariaPrefixCategory getPrefixCategory() { return TerrariaPrefixCategory.UNIVERSAL; }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.literal("Unlocks the Jungle Temple door").withStyle(ChatFormatting.GREEN));
    }
}
