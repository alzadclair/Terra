package com.terraforge.rpg.race;

/**
 * Data definition record for a race.
 */
public record RaceDefinition(
        String id,
        String displayNameKey,
        double xpMultiplier,
        boolean naturalFlight,
        String abilityId,
        double baseDefense,
        double baseMagicDefense,
        double baseAttack,
        double baseMagicAttack,
        double baseCritical,
        double baseCriticalChance,
        double baseSpeed
) {}
