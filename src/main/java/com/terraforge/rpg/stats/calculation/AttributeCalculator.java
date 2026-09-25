package com.terraforge.rpg.stats.calculation;

/**
 * Centralized calculation engine converting attribute ranks into gameplay modifiers.
 * Prevents magic numbers from scattering across disparate classes.
 */
public final class AttributeCalculator {

    private AttributeCalculator() {}

    /**
     * Converts Attack Rank into a physical damage multiplier.
     * Formula: 1.0 + (rank * 0.0025) -> Rank 400 = +100% damage.
     */
    public static double calculatePhysicalDamageMultiplier(int attackRank) {
        int rank = Math.max(0, attackRank);
        return 1.0 + (rank * 0.0025);
    }

    /**
     * Converts Magic Attack Rank into a magic damage multiplier.
     * Formula: 1.0 + (rank * 0.0030) -> Rank 500 = +150% damage.
     */
    public static double calculateMagicDamageMultiplier(int magicAttackRank) {
        int rank = Math.max(0, magicAttackRank);
        return 1.0 + (rank * 0.0030);
    }

    /**
     * Converts Defense Rank into a percentage reduction for physical damage.
     * Asymptotic formula: rank / (rank + 400) -> Rank 400 = 50%, Rank 800 = 66.6%.
     */
    public static double calculatePhysicalDamageReduction(int defenseRank) {
        int rank = Math.max(0, defenseRank);
        if (rank == 0) return 0.0;
        return (double) rank / (rank + 400.0);
    }

    /**
     * Converts Magic Defense Rank into a percentage reduction for magic damage.
     * Asymptotic formula: rank / (rank + 400) -> Rank 400 = 50%, Rank 800 = 66.6%.
     */
    public static double calculateMagicDamageReduction(int magicDefenseRank) {
        int rank = Math.max(0, magicDefenseRank);
        if (rank == 0) return 0.0;
        return (double) rank / (rank + 400.0);
    }

    /**
     * Converts Critical Rank into a critical hit damage multiplier.
     * Formula: 1.5 + (rank * 0.0020) -> Rank 500 = 2.5x critical multiplier.
     */
    public static double calculateCriticalMultiplier(int criticalRank) {
        int rank = Math.max(0, criticalRank);
        return 1.5 + (rank * 0.0020);
    }

    /**
     * Converts Critical Chance Rank into bonus percentage chance.
     * Formula: rank * 0.15% -> Rank 500 = +75% crit chance.
     */
    public static double calculateCriticalChanceBonus(int criticalChanceRank) {
        int rank = Math.max(0, criticalChanceRank);
        return rank * 0.15;
    }

    /**
     * Converts Speed Rank into movement speed attribute modifier with diminishing returns.
     * Safe curve clamped to avoid chunk desynchronization and server physics crashes:
     * min(0.60, (rank / (rank + 500.0)) * 0.80)
     */
    public static double calculateMovementSpeedBonus(int speedRank) {
        int rank = Math.max(0, speedRank);
        if (rank == 0) return 0.0;
        double curve = ((double) rank / (rank + 500.0)) * 0.80;
        return Math.min(0.60, curve);
    }
}
