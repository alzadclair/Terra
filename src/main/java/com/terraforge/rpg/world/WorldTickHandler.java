package com.terraforge.rpg.world;

import com.terraforge.rpg.event.TerrariaEventManager;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Handles world/level ticks to update events, invasions, and environmental effects.
 */
public final class WorldTickHandler {

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            TerrariaEventManager.getInstance().tick(serverLevel);
        }
    }

    private WorldTickHandler() {}
}
