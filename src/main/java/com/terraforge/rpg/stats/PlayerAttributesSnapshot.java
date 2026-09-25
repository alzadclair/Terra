package com.terraforge.rpg.stats;

import com.terraforge.rpg.player.data.PlayerRPGData;
import com.terraforge.rpg.race.RaceDefinition;
import com.terraforge.rpg.race.RaceRegistry;

/**
 * Breakdown of an individual attribute showing all sources (Base + Race + Invested + Gear + Buff = Final).
 */
public record PlayerAttributesSnapshot(
        AttributeType type,
        double baseValue,
        double raceBonus,
        int investedRank,
        double gearBonus,
        double buffBonus,
        double finalValue,
        int effectiveCap,
        boolean uncapped
) {
    public static PlayerAttributesSnapshot compute(PlayerRPGData data, AttributeType type) {
        int invested = StatService.getRank(data, type.getId());
        double base = 10.0;
        double raceBonus = getRaceBaseBonus(data, type);
        double gearBonus = 0.0; // Integrated in Phase 8 & 11
        double buffBonus = 0.0; // Integrated in Phase 9 & 10
        double finalVal = base + raceBonus + invested + gearBonus + buffBonus;

        int cap = AttributeCapRegistry.getEffectiveCap(data, type);
        boolean isUncapped = AttributeCapRegistry.isEvolutionUncapped(data);

        return new PlayerAttributesSnapshot(
                type,
                base,
                raceBonus,
                invested,
                gearBonus,
                buffBonus,
                finalVal,
                cap,
                isUncapped
        );
    }

    private static double getRaceBaseBonus(PlayerRPGData data, AttributeType type) {
        double primary = RaceRegistry.get(data.getPrimaryRace())
                .map(r -> getStatValue(r, type))
                .orElse(10.0);

        if (!data.isHybrid() || data.getSecondaryRace().isEmpty()) {
            return primary;
        }

        double secondary = RaceRegistry.get(data.getSecondaryRace())
                .map(r -> getStatValue(r, type))
                .orElse(10.0);

        return (primary + secondary) / 2.0;
    }

    private static double getStatValue(RaceDefinition def, AttributeType type) {
        return switch (type) {
            case DEFENSE -> def.baseDefense();
            case MAGIC_DEFENSE -> def.baseMagicDefense();
            case ATTACK -> def.baseAttack();
            case MAGIC_ATTACK -> def.baseMagicAttack();
            case CRITICAL -> def.baseCritical();
            case CRITICAL_CHANCE -> def.baseCriticalChance();
            case SPEED -> def.baseSpeed();
        };
    }
}
