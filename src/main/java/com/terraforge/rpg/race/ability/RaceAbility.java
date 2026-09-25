package com.terraforge.rpg.race.ability;

import com.terraforge.rpg.player.data.PlayerRPGData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Base contract for all 30 canonical racial abilities.
 */
public interface RaceAbility {
    String getId();
    String getOwnerRace();
    AbilityType getType();
    long getCooldownTicks();
    double getManaCost();
    Component getDescription();

    default boolean canActivate(ServerPlayer player, PlayerRPGData data) {
        if (getType() == AbilityType.PASSIVE) return false;
        if (player == null || !player.isAlive()) return false;
        return data.getCurrentMana() >= getManaCost();
    }

    /**
     * Executes the authoritative ability effect on the server.
     * @return true if execution succeeded and cooldown should be triggered.
     */
    boolean execute(ServerPlayer player, PlayerRPGData data);
}
