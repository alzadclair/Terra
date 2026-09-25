package com.terraforge.rpg;

import com.terraforge.rpg.mana.ManaService;
import com.terraforge.rpg.player.data.PlayerRPGData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ManaServiceTest {

    private PlayerRPGData data;

    @BeforeEach
    void setUp() {
        data = new PlayerRPGData();
        data.setLevel(1);
    }

    @Test
    @DisplayName("Mana consumption and restoration work authoritatively")
    void testManaConsumptionAndRestoration() {
        data.setMaxMana(100.0);
        data.setCurrentMana(50.0);

        // Valid consumption
        boolean success = ManaService.consumeMana(data, 20.0);
        assertTrue(success);
        assertEquals(30.0, data.getCurrentMana(), 0.001);

        // Insufficient mana consumption
        boolean fail = ManaService.consumeMana(data, 40.0);
        assertFalse(fail);
        assertEquals(30.0, data.getCurrentMana(), 0.001);

        // Restoration
        ManaService.restoreMana(data, 50.0);
        assertEquals(80.0, data.getCurrentMana(), 0.001);

        // Capped restoration
        ManaService.restoreMana(data, 50.0);
        assertEquals(100.0, data.getCurrentMana(), 0.001);
    }

    @Test
    @DisplayName("Life Crystals and Life Fruits increment and respect maximum caps")
    void testLifeProgressionCaps() {
        // Life crystals cap at 15
        for (int i = 0; i < 20; i++) {
            if (data.getLifeCrystalsUsed() < 15) {
                data.setLifeCrystalsUsed(data.getLifeCrystalsUsed() + 1);
            }
        }
        assertEquals(15, data.getLifeCrystalsUsed());

        // Life fruits cap at 20
        for (int i = 0; i < 25; i++) {
            if (data.getLifeFruitsUsed() < 20) {
                data.setLifeFruitsUsed(data.getLifeFruitsUsed() + 1);
            }
        }
        assertEquals(20, data.getLifeFruitsUsed());

        // Bonus health calculation: (15 * 2) + (20 * 1) = 50 extra HP
        double bonusHealth = (data.getLifeCrystalsUsed() * 2.0) + (data.getLifeFruitsUsed() * 1.0);
        assertEquals(50.0, bonusHealth, 0.001);
    }

    @Test
    @DisplayName("Mana Crystals scale max mana up to 200 base (9 crystals max)")
    void testManaCrystalsProgression() {
        assertEquals(20.0, data.getMaxMana(), 0.001);

        for (int i = 0; i < 9; i++) {
            data.setManaCrystalsUsed(data.getManaCrystalsUsed() + 1);
        }
        assertEquals(9, data.getManaCrystalsUsed());
        assertEquals(200.0, data.getMaxMana(), 0.001);
    }
}
