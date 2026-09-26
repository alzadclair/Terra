package com.terraforge.rpg.world.spawn;

import com.terraforge.rpg.registry.ModEntities;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

/**
 * Natural world spawn placement rules for TerraForge RPG mobs.
 * Ensures frequent, natural spawning across Overworld biomes.
 */
public final class ModSpawnPlacements {

    public static void register(RegisterSpawnPlacementsEvent event) {
        // Terraria Slimes spawn naturally in both daytime and night on solid ground
        event.register(ModEntities.GREEN_SLIME.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (entityType, level, spawnType, pos, random) ->
                        level.getBlockState(pos.below()).isSolid() && level.getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(ModEntities.BLUE_SLIME.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (entityType, level, spawnType, pos, random) ->
                        level.getBlockState(pos.below()).isSolid() && level.getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(ModEntities.TERRA_ZOMBIE.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);

        // Face Monster spawns in dark areas and surface
        event.register(ModEntities.FACE_MONSTER.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(ModEntities.DEMON_EYE.get(),
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (entityType, serverLevelAccessor, spawnType, blockPos, randomSource) ->
                        serverLevelAccessor.getRawBrightness(blockPos, 0) <= 7 &&
                        serverLevelAccessor.getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(ModEntities.GOBLIN_PEON.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(ModEntities.GOBLIN_THIEF.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(ModEntities.GOBLIN_WARRIOR.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(ModEntities.GOBLIN_SORCERER.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    private ModSpawnPlacements() {}
}
