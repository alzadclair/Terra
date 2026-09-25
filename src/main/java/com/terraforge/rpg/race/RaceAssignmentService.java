package com.terraforge.rpg.race;

import com.terraforge.rpg.config.TerraServerConfig;
import com.terraforge.rpg.network.TerraNetwork;
import com.terraforge.rpg.player.data.PlayerRPGData;
import com.terraforge.rpg.registry.ModAttachments;
import com.terraforge.rpg.util.TerraLogger;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Random;

/**
 * Server-authoritative service handling initial race generation,
 * hybrid rolls (1% chance), and flight authorization.
 */
public final class RaceAssignmentService {
    private static final Random RANDOM = new Random();

    private RaceAssignmentService() {}

    /**
     * Assigns initial race upon the player's first join.
     * Prevents relog reroll exploits.
     */
    public static void assignInitialRace(ServerPlayer player) {
        if (player == null) return;
        PlayerRPGData data = player.getData(ModAttachments.PLAYER_RPG_DATA);

        if (data.isRaceAssigned()) {
            updateFlightPermissions(player);
            return;
        }

        List<String> allRaces = RaceRegistry.getAllRaceIds();
        if (allRaces.isEmpty()) {
            TerraLogger.error("RACE", "Cannot assign race: RaceRegistry is empty!");
            return;
        }

        double hybridChance = 0.01;
        try {
            if (TerraServerConfig.HYBRID_CHANCE != null && TerraServerConfig.HYBRID_CHANCE.get() != null) {
                hybridChance = TerraServerConfig.HYBRID_CHANCE.get();
            }
        } catch (Exception ignored) {}

        boolean isHybrid = RANDOM.nextDouble() < hybridChance;

        if (!isHybrid) {
            String chosen = allRaces.get(RANDOM.nextInt(allRaces.size()));
            RaceDefinition def = RaceRegistry.get(chosen).orElseThrow();

            data.setPrimaryRace(def.id());
            data.setSecondaryRace("");
            data.setHybrid(false);
            data.setPrimaryAbility(def.abilityId());
            data.setSecondaryAbility("");
            data.setRaceAssigned(true);

            player.sendSystemMessage(Component.literal("§6§l[TerraForge RPG] §fVocê nasceu como: §e" + capitalize(def.id())));
            player.sendSystemMessage(Component.literal("§6§l[Habilidade Racial] §b" + capitalize(def.abilityId())));
            TerraLogger.info("RACE", "Assigned race {} to player {}", def.id(), player.getName().getString());
        } else {
            int indexA = RANDOM.nextInt(allRaces.size());
            int indexB = RANDOM.nextInt(allRaces.size() - 1);
            if (indexB >= indexA) indexB++;

            RaceDefinition raceA = RaceRegistry.get(allRaces.get(indexA)).orElseThrow();
            RaceDefinition raceB = RaceRegistry.get(allRaces.get(indexB)).orElseThrow();

            data.setPrimaryRace(raceA.id());
            data.setSecondaryRace(raceB.id());
            data.setHybrid(true);
            data.setPrimaryAbility(raceA.abilityId());
            data.setSecondaryAbility(raceB.abilityId());
            data.setRaceAssigned(true);

            player.sendSystemMessage(Component.literal("§d§l[RAÇA HÍBRIDA RARA!] §fVocê nasceu como: §e" + capitalize(raceA.id()) + " §f+ §e" + capitalize(raceB.id())));
            player.sendSystemMessage(Component.literal("§6§l[Habilidades] §b" + capitalize(raceA.abilityId()) + " §7(Tecla R) §fe §b" + capitalize(raceB.abilityId()) + " §7(Tecla V)"));
            TerraLogger.info("RACE", "Assigned hybrid races {} + {} to player {}", raceA.id(), raceB.id(), player.getName().getString());
        }

        updateFlightPermissions(player);
        TerraNetwork.sync(player);
    }

    /**
     * Updates player creative flight permissions according to race natural flight or equipped Phoenix Wings.
     */
    public static void updateFlightPermissions(ServerPlayer player) {
        if (player == null) return;
        PlayerRPGData data = player.getData(ModAttachments.PLAYER_RPG_DATA);

        boolean naturalFlight = isNaturallyWinged(data);
        boolean specialAccessoryFlight = "phoenix_wings".equalsIgnoreCase(data.getEquippedSpecialAccessory());

        boolean shouldFly = naturalFlight || specialAccessoryFlight;
        data.setFlightAuthorized(shouldFly);

        // In survival or adventure mode, control abilities
        if (!player.isCreative() && !player.isSpectator()) {
            player.getAbilities().mayfly = shouldFly;
            if (!shouldFly && player.getAbilities().flying) {
                player.getAbilities().flying = false;
            }
            if (player.connection != null) {
                player.connection.send(new ClientboundPlayerAbilitiesPacket(player.getAbilities()));
            }
        }
    }

    /**
     * Checks if the player's race or hybrid ancestry has natural flight.
     */
    public static boolean isNaturallyWinged(PlayerRPGData data) {
        boolean primaryFlight = RaceRegistry.get(data.getPrimaryRace())
                .map(RaceDefinition::naturalFlight)
                .orElse(false);

        if (!data.isHybrid() || data.getSecondaryRace().isEmpty()) {
            return primaryFlight;
        }

        boolean secondaryFlight = RaceRegistry.get(data.getSecondaryRace())
                .map(RaceDefinition::naturalFlight)
                .orElse(false);

        return primaryFlight || secondaryFlight;
    }

    private static String capitalize(String text) {
        if (text == null || text.isEmpty()) return "";
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}
