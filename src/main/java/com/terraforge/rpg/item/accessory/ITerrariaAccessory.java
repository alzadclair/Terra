package com.terraforge.rpg.item.accessory;

import com.terraforge.rpg.item.ITerrariaItem;
import com.terraforge.rpg.item.prefix.TerrariaPrefixCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Interface contract for standard Terraria accessories.
 */
public interface ITerrariaAccessory extends ITerrariaItem {

    @Override
    default TerrariaPrefixCategory getPrefixCategory() {
        return TerrariaPrefixCategory.ACCESSORY;
    }

    void onEquipped(Player player, ItemStack stack);

    void onUnequipped(Player player, ItemStack stack);

    void tickAccessory(Player player, ItemStack stack);
}
