package com.terraforge.rpg.item.mount;

import com.terraforge.rpg.item.ITerrariaItem;

/**
 * Interface for Terraria mount summon items.
 */
public interface ITerrariaMount extends ITerrariaItem {

    double getMountSpeed();

    double getJumpHeightBoost();

    boolean hasFallDamageImmunity();

    boolean canFloatOnLiquids();
}
