package com.terraforge.rpg.entity.npc.housing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * Validates player-constructed buildings against Terraria 1.4.5.8 housing requirements in 3D space:
 * 1. Fully enclosed structure (floor, ceiling, walls, doors).
 * 2. Minimum volume 30 blocks, maximum 750 blocks.
 * 3. At least one entrance (Door / Trapdoor).
 * 4. At least one light source (Torch, Lantern, etc.).
 * 5. At least one flat surface item (Table, Work Bench, Desk).
 * 6. At least one comfort item (Chair, Bed, Sofa).
 * 7. Solid floor standing space.
 */
public final class HousingValidator {

    public static final int MIN_VOLUME = 30;
    public static final int MAX_VOLUME = 750;
    public static final int MAX_RADIUS = 15;

    private HousingValidator() {}

    public static HousingValidationResult validate(Level level, BlockPos startPos) {
        if (level == null || startPos == null) {
            return HousingValidationResult.failure("Invalid level or coordinates.");
        }

        BlockState startState = level.getBlockState(startPos);
        if (startState.isSolid()) {
            return HousingValidationResult.failure("Starting position must be an interior open air space.");
        }

        Set<BlockPos> interior = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(startPos);
        interior.add(startPos);

        boolean hasEntrance = false;
        boolean hasLight = false;
        boolean hasFlatSurface = false;
        boolean hasComfort = false;

        BlockPos validStandPos = null;

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();

            // Check if room expands too large
            if (interior.size() > MAX_VOLUME) {
                return HousingValidationResult.failure("This house is too large (maximum " + MAX_VOLUME + " blocks).");
            }

            // Check distance limit from start position
            if (Math.abs(current.getX() - startPos.getX()) > MAX_RADIUS ||
                Math.abs(current.getY() - startPos.getY()) > MAX_RADIUS ||
                Math.abs(current.getZ() - startPos.getZ()) > MAX_RADIUS) {
                return HousingValidationResult.failure("This house is missing a wall or door (not fully enclosed).");
            }

            // Inspect this interior space for light and stand space
            BlockState currentState = level.getBlockState(current);
            if (currentState.getLightEmission(level, current) > 0) {
                hasLight = true;
            }

            // Check floor standing position
            BlockPos below = current.below();
            BlockState belowState = level.getBlockState(below);
            BlockPos above = current.above();
            BlockState aboveState = level.getBlockState(above);

            if (belowState.isSolid() && !currentState.isSolid() && !aboveState.isSolid() && validStandPos == null) {
                validStandPos = current;
            }

            // Check neighbors for walls, doors, and furniture
            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = current.relative(dir);
                BlockState neighborState = level.getBlockState(neighborPos);

                // Check furniture and items
                if (neighborState.getBlock() instanceof DoorBlock || neighborState.getBlock() instanceof TrapDoorBlock) {
                    hasEntrance = true;
                }
                if (neighborState.getLightEmission(level, neighborPos) > 0) {
                    hasLight = true;
                }

                String blockName = neighborState.getBlock().getDescriptionId().toLowerCase();
                if (neighborState.getBlock() instanceof CraftingTableBlock ||
                    blockName.contains("table") || blockName.contains("desk") || blockName.contains("bench") ||
                    blockName.contains("bookshelf") || blockName.contains("lectern")) {
                    hasFlatSurface = true;
                }
                if (neighborState.getBlock() instanceof BedBlock ||
                    blockName.contains("chair") || blockName.contains("bed") || blockName.contains("sofa")) {
                    hasComfort = true;
                }

                // If neighbor is air/passable, continue flood fill
                if (!neighborState.isSolid() && !(neighborState.getBlock() instanceof DoorBlock)) {
                    if (interior.add(neighborPos)) {
                        queue.add(neighborPos);
                    }
                }
            }
        }

        // Validate final metrics
        if (interior.size() < MIN_VOLUME) {
            return HousingValidationResult.failure("This house is too small (minimum " + MIN_VOLUME + " blocks).");
        }
        if (!hasEntrance) {
            return HousingValidationResult.failure("This house is missing an entrance door.");
        }
        if (!hasLight) {
            return HousingValidationResult.failure("This house is missing a light source.");
        }
        if (!hasFlatSurface) {
            return HousingValidationResult.failure("This house is missing a table or flat surface.");
        }
        if (!hasComfort) {
            return HousingValidationResult.failure("This house is missing a chair or comfort item.");
        }
        if (validStandPos == null) {
            return HousingValidationResult.failure("This house has no valid solid floor for the NPC to stand on.");
        }

        return HousingValidationResult.success(validStandPos, interior.size());
    }
}
