package com.terraforge.rpg.armor;

import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.item.ITerrariaItem;
import com.terraforge.rpg.item.rarity.TerrariaRarity;
import net.minecraft.world.entity.EquipmentSlot;

/**
 * Interface defining canonical Terraria 1.4.5.8 armor piece properties.
 */
public interface ITerrariaArmor extends ITerrariaItem {

    String getSetId();

    EquipmentSlot getEquipmentSlot();

    int getTerrariaDefense();

    DamageClass getArmorClass();

    @Override
    TerrariaRarity getBaseRarity();

    @Override
    long getBaseValue();
}
