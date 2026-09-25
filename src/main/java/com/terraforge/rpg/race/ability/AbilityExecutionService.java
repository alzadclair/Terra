package com.terraforge.rpg.race.ability;

import com.terraforge.rpg.network.TerraNetwork;
import com.terraforge.rpg.player.data.PlayerRPGData;
import com.terraforge.rpg.registry.ModAttachments;
import com.terraforge.rpg.registry.ModSoundEvents;
import com.terraforge.rpg.util.TerraLogger;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;

/**
 * Server-authoritative executor validating cooldowns, mana costs, and race permissions.
 */
public final class AbilityExecutionService {

    private AbilityExecutionService() {}

    /**
     * Executes the racial ability in the specified slot (0 for primary, 1 for secondary).
     */
    public static boolean execute(ServerPlayer player, int slot) {
        if (player == null || !player.isAlive()) return false;

        PlayerRPGData data = player.getData(ModAttachments.PLAYER_RPG_DATA);
        String abilityId;
        long currentCooldown;

        long currentTime = player.server.overworld().getGameTime();

        if (slot == 0) {
            abilityId = data.getPrimaryAbility();
            currentCooldown = data.getPrimaryAbilityCooldownUntil();
        } else if (slot == 1) {
            if (!data.isHybrid()) {
                TerraLogger.warn("ABILITY", "Non-hybrid player {} attempted to use secondary ability slot!",
                        player.getName().getString());
                return false;
            }
            abilityId = data.getSecondaryAbility();
            currentCooldown = data.getSecondaryAbilityCooldownUntil();
        } else {
            return false;
        }

        if (abilityId == null || abilityId.isEmpty()) {
            return false;
        }

        RaceAbility ability = AbilityRegistry.get(abilityId).orElse(null);
        if (ability == null) {
            TerraLogger.error("ABILITY", "Unknown ability ID: {}", abilityId);
            return false;
        }

        // Validate cooldown
        if (currentTime < currentCooldown) {
            long remainingSeconds = (currentCooldown - currentTime + 19) / 20;
            player.sendSystemMessage(Component.literal("§c[Habilidade em Recarga] §fAguarde " + remainingSeconds + "s."));
            return false;
        }

        // Validate ability type
        if (ability.getType() == AbilityType.PASSIVE) {
            player.sendSystemMessage(Component.literal("§e" + ability.getDescription().getString()));
            return false;
        }

        // Validate mana cost
        double cost = ability.getManaCost();
        if (cost > 0.0 && data.getCurrentMana() < cost) {
            player.sendSystemMessage(Component.literal("§c[Mana Insuficiente] §fCusto: " + (int) cost + " Mana."));
            return false;
        }

        // Deduct mana
        if (cost > 0.0) {
            data.setCurrentMana(data.getCurrentMana() - cost);
        }

        // Execute ability logic
        boolean success = ability.execute(player, data);

        if (success) {
            long cooldownEnd = currentTime + ability.getCooldownTicks();
            if (slot == 0) {
                data.setPrimaryAbilityCooldownUntil(cooldownEnd);
            } else {
                data.setSecondaryAbilityCooldownUntil(cooldownEnd);
            }

            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSoundEvents.ABILITY_ACTIVATE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);

            TerraLogger.debug("ABILITY", "Player {} executed racial ability {} (Slot {})",
                    player.getName().getString(), abilityId, slot);

            TerraNetwork.sync(player);
            return true;
        }

        return false;
    }
}
