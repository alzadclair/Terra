package com.terraforge.rpg.world.dimension;

import com.terraforge.rpg.TerraForgeRPG;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

/**
 * Dimension keys for the Terraria Realm vertical world.
 */
public final class ModDimensions {

    public static final ResourceKey<Level> TERRARIA_REALM =
            ResourceKey.create(Registries.DIMENSION, TerraForgeRPG.id("terraria_realm"));

    public static final ResourceKey<DimensionType> TERRARIA_REALM_TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE, TerraForgeRPG.id("terraria_realm_type"));

    private ModDimensions() {}
}
