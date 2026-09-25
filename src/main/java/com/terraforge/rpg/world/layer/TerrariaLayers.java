package com.terraforge.rpg.world.layer;

/**
 * Vertical continuous layer definitions for the Terraria Realm dimension.
 * Space (Y >= 260), Surface (120..259), Underground (40..119), Cavern (-30..39), Underworld (Y < -30).
 */
public enum TerrariaLayers {
    SPACE(260, 320, "layer.terraforge_rpg.space", 0.015),
    SURFACE(120, 259, "layer.terraforge_rpg.surface", 0.08),
    UNDERGROUND(40, 119, "layer.terraforge_rpg.underground", 0.08),
    CAVERN(-30, 39, "layer.terraforge_rpg.cavern", 0.08),
    UNDERWORLD(-64, -31, "layer.terraforge_rpg.underworld", 0.08);

    private final int minY;
    private final int maxY;
    private final String translationKey;
    private final double gravity;

    TerrariaLayers(int minY, int maxY, String translationKey, double gravity) {
        this.minY = minY;
        this.maxY = maxY;
        this.translationKey = translationKey;
        this.gravity = gravity;
    }

    public int getMinY() { return minY; }
    public int getMaxY() { return maxY; }
    public String getTranslationKey() { return translationKey; }
    public double getGravity() { return gravity; }

    public static TerrariaLayers getLayer(int y) {
        if (y >= 260) return SPACE;
        if (y >= 120) return SURFACE;
        if (y >= 40) return UNDERGROUND;
        if (y >= -30) return CAVERN;
        return UNDERWORLD;
    }
}
