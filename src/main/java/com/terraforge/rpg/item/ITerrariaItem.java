package com.terraforge.rpg.item;

import com.terraforge.rpg.item.prefix.TerrariaPrefix;
import com.terraforge.rpg.item.prefix.TerrariaPrefixCategory;
import com.terraforge.rpg.item.rarity.TerrariaRarity;
import com.terraforge.rpg.registry.ModDataComponents;
import com.terraforge.rpg.item.prefix.PrefixRegistry;
import net.minecraft.world.item.ItemStack;

/**
 * Common contract for all Terraria 1.4.5.8 items in TerraForge RPG.
 */
public interface ITerrariaItem {

    TerrariaRarity getBaseRarity();

    /**
     * Base coin value in copper coins (1 Gold = 10,000 Copper).
     */
    long getBaseValue();

    TerrariaPrefixCategory getPrefixCategory();

    default TerrariaPrefix getPrefix(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return TerrariaPrefix.NONE;
        String prefixId = stack.get(ModDataComponents.PREFIX);
        if (prefixId == null || prefixId.isEmpty()) return TerrariaPrefix.NONE;
        return PrefixRegistry.get(prefixId).orElse(TerrariaPrefix.NONE);
    }

    default TerrariaRarity calculateEffectiveRarity(TerrariaPrefix prefix) {
        if (prefix == null || prefix.isNone()) return getBaseRarity();
        int effectiveLevel = getBaseRarity().getLevel() + prefix.rarityTierOffset();
        return TerrariaRarity.fromLevel(effectiveLevel);
    }

    default TerrariaRarity getEffectiveRarity(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return getBaseRarity();
        Integer override = stack.get(ModDataComponents.RARITY_OVERRIDE);
        if (override != null) {
            return TerrariaRarity.fromLevel(override);
        }
        return calculateEffectiveRarity(getPrefix(stack));
    }
}
