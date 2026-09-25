package com.terraforge.rpg.entity.npc.housing;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Service managing Town NPC housing assignments and nighttime homecoming behavior.
 */
public final class HousingService {

    private static final HousingService INSTANCE = new HousingService();

    private final Map<String, BlockPos> npcToHouseMap = new HashMap<>();

    private HousingService() {}

    public static HousingService getInstance() {
        return INSTANCE;
    }

    public synchronized void assignHouse(String npcId, BlockPos standPos) {
        if (npcId != null && standPos != null) {
            npcToHouseMap.put(npcId.toLowerCase(), standPos);
        }
    }

    public synchronized BlockPos getHouse(String npcId) {
        if (npcId == null) return null;
        return npcToHouseMap.get(npcId.toLowerCase());
    }

    public synchronized void vacateHouse(String npcId) {
        if (npcId != null) {
            npcToHouseMap.remove(npcId.toLowerCase());
        }
    }

    public synchronized boolean hasAssignedHouse(String npcId) {
        return npcId != null && npcToHouseMap.containsKey(npcId.toLowerCase());
    }

    public synchronized boolean isHouseOccupied(BlockPos standPos) {
        if (standPos == null) return false;
        return npcToHouseMap.containsValue(standPos);
    }

    /**
     * Handles nighttime NPC homecoming. When off-screen during night, teleports to assigned room.
     */
    public void tickNpcHomecoming(ServerLevel level, String npcId, LivingEntity npc) {
        if (level == null || npcId == null || npc == null) return;

        BlockPos home = getHouse(npcId);
        if (home == null) return;

        long dayTime = level.getDayTime() % 24000L;
        boolean isNight = dayTime >= 13000L && dayTime <= 23000L;

        if (isNight) {
            double distSq = npc.distanceToSqr(home.getX() + 0.5, home.getY(), home.getZ() + 0.5);
            if (distSq > 400.0) { // > 20 blocks from house
                Player nearestPlayer = level.getNearestPlayer(npc, 24.0);
                if (nearestPlayer == null) {
                    // Off-screen: teleport home
                    npc.teleportTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5);
                }
            }
        }
    }
}
