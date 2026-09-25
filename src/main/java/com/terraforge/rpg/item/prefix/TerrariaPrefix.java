package com.terraforge.rpg.item.prefix;

/**
 * Representation of a canonical Terraria 1.4.5.8 prefix / modifier.
 */
public record TerrariaPrefix(
        String id,
        String displayName,
        TerrariaPrefixCategory category,
        double damageModifier,      // e.g. +0.15 (+15%)
        double speedModifier,       // e.g. +0.10 (+10% use speed)
        double critChanceBonus,     // e.g. +5.0 (+5%)
        double knockbackModifier,   // e.g. +0.15 (+15%)
        double manaCostModifier,    // e.g. -0.10 (-10% cost)
        double sizeModifier,        // e.g. +0.10 (+10% reach/size)
        double velocityModifier,    // e.g. +0.10 (+10% projectile speed)
        int defenseBonus,           // e.g. +4 for Warding
        double valueMultiplier,     // e.g. 1.60 for Legendary
        int rarityTierOffset        // e.g. +2 for best tiers, -1 for Broken
) {
    public static final TerrariaPrefix NONE = new TerrariaPrefix(
            "none", "", TerrariaPrefixCategory.UNIVERSAL,
            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 1.0, 0);

    public boolean isNone() {
        return "none".equalsIgnoreCase(id) || id.isEmpty();
    }
}
