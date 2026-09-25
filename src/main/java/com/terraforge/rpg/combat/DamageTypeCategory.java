package com.terraforge.rpg.combat;

/**
 * Categorization of damage used for armor mitigation and resistance calculation.
 */
public enum DamageTypeCategory {
    PHYSICAL,
    MAGICAL,
    TRUE_DAMAGE,
    ENVIRONMENTAL;

    public boolean isMitigatedByDefense() {
        return this == PHYSICAL;
    }

    public boolean isMitigatedByMagicDefense() {
        return this == MAGICAL;
    }

    public boolean bypassesAllDefense() {
        return this == TRUE_DAMAGE;
    }
}
