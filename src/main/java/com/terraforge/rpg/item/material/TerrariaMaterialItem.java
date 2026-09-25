package com.terraforge.rpg.item.material;

import com.terraforge.rpg.item.ITerrariaItem;
import com.terraforge.rpg.item.prefix.TerrariaPrefixCategory;
import com.terraforge.rpg.item.rarity.TerrariaRarity;
import com.terraforge.rpg.item.tooltip.TerrariaTooltipHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Canonical Terraria crafting material item (e.g. Hallowed Bar, Souls, Essences).
 */
public class TerrariaMaterialItem extends Item implements ITerrariaItem {

    private final TerrariaRarity rarity;
    private final long sellValue;
    private final String description;

    public TerrariaMaterialItem(Properties properties, TerrariaRarity rarity, long sellValue, String description) {
        super(properties);
        this.rarity = rarity;
        this.sellValue = sellValue;
        this.description = description;
    }

    public TerrariaMaterialItem(Properties properties, TerrariaRarity rarity, long sellValue) {
        this(properties, rarity, sellValue, null);
    }

    @Override
    public TerrariaRarity getBaseRarity() {
        return rarity;
    }

    @Override
    public long getBaseValue() {
        return sellValue;
    }

    @Override
    public TerrariaPrefixCategory getPrefixCategory() {
        return TerrariaPrefixCategory.UNIVERSAL;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (description != null && !description.isEmpty()) {
            tooltipComponents.add(Component.literal(description).withStyle(s -> s.withColor(rarity.getTextColor())));
        }
        TerrariaTooltipHelper.buildTooltip(stack, this, tooltipComponents);
    }
}
