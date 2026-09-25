package com.terraforge.rpg.item.ammo;

import com.terraforge.rpg.item.ITerrariaItem;
import com.terraforge.rpg.item.prefix.TerrariaPrefixCategory;

/**
 * Interface contract for Terraria ammunition items.
 */
public interface ITerrariaAmmo extends ITerrariaItem {

    AmmoType getAmmoType();

    double getBonusDamage();

    double getBonusVelocity();

    double getBonusKnockback();

    int getPiercingCount();

    int getBounceCount();

    @Override
    default TerrariaPrefixCategory getPrefixCategory() {
        return TerrariaPrefixCategory.UNIVERSAL;
    }
}
