package com.terraforge.rpg.entity.mob;

import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.experience.ThreatRating;

/**
 * Interface contract for all Terraria 1.4.5.8 monsters and hostile creatures.
 */
public interface ITerrariaMob {

    ThreatRating getThreatRating();

    long getMinCoinDrop();

    long getMaxCoinDrop();

    int getTerrariaDefense();

    DamageClass getAttackDamageClass();
}
