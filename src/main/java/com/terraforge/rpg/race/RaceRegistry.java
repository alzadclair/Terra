package com.terraforge.rpg.race;

import java.util.*;

/**
 * Registry and repository for all 30 canonical races in TerraForge RPG.
 */
public final class RaceRegistry {
    private static final Map<String, RaceDefinition> RACES = new LinkedHashMap<>();
    private static final List<String> ALL_RACE_IDS = new ArrayList<>();

    static {
        register(new RaceDefinition("human", "terraforge_rpg.race.human", 1.00, false, "evolution", 10, 10, 10, 10, 10, 5, 10));
        register(new RaceDefinition("goblin", "terraforge_rpg.race.goblin", 0.80, false, "goblin_craft", 6, 6, 8, 8, 12, 8, 12));
        register(new RaceDefinition("slime", "terraforge_rpg.race.slime", 0.78, false, "gelatinous_body", 5, 5, 6, 6, 5, 5, 10));
        register(new RaceDefinition("demon", "terraforge_rpg.race.demon", 1.50, true, "infernal_domain", 14, 16, 18, 18, 15, 8, 12));
        register(new RaceDefinition("angel", "terraforge_rpg.race.angel", 1.55, true, "celestial_grace", 15, 18, 12, 18, 12, 8, 12));
        register(new RaceDefinition("elf", "terraforge_rpg.race.elf", 1.05, false, "arcane_flow", 8, 14, 10, 16, 12, 10, 12));
        register(new RaceDefinition("dwarf", "terraforge_rpg.race.dwarf", 0.95, false, "mountain_heart", 18, 12, 14, 6, 10, 6, 8));
        register(new RaceDefinition("orc", "terraforge_rpg.race.orc", 1.00, false, "blood_fury", 14, 6, 18, 5, 14, 8, 10));
        register(new RaceDefinition("vampire", "terraforge_rpg.race.vampire", 1.20, false, "crimson_feast", 12, 12, 15, 14, 15, 10, 12));
        register(new RaceDefinition("werewolf", "terraforge_rpg.race.werewolf", 1.05, false, "lunar_frenzy", 14, 8, 16, 6, 14, 10, 14));
        register(new RaceDefinition("draconic", "terraforge_rpg.race.draconic", 1.40, true, "draconic_awakening", 16, 14, 16, 14, 14, 8, 12));
        register(new RaceDefinition("fairy", "terraforge_rpg.race.fairy", 1.15, true, "fae_leap", 6, 16, 6, 16, 10, 12, 16));
        register(new RaceDefinition("harpy", "terraforge_rpg.race.harpy", 1.10, true, "wind_sovereignty", 8, 10, 12, 8, 14, 12, 18));
        register(new RaceDefinition("triton", "terraforge_rpg.race.triton", 0.95, false, "tide_dominion", 12, 12, 12, 12, 10, 8, 12));
        register(new RaceDefinition("undead", "terraforge_rpg.race.undead", 1.00, false, "death_denial", 14, 12, 12, 10, 10, 6, 8));
        register(new RaceDefinition("golem", "terraforge_rpg.race.golem", 1.25, false, "living_fortress", 22, 18, 16, 5, 8, 4, 6));
        register(new RaceDefinition("shadowborn", "terraforge_rpg.race.shadowborn", 1.15, false, "shadow_step", 8, 10, 16, 12, 18, 14, 14));
        register(new RaceDefinition("stormborn", "terraforge_rpg.race.stormborn", 1.15, false, "overcharge", 10, 10, 14, 14, 12, 10, 16));
        register(new RaceDefinition("phoenix", "terraforge_rpg.race.phoenix", 1.45, true, "rebirth", 14, 16, 14, 16, 14, 10, 12));
        register(new RaceDefinition("frostborn", "terraforge_rpg.race.frostborn", 1.00, false, "absolute_zero", 12, 16, 10, 14, 10, 8, 10));
        register(new RaceDefinition("dryad", "terraforge_rpg.race.dryad", 0.95, false, "nature_pact", 10, 14, 10, 14, 10, 8, 10));
        register(new RaceDefinition("beastman", "terraforge_rpg.race.beastman", 0.95, false, "predator_instinct", 12, 8, 15, 6, 14, 12, 14));
        register(new RaceDefinition("enderian", "terraforge_rpg.race.enderian", 1.20, false, "rift_walker", 10, 12, 14, 14, 12, 10, 15));
        register(new RaceDefinition("voidborn", "terraforge_rpg.race.voidborn", 1.35, false, "devour_void", 15, 16, 15, 15, 12, 8, 10));
        register(new RaceDefinition("astral", "terraforge_rpg.race.astral", 1.35, true, "astral_surge", 10, 18, 10, 18, 16, 12, 12));
        register(new RaceDefinition("spectre", "terraforge_rpg.race.spectre", 1.20, true, "ethereal_form", 6, 16, 8, 16, 14, 10, 14));
        register(new RaceDefinition("insectoid", "terraforge_rpg.race.insectoid", 0.90, false, "metamorphosis", 16, 8, 12, 6, 10, 8, 12));
        register(new RaceDefinition("titan", "terraforge_rpg.race.titan", 1.50, false, "colossus", 20, 14, 20, 6, 12, 6, 6));
        register(new RaceDefinition("aetherian", "terraforge_rpg.race.aetherian", 1.45, true, "mana_singularity", 8, 20, 6, 22, 14, 12, 12));
        register(new RaceDefinition("reaper", "terraforge_rpg.race.reaper", 1.30, false, "soul_harvest", 10, 12, 18, 12, 18, 14, 12));
    }

    private static void register(RaceDefinition race) {
        RACES.put(race.id().toLowerCase(), race);
        ALL_RACE_IDS.add(race.id().toLowerCase());
    }

    public static Optional<RaceDefinition> get(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(RACES.get(id.toLowerCase()));
    }

    public static List<String> getAllRaceIds() {
        return Collections.unmodifiableList(ALL_RACE_IDS);
    }

    public static int getCount() {
        return RACES.size();
    }
}
