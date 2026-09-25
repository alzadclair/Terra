package com.terraforge.rpg.stats;

import com.terraforge.rpg.network.TerraNetwork;
import com.terraforge.rpg.player.data.PlayerRPGData;
import com.terraforge.rpg.registry.ModAttachments;
import com.terraforge.rpg.util.TerraLogger;
import net.minecraft.server.level.ServerPlayer;

/**
 * Server-authoritative logic for spending and calculating player status points.
 */
public final class StatService {

    private StatService() {}

    /**
     * Attempts to spend points on the given attribute for a player.
     * All checks are performed server-side with anti-cheat validation.
     */
    public static boolean spendPoints(ServerPlayer player, String attributeKey, int requestedAmount) {
        if (player == null || !player.isAlive() || requestedAmount <= 0) {
            return false;
        }

        AttributeType type = AttributeType.fromId(attributeKey);
        if (type == null) {
            TerraLogger.warn("STATS", "Player {} attempted to spend points on unknown attribute: {}",
                    player.getName().getString(), attributeKey);
            return false;
        }

        PlayerRPGData data = player.getData(ModAttachments.PLAYER_RPG_DATA);
        int available = data.getAvailableStatusPoints();
        if (available <= 0) {
            return false;
        }

        int pointsToSpend = Math.min(requestedAmount, available);
        int currentRank = getRank(data, type.getId());
        int effectiveCap = AttributeCapRegistry.getEffectiveCap(data, type);
        boolean bypassCap = AttributeCapRegistry.isEvolutionUncapped(data);

        if (!bypassCap) {
            if (currentRank >= effectiveCap) {
                TerraLogger.debug("STATS", "Player {} reached cap ({}) for {}",
                        player.getName().getString(), effectiveCap, type.getId());
                return false;
            }
            int room = effectiveCap - currentRank;
            pointsToSpend = Math.min(pointsToSpend, room);
        }

        if (pointsToSpend <= 0) {
            return false;
        }

        // Apply deduction atomically
        data.setAvailableStatusPoints(available - pointsToSpend);
        data.setStatusPointsSpent(data.getStatusPointsSpent() + pointsToSpend);
        applyRankIncrease(data, type, pointsToSpend);

        TerraLogger.debug("STATS", "Player {} spent {} points on {}. New rank: {}",
                player.getName().getString(), pointsToSpend, type.getId(), getRank(data, type.getId()));

        // Sync authoritative state to client
        TerraNetwork.sync(player);
        return true;
    }

    public static int getRank(PlayerRPGData data, String attributeKey) {
        AttributeType type = AttributeType.fromId(attributeKey);
        if (type == null) return 0;

        return switch (type) {
            case DEFENSE -> data.getDefenseRank();
            case MAGIC_DEFENSE -> data.getMagicDefenseRank();
            case ATTACK -> data.getAttackRank();
            case MAGIC_ATTACK -> data.getMagicAttackRank();
            case CRITICAL -> data.getCriticalRank();
            case CRITICAL_CHANCE -> data.getCriticalChanceRank();
            case SPEED -> data.getSpeedRank();
        };
    }

    private static void applyRankIncrease(PlayerRPGData data, AttributeType type, int amount) {
        switch (type) {
            case DEFENSE -> data.setDefenseRank(data.getDefenseRank() + amount);
            case MAGIC_DEFENSE -> data.setMagicDefenseRank(data.getMagicDefenseRank() + amount);
            case ATTACK -> data.setAttackRank(data.getAttackRank() + amount);
            case MAGIC_ATTACK -> data.setMagicAttackRank(data.getMagicAttackRank() + amount);
            case CRITICAL -> data.setCriticalRank(data.getCriticalRank() + amount);
            case CRITICAL_CHANCE -> data.setCriticalChanceRank(data.getCriticalChanceRank() + amount);
            case SPEED -> data.setSpeedRank(data.getSpeedRank() + amount);
        }
    }

    public static int getEffectiveCap(PlayerRPGData data, String attributeKey) {
        AttributeType type = AttributeType.fromId(attributeKey);
        if (type == null) return 1000;
        return AttributeCapRegistry.getEffectiveCap(data, type);
    }
}
