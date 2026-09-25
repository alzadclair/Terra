package com.terraforge.rpg.registry;

import com.terraforge.rpg.TerraForgeRPG;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Block registry for TerraForge RPG.
 */
public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TerraForgeRPG.MOD_ID);

    // Starter Terraria-equivalent blocks
    public static final DeferredBlock<Block> COPPER_ORE = BLOCKS.registerSimpleBlock("copper_ore",
            BlockBehaviour.Properties.of().strength(3.0f, 3.0f).sound(SoundType.STONE).requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> TIN_ORE = BLOCKS.registerSimpleBlock("tin_ore",
            BlockBehaviour.Properties.of().strength(3.0f, 3.0f).sound(SoundType.STONE).requiresCorrectToolForDrops());

    // World Layer Blocks
    public static final DeferredBlock<Block> ASH_BLOCK = BLOCKS.registerSimpleBlock("ash_block",
            BlockBehaviour.Properties.of().strength(0.5f, 0.5f).sound(SoundType.SAND));

    public static final DeferredBlock<Block> HELLSTONE_ORE = BLOCKS.registerSimpleBlock("hellstone_ore",
            BlockBehaviour.Properties.of().strength(4.0f, 6.0f).sound(SoundType.STONE).lightLevel(state -> 5).requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> EBONSTONE_BLOCK = BLOCKS.registerSimpleBlock("ebonstone_block",
            BlockBehaviour.Properties.of().strength(4.5f, 6.0f).sound(SoundType.STONE).requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> CRIMSTONE_BLOCK = BLOCKS.registerSimpleBlock("crimstone_block",
            BlockBehaviour.Properties.of().strength(4.5f, 6.0f).sound(SoundType.STONE).requiresCorrectToolForDrops());

    // Terraria Altars
    public static final DeferredBlock<com.terraforge.rpg.block.DemonAltarBlock> DEMON_ALTAR = BLOCKS.register("demon_altar",
            () -> new com.terraforge.rpg.block.DemonAltarBlock(false));

    public static final DeferredBlock<com.terraforge.rpg.block.DemonAltarBlock> CRIMSON_ALTAR = BLOCKS.register("crimson_altar",
            () -> new com.terraforge.rpg.block.DemonAltarBlock(true));

    // Hardmode Ores
    public static final DeferredBlock<Block> COBALT_ORE = BLOCKS.registerSimpleBlock("cobalt_ore",
            BlockBehaviour.Properties.of().strength(4.0f, 4.0f).sound(SoundType.STONE).requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> PALLADIUM_ORE = BLOCKS.registerSimpleBlock("palladium_ore",
            BlockBehaviour.Properties.of().strength(4.0f, 4.0f).sound(SoundType.STONE).requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> MYTHRIL_ORE = BLOCKS.registerSimpleBlock("mythril_ore",
            BlockBehaviour.Properties.of().strength(4.5f, 4.5f).sound(SoundType.STONE).requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> ORICHALCUM_ORE = BLOCKS.registerSimpleBlock("orichalcum_ore",
            BlockBehaviour.Properties.of().strength(4.5f, 4.5f).sound(SoundType.STONE).requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> ADAMANTITE_ORE = BLOCKS.registerSimpleBlock("adamantite_ore",
            BlockBehaviour.Properties.of().strength(5.0f, 5.0f).sound(SoundType.STONE).requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> TITANIUM_ORE = BLOCKS.registerSimpleBlock("titanium_ore",
            BlockBehaviour.Properties.of().strength(5.0f, 5.0f).sound(SoundType.STONE).requiresCorrectToolForDrops());

    // Endgame Celestial Ore
    public static final DeferredBlock<Block> LUMINITE_ORE = BLOCKS.registerSimpleBlock("luminite_ore",
            BlockBehaviour.Properties.of().strength(6.0f, 6.0f).sound(SoundType.STONE).lightLevel(state -> 7).requiresCorrectToolForDrops());

    private ModBlocks() {}
}
