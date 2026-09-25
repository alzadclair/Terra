package com.terraforge.rpg.item.prefix;

import java.util.*;

/**
 * Registry of canonical Terraria 1.4.5.8 prefixes.
 */
public final class PrefixRegistry {

    private static final Map<String, TerrariaPrefix> PREFIXES = new LinkedHashMap<>();

    static {
        // Universal & Common
        register(new TerrariaPrefix("quick", "Quick", TerrariaPrefixCategory.COMMON, 0.0, 0.10, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 1.21, 1));
        register(new TerrariaPrefix("deadly", "Deadly", TerrariaPrefixCategory.COMMON, 0.10, 0.10, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 1.46, 1));
        register(new TerrariaPrefix("agile", "Agile", TerrariaPrefixCategory.COMMON, 0.0, 0.10, 3.0, 0.0, 0.0, 0.0, 0.0, 0, 1.30, 1));
        register(new TerrariaPrefix("nimble", "Nimble", TerrariaPrefixCategory.COMMON, 0.0, 0.05, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 1.10, 0));
        register(new TerrariaPrefix("murderous", "Murderous", TerrariaPrefixCategory.COMMON, 0.07, 0.06, 3.0, 0.0, 0.0, 0.0, 0.0, 0, 1.33, 1));
        register(new TerrariaPrefix("slow", "Slow", TerrariaPrefixCategory.COMMON, 0.0, -0.15, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0.72, -1));
        register(new TerrariaPrefix("sluggish", "Sluggish", TerrariaPrefixCategory.COMMON, 0.0, -0.20, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0.64, -1));
        register(new TerrariaPrefix("lazy", "Lazy", TerrariaPrefixCategory.COMMON, 0.0, -0.08, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0.85, 0));
        register(new TerrariaPrefix("keen", "Keen", TerrariaPrefixCategory.COMMON, 0.0, 0.0, 3.0, 0.0, 0.0, 0.0, 0.0, 0, 1.12, 1));
        register(new TerrariaPrefix("superior", "Superior", TerrariaPrefixCategory.COMMON, 0.10, 0.0, 3.0, 0.10, 0.0, 0.0, 0.0, 0, 1.45, 1));
        register(new TerrariaPrefix("forceful", "Forceful", TerrariaPrefixCategory.COMMON, 0.0, 0.0, 0.0, 0.15, 0.0, 0.0, 0.0, 0, 1.15, 0));
        register(new TerrariaPrefix("broken", "Broken", TerrariaPrefixCategory.COMMON, -0.30, 0.0, 0.0, -0.20, 0.0, 0.0, 0.0, 0, 0.45, -2));
        register(new TerrariaPrefix("damaged", "Damaged", TerrariaPrefixCategory.COMMON, -0.15, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0.72, -1));
        register(new TerrariaPrefix("shoddy", "Shoddy", TerrariaPrefixCategory.COMMON, -0.10, 0.0, 0.0, -0.15, 0.0, 0.0, 0.0, 0, 0.68, -1));
        register(new TerrariaPrefix("hurtful", "Hurtful", TerrariaPrefixCategory.COMMON, 0.10, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 1.21, 1));
        register(new TerrariaPrefix("strong", "Strong", TerrariaPrefixCategory.COMMON, 0.0, 0.0, 0.0, 0.15, 0.0, 0.0, 0.0, 0, 1.15, 0));
        register(new TerrariaPrefix("unpleasant", "Unpleasant", TerrariaPrefixCategory.COMMON, 0.05, 0.0, 0.0, 0.15, 0.0, 0.0, 0.0, 0, 1.28, 1));
        register(new TerrariaPrefix("weak", "Weak", TerrariaPrefixCategory.COMMON, 0.0, 0.0, 0.0, -0.20, 0.0, 0.0, 0.0, 0, 0.80, -1));
        register(new TerrariaPrefix("ruthless", "Ruthless", TerrariaPrefixCategory.COMMON, 0.18, 0.0, 0.0, -0.10, 0.0, 0.0, 0.0, 0, 1.28, 1));
        register(new TerrariaPrefix("godly", "Godly", TerrariaPrefixCategory.COMMON, 0.15, 0.0, 5.0, 0.15, 0.0, 0.0, 0.0, 0, 1.70, 2));
        register(new TerrariaPrefix("demonic", "Demonic", TerrariaPrefixCategory.COMMON, 0.15, 0.0, 5.0, 0.0, 0.0, 0.0, 0.0, 0, 1.50, 1));
        register(new TerrariaPrefix("zealous", "Zealous", TerrariaPrefixCategory.COMMON, 0.0, 0.0, 5.0, 0.0, 0.0, 0.0, 0.0, 0, 1.21, 1));

        // Melee Exclusive
        register(new TerrariaPrefix("legendary", "Legendary", TerrariaPrefixCategory.MELEE, 0.15, 0.10, 5.0, 0.15, 0.0, 0.10, 0.0, 0, 2.10, 2));
        register(new TerrariaPrefix("heavy", "Heavy", TerrariaPrefixCategory.MELEE, 0.0, -0.10, 0.0, 0.15, 0.0, 0.0, 0.0, 0, 1.05, 0));
        register(new TerrariaPrefix("light", "Light", TerrariaPrefixCategory.MELEE, -0.10, 0.15, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 1.08, 0));
        register(new TerrariaPrefix("massive", "Massive", TerrariaPrefixCategory.MELEE, 0.0, 0.0, 0.0, 0.0, 0.0, 0.18, 0.0, 0, 1.18, 1));
        register(new TerrariaPrefix("savage", "Savage", TerrariaPrefixCategory.MELEE, 0.10, 0.0, 0.0, 0.10, 0.0, 0.10, 0.0, 0, 1.45, 1));
        register(new TerrariaPrefix("sharp", "Sharp", TerrariaPrefixCategory.MELEE, 0.15, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 1.32, 1));
        register(new TerrariaPrefix("tiny", "Tiny", TerrariaPrefixCategory.MELEE, 0.0, 0.0, 0.0, 0.0, 0.0, -0.18, 0.0, 0, 0.70, -1));
        register(new TerrariaPrefix("shameful", "Shameful", TerrariaPrefixCategory.MELEE, -0.10, 0.0, 0.0, -0.20, 0.0, -0.20, 0.0, 0, 0.50, -2));
        register(new TerrariaPrefix("bulky", "Bulky", TerrariaPrefixCategory.MELEE, 0.05, -0.15, 0.0, 0.10, 0.0, 0.10, 0.0, 0, 1.10, 0));

        // Ranged Exclusive
        register(new TerrariaPrefix("unreal", "Unreal", TerrariaPrefixCategory.RANGED, 0.15, 0.10, 5.0, 0.15, 0.0, 0.0, 0.10, 0, 2.10, 2));
        register(new TerrariaPrefix("rapid", "Rapid", TerrariaPrefixCategory.RANGED, 0.0, 0.15, 0.0, 0.0, 0.0, 0.0, 0.10, 0, 1.35, 1));
        register(new TerrariaPrefix("hasty", "Hasty", TerrariaPrefixCategory.RANGED, 0.0, 0.10, 0.0, 0.0, 0.0, 0.0, 0.15, 0, 1.32, 1));
        register(new TerrariaPrefix("powerful", "Powerful", TerrariaPrefixCategory.RANGED, 0.15, -0.10, 1.0, 0.0, 0.0, 0.0, 0.0, 0, 1.25, 1));
        register(new TerrariaPrefix("awkward", "Awkward", TerrariaPrefixCategory.RANGED, 0.0, -0.10, 0.0, 0.0, 0.0, 0.0, -0.10, 0, 0.75, -1));

        // Magic Exclusive
        register(new TerrariaPrefix("mythical", "Mythical", TerrariaPrefixCategory.MAGIC, 0.15, 0.10, 5.0, 0.15, -0.10, 0.0, 0.0, 0, 2.10, 2));
        register(new TerrariaPrefix("masterful", "Masterful", TerrariaPrefixCategory.MAGIC, 0.15, 0.0, 0.0, 0.05, -0.20, 0.0, 0.0, 0, 1.75, 2));
        register(new TerrariaPrefix("adept", "Adept", TerrariaPrefixCategory.MAGIC, 0.0, 0.0, 0.0, 0.0, -0.15, 0.0, 0.0, 0, 1.25, 1));
        register(new TerrariaPrefix("mystic", "Mystic", TerrariaPrefixCategory.MAGIC, 0.10, 0.0, 0.0, 0.0, -0.15, 0.0, 0.0, 0, 1.45, 1));
        register(new TerrariaPrefix("taboo", "Taboo", TerrariaPrefixCategory.MAGIC, 0.0, -0.10, 0.0, 0.10, 0.10, 0.0, 0.0, 0, 0.85, -1));
        register(new TerrariaPrefix("celestial", "Celestial", TerrariaPrefixCategory.MAGIC, 0.10, -0.10, 0.0, 0.10, -0.10, 0.0, 0.0, 0, 1.28, 1));

        // Accessory Exclusive
        register(new TerrariaPrefix("hard", "Hard", TerrariaPrefixCategory.ACCESSORY, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1, 1.25, 1));
        register(new TerrariaPrefix("guarding", "Guarding", TerrariaPrefixCategory.ACCESSORY, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 2, 1.50, 1));
        register(new TerrariaPrefix("armored", "Armored", TerrariaPrefixCategory.ACCESSORY, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 3, 1.75, 1));
        register(new TerrariaPrefix("warding", "Warding", TerrariaPrefixCategory.ACCESSORY, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 4, 2.00, 2));

        register(new TerrariaPrefix("precise", "Precise", TerrariaPrefixCategory.ACCESSORY, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0, 1.25, 1));
        register(new TerrariaPrefix("lucky", "Lucky", TerrariaPrefixCategory.ACCESSORY, 0.0, 0.0, 4.0, 0.0, 0.0, 0.0, 0.0, 0, 2.00, 2));

        register(new TerrariaPrefix("jagged", "Jagged", TerrariaPrefixCategory.ACCESSORY, 0.01, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 1.25, 1));
        register(new TerrariaPrefix("spiked", "Spiked", TerrariaPrefixCategory.ACCESSORY, 0.02, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 1.50, 1));
        register(new TerrariaPrefix("angry", "Angry", TerrariaPrefixCategory.ACCESSORY, 0.03, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 1.75, 1));
        register(new TerrariaPrefix("menacing", "Menacing", TerrariaPrefixCategory.ACCESSORY, 0.04, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 2.00, 2));

        register(new TerrariaPrefix("brisk", "Brisk", TerrariaPrefixCategory.ACCESSORY, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 1.25, 1));
        register(new TerrariaPrefix("fleeting", "Fleeting", TerrariaPrefixCategory.ACCESSORY, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 1.50, 1));
        register(new TerrariaPrefix("hasty_acc", "Hasty", TerrariaPrefixCategory.ACCESSORY, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 1.75, 1));
        register(new TerrariaPrefix("quick_acc", "Quick", TerrariaPrefixCategory.ACCESSORY, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 2.00, 2));

        register(new TerrariaPrefix("wild", "Wild", TerrariaPrefixCategory.ACCESSORY, 0.0, 0.01, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 1.25, 1));
        register(new TerrariaPrefix("violent", "Violent", TerrariaPrefixCategory.ACCESSORY, 0.0, 0.04, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 2.00, 2));
        register(new TerrariaPrefix("arcane", "Arcane", TerrariaPrefixCategory.ACCESSORY, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 1.50, 1));
    }

    private static void register(TerrariaPrefix prefix) {
        PREFIXES.put(prefix.id().toLowerCase(), prefix);
    }

    public static Optional<TerrariaPrefix> get(String id) {
        if (id == null || id.isEmpty()) return Optional.of(TerrariaPrefix.NONE);
        return Optional.ofNullable(PREFIXES.get(id.toLowerCase()));
    }

    public static List<TerrariaPrefix> getByCategory(TerrariaPrefixCategory category) {
        return PREFIXES.values().stream()
                .filter(p -> p.category() == category)
                .toList();
    }

    public static List<TerrariaPrefix> getApplicablePrefixes(TerrariaPrefixCategory category) {
        if (category == TerrariaPrefixCategory.ACCESSORY) {
            return getByCategory(TerrariaPrefixCategory.ACCESSORY);
        }
        List<TerrariaPrefix> list = new ArrayList<>();
        list.addAll(getByCategory(TerrariaPrefixCategory.COMMON));
        list.addAll(getByCategory(TerrariaPrefixCategory.UNIVERSAL));
        list.addAll(getByCategory(category));
        return list;
    }

    public static TerrariaPrefix rollRandomPrefix(TerrariaPrefixCategory category, Random random) {
        List<TerrariaPrefix> applicable = getApplicablePrefixes(category);
        if (applicable.isEmpty()) return TerrariaPrefix.NONE;
        return applicable.get(random.nextInt(applicable.size()));
    }

    public static Collection<TerrariaPrefix> getAll() {
        return Collections.unmodifiableCollection(PREFIXES.values());
    }

    private PrefixRegistry() {}
}
