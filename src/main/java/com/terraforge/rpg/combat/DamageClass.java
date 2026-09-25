package com.terraforge.rpg.combat;

/**
 * Terraria 1.4.5.8 weapon/damage classes.
 */
public enum DamageClass {
    MELEE("Melee", true),
    RANGED("Ranged", true),
    MAGIC("Magic", false),
    SUMMON("Summon", false),
    TRUE("True", false),
    GENERIC("Generic", true);

    private final String displayName;
    private final boolean physicalDefault;

    DamageClass(String displayName, boolean physicalDefault) {
        this.displayName = displayName;
        this.physicalDefault = physicalDefault;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isPhysicalDefault() {
        return physicalDefault;
    }
}
