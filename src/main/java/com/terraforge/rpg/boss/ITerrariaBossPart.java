package com.terraforge.rpg.boss;

/**
 * Contract for sub-entities or body segments of multi-part Terraria bosses.
 */
public interface ITerrariaBossPart {

    ITerrariaBoss getParentBoss();

    int getPartIndex();

    boolean routesDamageToParent();

    double getDamageTransferRatio();
}
