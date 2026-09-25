package com.terraforge.rpg.armor;

import com.terraforge.rpg.combat.DamageClass;

/**
 * Canonical Terraria 1.4.5.8 armor set bonuses.
 */
public enum ArmorSetBonus {
    NONE("None", 0, 0.0, DamageClass.GENERIC, "No set bonus."),
    COPPER("Copper", 2, 0.0, DamageClass.GENERIC, "+2 Defense"),
    IRON("Iron", 2, 0.0, DamageClass.GENERIC, "+2 Defense"),
    GOLD("Gold", 3, 0.0, DamageClass.GENERIC, "+3 Defense"),
    SHADOW("Shadow", 0, 0.0, DamageClass.MELEE, "+15% Movement speed and rapid attack acceleration"),
    CRIMSON("Crimson", 0, 0.18, DamageClass.GENERIC, "Greatly increased life regeneration and +18% damage"),
    METEOR("Meteor", 0, 0.27, DamageClass.MAGIC, "Space and magic weapon mana cost reduced to 0, +27% magic damage"),
    JUNGLE("Jungle", 0, 0.0, DamageClass.MAGIC, "-16% Mana usage, +80 Max Mana, +12% magic critical strike chance"),
    NECRO("Necro", 0, 0.15, DamageClass.RANGED, "20% chance to not consume ammo, +15% ranged damage"),
    MOLTEN("Molten", 0, 0.17, DamageClass.MELEE, "Cannot be set on fire and +17% melee damage"),
    HALLOWED("Hallowed", 0, 0.10, DamageClass.GENERIC, "Holy Protection: enables shadow dodge against attacks");

    private final String displayName;
    private final int bonusDefense;
    private final double damageMultiplierBonus;
    private final DamageClass targetClass;
    private final String description;

    ArmorSetBonus(String displayName, int bonusDefense, double damageMultiplierBonus, DamageClass targetClass, String description) {
        this.displayName = displayName;
        this.bonusDefense = bonusDefense;
        this.damageMultiplierBonus = damageMultiplierBonus;
        this.targetClass = targetClass;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public int getBonusDefense() { return bonusDefense; }
    public double getDamageMultiplierBonus() { return damageMultiplierBonus; }
    public DamageClass getTargetClass() { return targetClass; }
    public String getDescription() { return description; }
}
