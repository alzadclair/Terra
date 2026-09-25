package com.terraforge.rpg.entity.npc.housing;

import net.minecraft.core.BlockPos;

/**
 * Result record from evaluating a 3D structure against Terraria housing rules.
 */
public record HousingValidationResult(
        boolean isValid,
        String reason,
        BlockPos standPosition,
        int volume
) {
    public static HousingValidationResult success(BlockPos standPos, int volume) {
        return new HousingValidationResult(true, "This housing is suitable.", standPos, volume);
    }

    public static HousingValidationResult failure(String reason) {
        return new HousingValidationResult(false, reason, null, 0);
    }
}
