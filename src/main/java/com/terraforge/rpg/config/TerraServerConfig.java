package com.terraforge.rpg.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Server-authoritative gameplay configuration for TerraForge RPG.
 */
public final class TerraServerConfig {
    public static final ModConfigSpec SPEC;

    // Level & XP
    public static final ModConfigSpec.IntValue MAX_LEVEL;
    public static final ModConfigSpec.IntValue POINTS_PER_LEVEL;
    public static final ModConfigSpec.IntValue START_LEVEL;
    public static final ModConfigSpec.BooleanValue GIVE_POINTS_AT_LEVEL_ONE;
    public static final ModConfigSpec.DoubleValue BASE_XP;
    public static final ModConfigSpec.DoubleValue XP_EXPONENT;
    public static final ModConfigSpec.DoubleValue GLOBAL_XP_MULTIPLIER;

    // Race & Hybrid
    public static final ModConfigSpec.DoubleValue HYBRID_CHANCE;
    public static final ModConfigSpec.DoubleValue HYBRID_PENALTY;
    public static final ModConfigSpec.BooleanValue NATURAL_FLIGHT_ENABLED;
    public static final ModConfigSpec.BooleanValue EVOLUTION_ENABLED;
    public static final ModConfigSpec.BooleanValue RACE_REROLL_ALLOWED;

    // Special Accessories
    public static final ModConfigSpec.BooleanValue SPECIAL_ACCESSORY_ENABLED;
    public static final ModConfigSpec.BooleanValue PHOENIX_WINGS_ENABLED;

    // Status Points & Kills
    public static final ModConfigSpec.DoubleValue KILL_POINT_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue FIRST_BOSS_KILL_BONUS;
    public static final ModConfigSpec.DoubleValue REPEAT_BOSS_KILL_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue WEAK_MOB_PENALTY;

    // Combat & Scaling
    public static final ModConfigSpec.BooleanValue OVERCRIT_ENABLED;
    public static final ModConfigSpec.DoubleValue BOSS_HEALTH_SCALING;
    public static final ModConfigSpec.DoubleValue BOSS_DAMAGE_SCALING;
    public static final ModConfigSpec.DoubleValue DROP_RATE_MULTIPLIER;

    // World & Progression
    public static final ModConfigSpec.ConfigValue<String> WORLD_EVIL_MODE;
    public static final ModConfigSpec.ConfigValue<String> DIFFICULTY_RULE_SET;
    public static final ModConfigSpec.BooleanValue HARDMODE_DEFAULT_ENABLED;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("RPG Level and Progression Settings").push("level_and_progression");
        MAX_LEVEL = builder.comment("Maximum character level achievable before Evolution potential.")
                .defineInRange("maxLevel", 1000, 1, 100000);
        POINTS_PER_LEVEL = builder.comment("Status points awarded per level up.")
                .defineInRange("pointsPerLevel", 5, 1, 100);
        START_LEVEL = builder.comment("Starting level for newly spawned players.")
                .defineInRange("startLevel", 1, 1, 100);
        GIVE_POINTS_AT_LEVEL_ONE = builder.comment("Award points upon reaching level 1.")
                .define("givePointsAtLevelOne", true);
        BASE_XP = builder.comment("Base XP multiplier for the experience curve.")
                .defineInRange("baseXP", 100.0, 1.0, 100000.0);
        XP_EXPONENT = builder.comment("Exponent factor for required XP curve.")
                .defineInRange("xpExponent", 1.35, 1.0, 3.0);
        GLOBAL_XP_MULTIPLIER = builder.comment("Global experience gain multiplier.")
                .defineInRange("globalXpMultiplier", 1.0, 0.01, 100.0);
        builder.pop();

        builder.comment("Race and Hybrid Settings").push("races");
        HYBRID_CHANCE = builder.comment("Probability of being born as a Hybrid race (0.01 = 1%).")
                .defineInRange("hybridChance", 0.01, 0.0, 1.0);
        HYBRID_PENALTY = builder.comment("Additional XP penalty multiplier added to hybrid races.")
                .defineInRange("hybridPenalty", 0.10, 0.0, 2.0);
        NATURAL_FLIGHT_ENABLED = builder.comment("Allow winged races to fly naturally.")
                .define("naturalFlightEnabled", true);
        EVOLUTION_ENABLED = builder.comment("Allow Human and Human-hybrid races to progress infinitely past level 1000.")
                .define("evolutionEnabled", true);
        RACE_REROLL_ALLOWED = builder.comment("Allow survival players to reroll races via standard mechanics.")
                .define("raceRerollAllowed", false);
        builder.pop();

        builder.comment("Special Accessories Settings").push("special_accessories");
        SPECIAL_ACCESSORY_ENABLED = builder.comment("Enable the unique Special Accessory slot in the K menu.")
                .define("specialAccessoryEnabled", true);
        PHOENIX_WINGS_ENABLED = builder.comment("Enable creative flight when Phoenix Wings are equipped.")
                .define("phoenixWingsEnabled", true);
        builder.pop();

        builder.comment("Kill Point Rewards").push("kill_rewards");
        KILL_POINT_MULTIPLIER = builder.comment("Multiplier for fractional status points gained from entity kills.")
                .defineInRange("killPointMultiplier", 1.0, 0.0, 50.0);
        FIRST_BOSS_KILL_BONUS = builder.comment("Bonus fractional points awarded on defeating a boss for the first time.")
                .defineInRange("firstBossKillBonus", 50.0, 0.0, 500.0);
        REPEAT_BOSS_KILL_MULTIPLIER = builder.comment("Multiplier for points on repeat boss kills.")
                .defineInRange("repeatBossKillMultiplier", 1.0, 0.0, 10.0);
        WEAK_MOB_PENALTY = builder.comment("Fractional progress awarded for extremely weak mobs.")
                .defineInRange("weakMobPenalty", 0.10, 0.0, 1.0);
        builder.pop();

        builder.comment("Combat Engine and Scaling").push("combat");
        OVERCRIT_ENABLED = builder.comment("Allow critical chance above 100% to roll for secondary tier critical hits.")
                .define("overcritEnabled", false);
        BOSS_HEALTH_SCALING = builder.comment("Multiplayer boss health scaling multiplier per additional player.")
                .defineInRange("bossHealthScaling", 0.35, 0.0, 5.0);
        BOSS_DAMAGE_SCALING = builder.comment("Multiplayer boss damage scaling multiplier.")
                .defineInRange("bossDamageScaling", 1.0, 0.1, 5.0);
        DROP_RATE_MULTIPLIER = builder.comment("Global drop rate multiplier.")
                .defineInRange("dropRateMultiplier", 1.0, 0.1, 20.0);
        builder.pop();

        builder.comment("World and Progression").push("world");
        WORLD_EVIL_MODE = builder.comment("World evil type: CORRUPTION, CRIMSON, or BOTH.")
                .define("worldEvilMode", "CORRUPTION_OR_CRIMSON");
        DIFFICULTY_RULE_SET = builder.comment("Terraria difficulty ruleset: CLASSIC, EXPERT, or MASTER.")
                .define("difficultyRuleSet", "CLASSIC");
        HARDMODE_DEFAULT_ENABLED = builder.comment("Start world with Hardmode unlocked immediately (Testing only).")
                .define("hardmodeDefaultEnabled", false);
        builder.pop();

        SPEC = builder.build();
    }

    private TerraServerConfig() {}
}
