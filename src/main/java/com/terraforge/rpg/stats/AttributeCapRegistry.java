package com.terraforge.rpg.stats;

import com.terraforge.rpg.config.TerraServerConfig;
import com.terraforge.rpg.player.data.PlayerRPGData;

/**
 * Evaluates effective attribute caps considering base limits, race modifiers,
 * hybrid status, and post-1000 Evolution rules.
 */
public final class AttributeCapRegistry {

    private AttributeCapRegistry() {}

    /**
     * Determines whether the player can invest additional points into this attribute.
     */
    public static boolean canInvest(PlayerRPGData data, AttributeType type, int additionalPoints) {
        if (additionalPoints <= 0) return false;

        // Evolution post-1000 ignores caps
        if (isEvolutionUncapped(data)) {
            return true;
        }

        int currentRank = StatService.getRank(data, type.getId());
        int cap = getEffectiveCap(data, type);
        return currentRank + additionalPoints <= cap;
    }

    /**
     * Checks if the player has Evolution and is at or above level 1000.
     */
    public static boolean isEvolutionUncapped(PlayerRPGData data) {
        int maxLevel = 1000;
        try {
            if (TerraServerConfig.MAX_LEVEL != null && TerraServerConfig.MAX_LEVEL.get() != null) {
                maxLevel = TerraServerConfig.MAX_LEVEL.get();
            }
        } catch (Exception ignored) {}

        return data.hasEvolution() && data.getLevel() >= maxLevel;
    }

    /**
     * Returns the maximum rank cap for the specified attribute.
     * Returns Integer.MAX_VALUE if Evolution bypasses the cap.
     */
    public static int getEffectiveCap(PlayerRPGData data, AttributeType type) {
        if (isEvolutionUncapped(data)) {
            return Integer.MAX_VALUE;
        }

        int baseCap = type.getBaseGlobalCap();
        double multiplier = getRaceCapMultiplier(data.getPrimaryRace(), type);

        if (data.isHybrid() && !data.getSecondaryRace().isEmpty()) {
            double secondaryMultiplier = getRaceCapMultiplier(data.getSecondaryRace(), type);
            multiplier = (multiplier + secondaryMultiplier) / 2.0;
        }

        return (int) Math.round(baseCap * multiplier);
    }

    /**
     * Returns race-specific cap multiplier for a given attribute.
     * Archetype specialization:
     * - Dwarf/Golem: Higher Defense caps
     * - Titan/Orc: Higher Attack caps
     * - Aetherian/Elf: Higher Magic Attack caps
     * - Harpy/Stormborn: Higher Speed caps
     */
    public static double getRaceCapMultiplier(String raceId, AttributeType type) {
        if (raceId == null) return 1.0;
        String r = raceId.toLowerCase();

        return switch (type) {
            case DEFENSE -> switch (r) {
                case "dwarf", "golem" -> 1.30;
                case "titan" -> 1.20;
                case "fairy", "spectre" -> 0.75;
                default -> 1.00;
            };
            case MAGIC_DEFENSE -> switch (r) {
                case "frostborn", "aetherian", "angel" -> 1.25;
                case "golem" -> 1.15;
                case "orc" -> 0.70;
                default -> 1.00;
            };
            case ATTACK -> switch (r) {
                case "orc", "titan" -> 1.30;
                case "reaper", "beastman" -> 1.20;
                case "fairy", "slime" -> 0.70;
                default -> 1.00;
            };
            case MAGIC_ATTACK -> switch (r) {
                case "aetherian", "elf" -> 1.30;
                case "demon", "angel", "astral" -> 1.25;
                case "dwarf", "orc", "golem" -> 0.65;
                default -> 1.00;
            };
            case CRITICAL -> switch (r) {
                case "reaper", "shadowborn" -> 1.25;
                case "astral" -> 1.20;
                default -> 1.00;
            };
            case CRITICAL_CHANCE -> switch (r) {
                case "shadowborn", "beastman", "reaper" -> 1.20;
                case "golem", "titan" -> 0.80;
                default -> 1.00;
            };
            case SPEED -> switch (r) {
                case "harpy", "stormborn" -> 1.25;
                case "fairy", "shadowborn" -> 1.15;
                case "golem", "titan" -> 0.70;
                default -> 1.00;
            };
        };
    }
}
