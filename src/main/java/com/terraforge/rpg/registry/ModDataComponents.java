package com.terraforge.rpg.registry;

import com.mojang.serialization.Codec;
import com.terraforge.rpg.TerraForgeRPG;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Data components for Terraria items, prefixes, and rarities in Minecraft 1.21.1.
 */
public final class ModDataComponents {

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, TerraForgeRPG.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> PREFIX =
            DATA_COMPONENT_TYPES.register("prefix", () -> DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> RARITY_OVERRIDE =
            DATA_COMPONENT_TYPES.register("rarity_override", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> REFORGE_COUNT =
            DATA_COMPONENT_TYPES.register("reforge_count", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    private ModDataComponents() {}
}
