package com.terraforge.rpg.client.render.entity.state;

import com.terraforge.rpg.boss.prehardmode.EyeOfCthulhuEntity;
import com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshData;
import com.terraforge.rpg.client.render.mesh.TerraSkinnedMeshLoader;
import com.terraforge.rpg.util.TerraLogger;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages per-entity render and animation states for Eye of Cthulhu on the client.
 * Registers lifecycle hooks to prevent memory leaks on entity death, despawn, level unload,
 * and client disconnects.
 */
public final class EyeRenderStateManager {

    private static final Map<UUID, EyeRenderState> STATES = new ConcurrentHashMap<>();

    private EyeRenderStateManager() {}

    public static void registerEvents() {
        NeoForge.EVENT_BUS.addListener(EyeRenderStateManager::onEntityLeaveLevel);
        NeoForge.EVENT_BUS.addListener(EyeRenderStateManager::onLevelUnload);
        NeoForge.EVENT_BUS.addListener(EyeRenderStateManager::onLoggingOut);
    }

    public static EyeRenderState getOrCreate(EyeOfCthulhuEntity entity) {
        return STATES.computeIfAbsent(entity.getUUID(), uuid -> {
            try {
                TerraSkinnedMeshData dataP1 = TerraSkinnedMeshLoader.getOrLoad(TerraSkinnedMeshLoader.SKIN_EYE_P1);
                TerraSkinnedMeshData dataP2 = TerraSkinnedMeshLoader.getOrLoad(TerraSkinnedMeshLoader.SKIN_EYE_P2);
                EyeRenderState state = EyeRenderState.create(uuid, dataP1, dataP2);
                state.setEntityId(entity.getId());
                return state;
            } catch (Exception e) {
                TerraLogger.error("CLIENT", "Failed to create EyeRenderState for entity " + uuid, e);
                throw new RuntimeException("Could not create EyeRenderState", e);
            }
        });
    }

    public static EyeRenderState get(UUID uuid) {
        return STATES.get(uuid);
    }

    public static void put(UUID uuid, EyeRenderState state) {
        STATES.put(uuid, state);
    }

    public static void remove(UUID uuid) {
        STATES.remove(uuid);
    }

    public static void clear() {
        STATES.clear();
    }

    public static void invalidateInstances() {
        for (EyeRenderState state : STATES.values()) {
            state.resetInstances();
        }
    }

    public static int getActiveCount() {
        return STATES.size();
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof EyeOfCthulhuEntity eye) {
            STATES.remove(eye.getUUID());
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            clear();
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }
}
