package com.terraforge.rpg;

import com.terraforge.rpg.item.fishing.FishingPowerCalculator;
import com.terraforge.rpg.item.fishing.TerrariaCrateItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FishingMountHookTest {

    @Test
    @DisplayName("FishingPowerCalculator time of day multipliers")
    void testTimeModifiers() {
        // Dawn: 23000 ticks -> 1.30
        assertEquals(1.30, FishingPowerCalculator.getTimeModifier(23000L), 0.001);
        // Dusk: 13000 ticks -> 1.30
        assertEquals(1.30, FishingPowerCalculator.getTimeModifier(13000L), 0.001);
        // Noon: 6000 ticks -> 0.80
        assertEquals(0.80, FishingPowerCalculator.getTimeModifier(6000L), 0.001);
        // Midnight: 18000 ticks -> 0.80
        assertEquals(0.80, FishingPowerCalculator.getTimeModifier(18000L), 0.001);
        // Regular Morning: 2000 ticks -> 1.00
        assertEquals(1.00, FishingPowerCalculator.getTimeModifier(2000L), 0.001);
    }

    @Test
    @DisplayName("FishingPowerCalculator weather and pool size multipliers")
    void testWeatherAndPoolModifiers() {
        assertEquals(1.20, FishingPowerCalculator.getWeatherModifier(true), 0.001);
        assertEquals(1.00, FishingPowerCalculator.getWeatherModifier(false), 0.001);

        // Pool < 75 tiles -> 0.0
        assertEquals(0.0, FishingPowerCalculator.getPoolSizeModifier(50), 0.001);
        // Pool 150 tiles -> 150/300 = 0.50
        assertEquals(0.50, FishingPowerCalculator.getPoolSizeModifier(150), 0.001);
        // Pool 300+ tiles -> 1.00
        assertEquals(1.00, FishingPowerCalculator.getPoolSizeModifier(300), 0.001);
        assertEquals(1.00, FishingPowerCalculator.getPoolSizeModifier(500), 0.001);
    }

    @Test
    @DisplayName("FishingPowerCalculator total canonical calculation")
    void testTotalFishingPowerCalculation() {
        // Golden Fishing Rod (50) + Master Bait (50) = 100 base
        // Dawn (1.30) + Rain (1.20) + 300 water tiles (1.0)
        // 100 * 1.30 * 1.20 = 156
        int total = FishingPowerCalculator.calculateTotalFishingPower(50, 50, 0, 23000L, true, 300);
        assertEquals(156, total);

        // Sub-75 pool tiles cannot catch anything
        int zeroPower = FishingPowerCalculator.calculateTotalFishingPower(50, 50, 0, 23000L, true, 50);
        assertEquals(0, zeroPower);
    }

    @Test
    @DisplayName("Terraria Crate coin yield specifications")
    void testCrateTiers() {
        assertEquals(50, TerrariaCrateItem.CrateTier.WOODEN.getMinCoins());
        assertEquals(150, TerrariaCrateItem.CrateTier.WOODEN.getMaxCoins());

        assertEquals(200, TerrariaCrateItem.CrateTier.IRON.getMinCoins());
        assertEquals(800, TerrariaCrateItem.CrateTier.IRON.getMaxCoins());

        assertEquals(1000, TerrariaCrateItem.CrateTier.GOLDEN.getMinCoins());
        assertEquals(5000, TerrariaCrateItem.CrateTier.GOLDEN.getMaxCoins());
    }
}
