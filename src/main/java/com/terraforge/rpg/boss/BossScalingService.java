package com.terraforge.rpg.boss;

/**
 * Server-authoritative multiplayer scaling service for Terraria boss encounters.
 * Formula: BaseHealth * [1.0 + (ActivePlayers - 1) * 0.35]
 */
public final class BossScalingService {

    public static final double PLAYER_SCALING_COEFFICIENT = 0.35;
    public static final double MAX_SCALING_MULTIPLIER = 4.0; // Caps at ~10 players

    private BossScalingService() {}

    /**
     * Calculates the scaled boss health based on player count.
     */
    public static double calculateScaledHealth(double baseHealth, int activePlayers) {
        if (baseHealth <= 0.0) return 1.0;
        int players = Math.max(1, activePlayers);
        if (players == 1) return baseHealth;

        double multiplier = 1.0 + ((players - 1) * PLAYER_SCALING_COEFFICIENT);
        multiplier = Math.min(MAX_SCALING_MULTIPLIER, multiplier);

        return Math.round(baseHealth * multiplier);
    }
}
