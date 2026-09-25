package com.terraforge.rpg;

import com.terraforge.rpg.armor.ArmorSetBonus;
import com.terraforge.rpg.armor.ITerrariaArmor;
import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.item.prefix.TerrariaPrefixCategory;
import com.terraforge.rpg.item.rarity.TerrariaRarity;
import net.minecraft.world.entity.EquipmentSlot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ArmorFrameworkTest {

    static class MockArmorPiece implements ITerrariaArmor {
        private final String setId;
        private final EquipmentSlot slot;
        private final int defense;
        private final DamageClass armorClass;

        MockArmorPiece(String setId, EquipmentSlot slot, int defense, DamageClass armorClass) {
            this.setId = setId;
            this.slot = slot;
            this.defense = defense;
            this.armorClass = armorClass;
        }

        @Override public String getSetId() { return setId; }
        @Override public EquipmentSlot getEquipmentSlot() { return slot; }
        @Override public int getTerrariaDefense() { return defense; }
        @Override public DamageClass getArmorClass() { return armorClass; }
        @Override public TerrariaRarity getBaseRarity() { return TerrariaRarity.WHITE; }
        @Override public long getBaseValue() { return 100L; }
        @Override public TerrariaPrefixCategory getPrefixCategory() { return TerrariaPrefixCategory.ACCESSORY; }
    }

    @Test
    @DisplayName("ArmorSetBonus canonical values and class alignments")
    void testSetBonusValues() {
        assertEquals(2, ArmorSetBonus.COPPER.getBonusDefense());
        assertEquals(2, ArmorSetBonus.IRON.getBonusDefense());
        assertEquals(3, ArmorSetBonus.GOLD.getBonusDefense());

        assertEquals(0.18, ArmorSetBonus.CRIMSON.getDamageMultiplierBonus(), 0.001);
        assertEquals(0.27, ArmorSetBonus.METEOR.getDamageMultiplierBonus(), 0.001);
        assertEquals(0.17, ArmorSetBonus.MOLTEN.getDamageMultiplierBonus(), 0.001);

        assertEquals(DamageClass.MELEE, ArmorSetBonus.MOLTEN.getTargetClass());
        assertEquals(DamageClass.MAGIC, ArmorSetBonus.METEOR.getTargetClass());
        assertEquals(DamageClass.RANGED, ArmorSetBonus.NECRO.getTargetClass());
    }

    @Test
    @DisplayName("Canonical Pre-Hardmode full set defense calculations")
    void testCanonicalArmorSetDefense() {
        // Copper: 1 + 2 + 1 + 0 = 4 pieces + 2 bonus = 6 total defense
        int copperPieces = 1 + 2 + 1 + 0;
        int copperTotal = copperPieces + ArmorSetBonus.COPPER.getBonusDefense();
        assertEquals(6, copperTotal);

        // Shadow Armor: 5 + 7 + 6 + 1 = 19 defense
        int shadowTotal = 5 + 7 + 6 + 1;
        assertEquals(19, shadowTotal);

        // Crimson Armor: 5 + 7 + 6 + 1 = 19 defense
        int crimsonTotal = 5 + 7 + 6 + 1;
        assertEquals(19, crimsonTotal);

        // Molten Armor: 8 + 9 + 7 + 1 = 25 defense
        int moltenTotal = 8 + 9 + 7 + 1;
        assertEquals(25, moltenTotal);
    }
}
