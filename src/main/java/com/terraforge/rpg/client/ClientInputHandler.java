package com.terraforge.rpg.client;

import com.terraforge.rpg.client.gui.screen.CharacterMenuScreen;
import com.terraforge.rpg.client.key.ModKeyMappings;
import com.terraforge.rpg.network.payload.ActivateRaceAbilityRequest;
import com.terraforge.rpg.network.payload.OpenCharacterMenuRequest;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Handles client key input detecting K, R, and V key presses.
 */
public final class ClientInputHandler {

    private static int captureTickDelay = 0;

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();

        // Automated runtime validation hook
        if ("true".equalsIgnoreCase(System.getProperty("terraforge.auto_capture_eye"))
                && !com.terraforge.rpg.client.validation.EyeRuntimeCapture.isFinished()
                && minecraft.level != null && minecraft.player != null) {
            com.terraforge.rpg.client.validation.EyeRuntimeCapture.onClientTick(minecraft);
        }

        if (minecraft.player == null) return;

        // Open Character Menu (Key K)
        if (ModKeyMappings.OPEN_CHARACTER_MENU.consumeClick() && minecraft.screen == null) {
            PacketDistributor.sendToServer(new OpenCharacterMenuRequest());
            minecraft.setScreen(new CharacterMenuScreen());
            return;
        }

        // Only trigger abilities when in-game and not inside a screen or paused
        if (minecraft.screen == null && !minecraft.isPaused()) {
            // Activate Primary Racial Ability (Key R)
            if (ModKeyMappings.PRIMARY_ABILITY.consumeClick()) {
                PacketDistributor.sendToServer(new ActivateRaceAbilityRequest(0));
            }

            // Activate Secondary Racial Ability (Key V - Hybrids)
            if (ModKeyMappings.SECONDARY_ABILITY.consumeClick()) {
                PacketDistributor.sendToServer(new ActivateRaceAbilityRequest(1));
            }
        }
    }

    private ClientInputHandler() {}
}
