package com.terraforge.rpg.player;

import com.terraforge.rpg.config.TerraServerConfig;
import com.terraforge.rpg.mana.ManaService;
import com.terraforge.rpg.network.TerraNetwork;
import com.terraforge.rpg.player.data.PlayerRPGData;
import com.terraforge.rpg.race.RaceAssignmentService;
import com.terraforge.rpg.registry.ModAttachments;
import com.terraforge.rpg.util.TerraLogger;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Handles player lifecycle events such as login, death persistence, and dimension changes.
 */
public final class PlayerLifecycleHandler {

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerRPGData data = player.getData(ModAttachments.PLAYER_RPG_DATA);

            // First-time player initialization with race roll (including 1% hybrid chance)
            if (!data.isRaceAssigned()) {
                data.setLevel(TerraServerConfig.START_LEVEL.get());
                if (TerraServerConfig.GIVE_POINTS_AT_LEVEL_ONE.get()) {
                    data.setAvailableStatusPoints(TerraServerConfig.POINTS_PER_LEVEL.get());
                    data.setTotalStatusPointsAcquired(TerraServerConfig.POINTS_PER_LEVEL.get());
                }
                RaceAssignmentService.assignInitialRace(player);
            } else {
                RaceAssignmentService.updateFlightPermissions(player);
                TerraNetwork.sync(player);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer newPlayer) {
            ServerPlayer oldPlayer = (ServerPlayer) event.getOriginal();
            PlayerRPGData oldData = oldPlayer.getData(ModAttachments.PLAYER_RPG_DATA);
            PlayerRPGData newData = newPlayer.getData(ModAttachments.PLAYER_RPG_DATA);

            // Ensure exact persistence across deaths and dimension transfers
            newData.deserializeNBT(newPlayer.registryAccess(), oldData.serializeNBT(oldPlayer.registryAccess()));
            RaceAssignmentService.updateFlightPermissions(newPlayer);
            TerraLogger.debug("PLAYER", "Restored PlayerRPGData for {} after clone event (wasDeath={})",
                    newPlayer.getName().getString(), event.isWasDeath());
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RaceAssignmentService.updateFlightPermissions(player);
            TerraNetwork.sync(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RaceAssignmentService.updateFlightPermissions(player);
            TerraNetwork.sync(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ManaService.tick(player);
            com.terraforge.rpg.armor.ArmorSetService.tickArmorEffects(player);
        }
    }

    private PlayerLifecycleHandler() {}
}
