package com.terraforge.rpg.entity;

import com.terraforge.rpg.experience.ThreatRating;
import com.terraforge.rpg.level.LevelService;
import com.terraforge.rpg.world.progression.WorldProgressionData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/**
 * Listens to entity death events to reward killing players with XP and Status Points.
 */
public final class EntityDeathHandler {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            LivingEntity target = event.getEntity();
            if (target == null || target == player) {
                return;
            }

            ThreatRating rating = ThreatRating.resolve(target);
            boolean firstBossKill = false;

            if (rating == ThreatRating.BOSS || rating == ThreatRating.EVENT_BOSS) {
                String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).toString();
                WorldProgressionData worldData = WorldProgressionData.get(player.server);
                if (!worldData.isBossDefeated(entityId)) {
                    firstBossKill = true;
                    worldData.markBossDefeated(entityId);
                }
            }

            LevelService.handleKillReward(player, rating, firstBossKill);
            com.terraforge.rpg.event.TerrariaEventManager.getInstance().onEntityKilled(target, player);
        }
    }

    private EntityDeathHandler() {}
}
