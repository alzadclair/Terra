package com.terraforge.rpg.accessory.special;

import com.terraforge.rpg.network.TerraNetwork;
import com.terraforge.rpg.player.data.PlayerRPGData;
import com.terraforge.rpg.race.RaceAssignmentService;
import com.terraforge.rpg.registry.ModAttachments;
import com.terraforge.rpg.registry.ModItems;
import com.terraforge.rpg.util.TerraLogger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

/**
 * Server-authoritative service handling equipping, unequipping, and transaction safety
 * for the unique Special Accessory slot.
 */
public final class SpecialAccessoryService {
    private static final Set<String> VALID_ACCESSORIES = Set.of(
            "phoenix_wings", "thunder_fragment", "void_heart", "titan_core", "arcane_prism",
            "guardian_seal", "blood_crystal", "time_gear", "predator_eye", "gravity_sigil"
    );

    private SpecialAccessoryService() {}

    public static boolean isValidAccessory(String id) {
        if (id == null) return false;
        return VALID_ACCESSORIES.contains(id.toLowerCase());
    }

    /**
     * Equips a special accessory from the player's inventory safely without duplication.
     */
    public static boolean equip(ServerPlayer player, String accessoryId) {
        if (player == null || !isValidAccessory(accessoryId)) return false;

        PlayerRPGData data = player.getData(ModAttachments.PLAYER_RPG_DATA);

        // Find the item in player inventory
        int targetSlot = -1;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (key.getPath().equalsIgnoreCase(accessoryId)) {
                targetSlot = i;
                break;
            }
        }

        if (targetSlot == -1) {
            TerraLogger.warn("ACCESSORY", "Player {} attempted to equip {} without having it in inventory!",
                    player.getName().getString(), accessoryId);
            return false;
        }

        // Unequip existing accessory first if present
        if (!data.getEquippedSpecialAccessory().isEmpty()) {
            unequip(player);
        }

        // Consume 1 item from inventory
        player.getInventory().removeItem(targetSlot, 1);
        data.setEquippedSpecialAccessory(accessoryId.toLowerCase());

        // Update flight and sync
        RaceAssignmentService.updateFlightPermissions(player);
        TerraNetwork.sync(player);
        TerraLogger.info("ACCESSORY", "Player {} equipped special accessory {}",
                player.getName().getString(), accessoryId);
        return true;
    }

    /**
     * Unequips the currently equipped special accessory, returning it to inventory or safely dropping it.
     */
    public static boolean unequip(ServerPlayer player) {
        if (player == null) return false;

        PlayerRPGData data = player.getData(ModAttachments.PLAYER_RPG_DATA);
        String current = data.getEquippedSpecialAccessory();
        if (current.isEmpty()) return false;

        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("terraforge_rpg", current));
        if (item != null) {
            ItemStack stack = new ItemStack(item);
            boolean added = player.getInventory().add(stack);
            if (!added) {
                player.drop(stack, false);
            }
        }

        data.setEquippedSpecialAccessory("");
        RaceAssignmentService.updateFlightPermissions(player);
        TerraNetwork.sync(player);
        TerraLogger.info("ACCESSORY", "Player {} unequipped special accessory {}",
                player.getName().getString(), current);
        return true;
    }
}
