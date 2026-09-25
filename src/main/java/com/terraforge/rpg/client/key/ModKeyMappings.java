package com.terraforge.rpg.client.key;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

/**
 * Client key mappings for TerraForge RPG.
 */
public final class ModKeyMappings {
    private static final String CATEGORY = "key.categories.terraforge_rpg";

    public static final KeyMapping OPEN_CHARACTER_MENU = new KeyMapping(
            "key.terraforge_rpg.open_character_menu",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            CATEGORY
    );

    public static final KeyMapping PRIMARY_ABILITY = new KeyMapping(
            "key.terraforge_rpg.primary_ability",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            CATEGORY
    );

    public static final KeyMapping SECONDARY_ABILITY = new KeyMapping(
            "key.terraforge_rpg.secondary_ability",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            CATEGORY
    );

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_CHARACTER_MENU);
        event.register(PRIMARY_ABILITY);
        event.register(SECONDARY_ABILITY);
    }

    private ModKeyMappings() {}
}
