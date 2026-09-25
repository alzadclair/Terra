package com.terraforge.rpg.world.biome;

import com.terraforge.rpg.TerraForgeRPG;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

/**
 * Resource keys for canonical Terraria biomes in TerraForge RPG.
 */
public final class ModBiomes {

    public static final ResourceKey<Biome> FOREST = register("forest");
    public static final ResourceKey<Biome> CORRUPTION = register("corruption");
    public static final ResourceKey<Biome> CRIMSON = register("crimson");
    public static final ResourceKey<Biome> JUNGLE = register("jungle");
    public static final ResourceKey<Biome> DESERT = register("desert");
    public static final ResourceKey<Biome> SNOW = register("snow");
    public static final ResourceKey<Biome> GLOWING_MUSHROOM = register("glowing_mushroom");
    public static final ResourceKey<Biome> UNDERWORLD = register("underworld");
    public static final ResourceKey<Biome> FLOATING_ISLAND = register("floating_island");
    public static final ResourceKey<Biome> OCEAN = register("ocean");

    private static ResourceKey<Biome> register(String name) {
        return ResourceKey.create(Registries.BIOME, TerraForgeRPG.id(name));
    }

    private ModBiomes() {}
}
