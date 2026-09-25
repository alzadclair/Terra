package com.terraforge.rpg.item.weapon.yoyo;

import com.terraforge.rpg.item.weapon.ITerrariaWeapon;

/**
 * Interface contract for Terraria Yoyos.
 */
public interface ITerrariaYoyo extends ITerrariaWeapon {

    /**
     * Maximum reach distance from the player in blocks.
     */
    double getReach();

    /**
     * Maximum flight time in ticks before retracting (e.g. 80 ticks = 4 seconds for Wooden Yoyo, -1 for infinite).
     */
    int getFlightTime();

    /**
     * Color of the yoyo string in RGB.
     */
    int getStringColor();
}
