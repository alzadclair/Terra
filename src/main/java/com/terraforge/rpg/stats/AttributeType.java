package com.terraforge.rpg.stats;

/**
 * The 7 primary distributable RPG attributes of TerraForge RPG.
 * Life and Mana are deliberately excluded and managed through dedicated Terraria systems.
 */
public enum AttributeType {
    DEFENSE("defense", "terraforge_rpg.stat.defense", 800),
    MAGIC_DEFENSE("magic_defense", "terraforge_rpg.stat.magic_defense", 800),
    ATTACK("attack", "terraforge_rpg.stat.attack", 1000),
    MAGIC_ATTACK("magic_attack", "terraforge_rpg.stat.magic_attack", 1000),
    CRITICAL("critical", "terraforge_rpg.stat.critical", 600),
    CRITICAL_CHANCE("critical_chance", "terraforge_rpg.stat.critical_chance", 500),
    SPEED("speed", "terraforge_rpg.stat.speed", 500);

    private final String id;
    private final String translationKey;
    private final int baseGlobalCap;

    AttributeType(String id, String translationKey, int baseGlobalCap) {
        this.id = id;
        this.translationKey = translationKey;
        this.baseGlobalCap = baseGlobalCap;
    }

    public String getId() {
        return id;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public int getBaseGlobalCap() {
        return baseGlobalCap;
    }

    public static AttributeType fromId(String id) {
        if (id == null) return null;
        for (AttributeType type : values()) {
            if (type.id.equalsIgnoreCase(id) || type.name().equalsIgnoreCase(id)) {
                return type;
            }
        }
        return null;
    }
}
