package com.terraforge.rpg.experience;

import com.terraforge.rpg.config.TerraServerConfig;

/**
 * Standard implementation of ExperienceCurve:
 * requiredXP(level) = baseXP * level^exponent * raceXpMultiplier
 */
public final class DefaultExperienceCurve implements ExperienceCurve {
    private final double fallbackBaseXp;
    private final double fallbackExponent;

    public DefaultExperienceCurve() {
        this(100.0, 1.35);
    }

    public DefaultExperienceCurve(double fallbackBaseXp, double fallbackExponent) {
        this.fallbackBaseXp = fallbackBaseXp;
        this.fallbackExponent = fallbackExponent;
    }

    @Override
    public double getRequiredXp(int level, double raceXpMultiplier) {
        int lvl = Math.max(1, level);
        double multiplier = Math.max(0.1, raceXpMultiplier);

        double base = fallbackBaseXp;
        double exponent = fallbackExponent;

        try {
            if (TerraServerConfig.BASE_XP != null && TerraServerConfig.BASE_XP.get() != null) {
                base = TerraServerConfig.BASE_XP.get();
            }
            if (TerraServerConfig.XP_EXPONENT != null && TerraServerConfig.XP_EXPONENT.get() != null) {
                exponent = TerraServerConfig.XP_EXPONENT.get();
            }
        } catch (Exception ignored) {
            // Use constructor fallback values during early initialization or unit testing
        }

        return base * Math.pow(lvl, exponent) * multiplier;
    }
}
