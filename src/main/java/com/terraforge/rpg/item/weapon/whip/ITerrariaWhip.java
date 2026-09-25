package com.terraforge.rpg.item.weapon.whip;

import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.item.prefix.TerrariaPrefixCategory;
import com.terraforge.rpg.item.weapon.ITerrariaWeapon;

/**
 * Interface contract for Terraria summoner whips.
 */
public interface ITerrariaWhip extends ITerrariaWeapon {

    int getSummonTagDamage();

    double getReach();

    @Override
    default DamageClass getDamageClass() {
        return DamageClass.SUMMON;
    }

    @Override
    default TerrariaPrefixCategory getPrefixCategory() {
        return TerrariaPrefixCategory.MELEE;
    }
}
