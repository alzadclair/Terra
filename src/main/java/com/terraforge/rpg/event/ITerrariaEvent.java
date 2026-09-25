package com.terraforge.rpg.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Interface defining a canonical Terraria event or invasion.
 */
public interface ITerrariaEvent {

    String getEventId();

    String getDisplayName();

    boolean isActive();

    void start(ServerLevel level);

    void tick(ServerLevel level);

    void stop(ServerLevel level);

    float getProgress();

    void onEntityKilled(LivingEntity entity, Player killer);
}
