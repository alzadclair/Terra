package com.terraforge.rpg.boss;

import com.terraforge.rpg.entity.mob.ITerrariaMob;

/**
 * Common contract for all Terraria bosses.
 */
public interface ITerrariaBoss extends ITerrariaMob {

    String getBossId();

    BossPhase getCurrentPhase();

    void setPhase(BossPhase phase);

    boolean isEnraged();

    int getArenaRadius();
}
