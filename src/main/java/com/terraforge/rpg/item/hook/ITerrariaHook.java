package com.terraforge.rpg.item.hook;

import com.terraforge.rpg.item.ITerrariaItem;

/**
 * Interface for all Terraria grappling hooks.
 */
public interface ITerrariaHook extends ITerrariaItem {

    double getHookRange();

    double getPullVelocity();

    int getMaxHooks();
}
