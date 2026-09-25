package com.terraforge.rpg.item.fishing;

/**
 * Calculates Terraria 1.4.5.8 canonical Fishing Power based on:
 * - Fishing Rod power
 * - Bait power
 * - Time of day (Dawn +30%, Dusk +30%, Noon -20%, Midnight -20%)
 * - Weather (Rain +20%)
 * - Pool size (penalty if under 300 tiles, impossible if under 75 tiles)
 */
public final class FishingPowerCalculator {

    private FishingPowerCalculator() {}

    /**
     * Calculates the time of day fishing modifier based on Minecraft dayTime (0..24000).
     */
    public static double getTimeModifier(long dayTime) {
        long time = dayTime % 24000L;

        // Dawn: 4:30 AM - 6:00 AM (22500..24000 / 0)
        if (time >= 22500L || time <= 500L) {
            return 1.30;
        }
        // Dusk: 6:00 PM - 7:30 PM (12000..13500)
        if (time >= 12000L && time <= 13500L) {
            return 1.30;
        }
        // Noon: 9:00 AM - 3:00 PM (3000..9000)
        if (time >= 3000L && time <= 9000L) {
            return 0.80;
        }
        // Midnight: 9:00 PM - 3:00 AM (15000..21000)
        if (time >= 15000L && time <= 21000L) {
            return 0.80;
        }

        return 1.0;
    }

    /**
     * Calculates weather modifier (Rain/Thunder +20%).
     */
    public static double getWeatherModifier(boolean isRaining) {
        return isRaining ? 1.20 : 1.0;
    }

    /**
     * Calculates pool size modifier.
     * Under 75 blocks: 0.0 (too small to fish).
     * 75 to 299 blocks: penalty ratio (pool / 300).
     * 300+ blocks: 1.0 (no penalty).
     */
    public static double getPoolSizeModifier(int poolTiles) {
        if (poolTiles < 75) {
            return 0.0;
        }
        if (poolTiles < 300) {
            return (double) poolTiles / 300.0;
        }
        return 1.0;
    }

    /**
     * Calculates total effective fishing power.
     */
    public static int calculateTotalFishingPower(
            int rodPower,
            int baitPower,
            int equipmentBonus,
            long dayTime,
            boolean isRaining,
            int poolTiles
    ) {
        double poolMod = getPoolSizeModifier(poolTiles);
        if (poolMod <= 0.0) {
            return 0;
        }

        double basePower = rodPower + baitPower + equipmentBonus;
        double timeMod = getTimeModifier(dayTime);
        double weatherMod = getWeatherModifier(isRaining);

        double total = basePower * timeMod * weatherMod * poolMod;
        return (int) Math.round(total);
    }
}
