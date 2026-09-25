package com.terraforge.rpg.network;

import com.terraforge.rpg.accessory.special.SpecialAccessoryService;
import com.terraforge.rpg.network.payload.*;
import com.terraforge.rpg.player.data.PlayerRPGData;
import com.terraforge.rpg.race.ability.AbilityExecutionService;
import com.terraforge.rpg.registry.ModAttachments;
import com.terraforge.rpg.stats.StatService;
import com.terraforge.rpg.util.TerraLogger;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Networking registry and message dispatcher for TerraForge RPG.
 */
public final class TerraNetwork {
    public static final String PROTOCOL_VERSION = "1";

    private TerraNetwork() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION).executesOn(HandlerThread.MAIN);

        // Server to Client: Authoritative RPG data snapshot
        registrar.playToClient(
                SyncPlayerRPGPacket.TYPE,
                SyncPlayerRPGPacket.STREAM_CODEC,
                (payload, context) -> {
                    if (context.player() != null) {
                        PlayerRPGData data = context.player().getData(ModAttachments.PLAYER_RPG_DATA);
                        data.deserializeNBT(context.player().registryAccess(), payload.data());
                        TerraLogger.debug("NET", "Client received PlayerRPGData snapshot.");
                    }
                }
        );

        // Client to Server: Allocate status points
        registrar.playToServer(
                SpendStatPointsRequest.TYPE,
                SpendStatPointsRequest.STREAM_CODEC,
                (payload, context) -> {
                    if (context.player() instanceof ServerPlayer player) {
                        StatService.spendPoints(player, payload.attributeKey(), payload.amount());
                    }
                }
        );

        // Client to Server: Open Character Menu
        registrar.playToServer(
                OpenCharacterMenuRequest.TYPE,
                OpenCharacterMenuRequest.STREAM_CODEC,
                (payload, context) -> {
                    if (context.player() instanceof ServerPlayer player) {
                        TerraLogger.debug("NET", "Received OpenCharacterMenuRequest from {}", player.getName().getString());
                        sync(player);
                    }
                }
        );

        // Client to Server: Activate Racial Ability
        registrar.playToServer(
                ActivateRaceAbilityRequest.TYPE,
                ActivateRaceAbilityRequest.STREAM_CODEC,
                (payload, context) -> {
                    if (context.player() instanceof ServerPlayer player) {
                        AbilityExecutionService.execute(player, payload.slot());
                    }
                }
        );

        // Client to Server: Equip Special Accessory
        registrar.playToServer(
                EquipSpecialAccessoryRequest.TYPE,
                EquipSpecialAccessoryRequest.STREAM_CODEC,
                (payload, context) -> {
                    if (context.player() instanceof ServerPlayer player) {
                        SpecialAccessoryService.equip(player, payload.accessoryId());
                    }
                }
        );

        // Client to Server: Unequip Special Accessory
        registrar.playToServer(
                UnequipSpecialAccessoryRequest.TYPE,
                UnequipSpecialAccessoryRequest.STREAM_CODEC,
                (payload, context) -> {
                    if (context.player() instanceof ServerPlayer player) {
                        SpecialAccessoryService.unequip(player);
                    }
                }
        );
    }

    /**
     * Sends authoritative PlayerRPGData to the specific player.
     */
    public static void sync(ServerPlayer player) {
        if (player == null || player.connection == null) {
            return;
        }
        PlayerRPGData data = player.getData(ModAttachments.PLAYER_RPG_DATA);
        PacketDistributor.sendToPlayer(player, new SyncPlayerRPGPacket(data.serializeNBT(player.registryAccess())));
    }
}
