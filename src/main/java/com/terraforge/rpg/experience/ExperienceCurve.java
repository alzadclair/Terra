package com.terraforge.rpg.experience;

/**
 * Strategy interface for calculating experience thresholds.
 */
public interface ExperienceCurve {
    /**
     * Calculates the experience required to advance from the given level to level + 1.
     *
     * @param level Current level
     * @param raceXpMultiplier Multiplier determined by player's race or hybrid status
     * @return Total experience required for this level threshold
     */
    double getRequiredXp(int level, double raceXpMultiplier);
}
