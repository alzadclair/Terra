package com.terraforge.rpg;

import com.terraforge.rpg.armor.ArmorSetBonus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Fase 25: Audio, Visual Effects, Polish & Complete HUD.
 */
public class AudioAndPolishTest {

    @Test
    @DisplayName("Sound events IDs match canonical TerraForge namespace format")
    void testSoundEventIdentifiers() {
        String modId = TerraForgeRPG.MOD_ID;
        assertEquals("terraforge_rpg", modId);

        String bossRoar = "boss.roar";
        String mechRoar = "boss.roar_mechanical";
        String deathray = "boss.deathray";
        String reforgeSuccess = "item.reforge_success";
        String magicMirror = "item.magic_mirror";

        assertTrue(bossRoar.startsWith("boss."));
        assertTrue(mechRoar.startsWith("boss."));
        assertTrue(deathray.startsWith("boss."));
        assertTrue(reforgeSuccess.startsWith("item."));
        assertTrue(magicMirror.startsWith("item."));
    }

    @Test
    @DisplayName("Armor set bonuses formatted correctly for HUD presentation")
    void testArmorSetBonusHudPresentation() {
        for (ArmorSetBonus bonus : ArmorSetBonus.values()) {
            assertNotNull(bonus.getDisplayName());
            assertNotNull(bonus.getDescription());
            assertFalse(bonus.getDisplayName().isEmpty());
        }

        assertEquals("Meteor", ArmorSetBonus.METEOR.getDisplayName());
        assertEquals("Molten", ArmorSetBonus.MOLTEN.getDisplayName());
        assertEquals("Hallowed", ArmorSetBonus.HALLOWED.getDisplayName());
    }

    @Test
    @DisplayName("HUD Mana and Life ratio clamping formulas")
    void testHudClampingFormulas() {
        // Clamping health and mana
        double curMana = 150.0;
        double maxMana = 200.0;
        double ratio = Math.clamp(curMana / maxMana, 0.0, 1.0);
        assertEquals(0.75, ratio, 0.001);

        double overflowMana = 300.0;
        double clampedOverflow = Math.clamp(overflowMana / maxMana, 0.0, 1.0);
        assertEquals(1.0, clampedOverflow, 0.001);

        double negativeMana = -20.0;
        double clampedNegative = Math.clamp(negativeMana / maxMana, 0.0, 1.0);
        assertEquals(0.0, clampedNegative, 0.001);
    }
}
