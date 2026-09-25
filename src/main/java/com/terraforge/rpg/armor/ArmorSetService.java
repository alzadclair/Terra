package com.terraforge.rpg.armor;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Service evaluating equipped armor sets and applying canonical set bonuses.
 */
public final class ArmorSetService {

    private ArmorSetService() {}

    /**
     * Resolves the active ArmorSetBonus for the given player if wearing a complete matching set.
     */
    public static ArmorSetBonus getActiveSetBonus(Player player) {
        if (player == null) return ArmorSetBonus.NONE;

        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);

        if (!(head.getItem() instanceof ITerrariaArmor headArmor) ||
            !(chest.getItem() instanceof ITerrariaArmor chestArmor) ||
            !(legs.getItem() instanceof ITerrariaArmor legsArmor) ||
            !(feet.getItem() instanceof ITerrariaArmor feetArmor)) {
            return ArmorSetBonus.NONE;
        }

        String setId = headArmor.getSetId();
        if (setId == null || setId.isEmpty() ||
            !setId.equalsIgnoreCase(chestArmor.getSetId()) ||
            !setId.equalsIgnoreCase(legsArmor.getSetId()) ||
            !setId.equalsIgnoreCase(feetArmor.getSetId())) {
            return ArmorSetBonus.NONE;
        }

        return switch (setId.toLowerCase()) {
            case "copper" -> ArmorSetBonus.COPPER;
            case "iron" -> ArmorSetBonus.IRON;
            case "gold" -> ArmorSetBonus.GOLD;
            case "shadow" -> ArmorSetBonus.SHADOW;
            case "crimson" -> ArmorSetBonus.CRIMSON;
            case "meteor" -> ArmorSetBonus.METEOR;
            case "jungle" -> ArmorSetBonus.JUNGLE;
            case "necro" -> ArmorSetBonus.NECRO;
            case "molten" -> ArmorSetBonus.MOLTEN;
            case "hallowed" -> ArmorSetBonus.HALLOWED;
            default -> ArmorSetBonus.NONE;
        };
    }

    /**
     * Calculates the total effective defense granted by equipped Terraria armor pieces and set bonus.
     */
    public static int getTotalArmorDefense(Player player) {
        if (player == null) return 0;

        int total = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                ItemStack stack = player.getItemBySlot(slot);
                if (stack.getItem() instanceof ITerrariaArmor armor) {
                    total += armor.getTerrariaDefense();
                }
            }
        }

        ArmorSetBonus setBonus = getActiveSetBonus(player);
        total += setBonus.getBonusDefense();

        return total;
    }

    /**
     * Applies passive ticking effects from active armor sets (e.g. Crimson regeneration, Molten fire immunity).
     */
    public static void tickArmorEffects(Player player) {
        if (player == null || player.level().isClientSide()) return;

        ArmorSetBonus setBonus = getActiveSetBonus(player);

        if (setBonus == ArmorSetBonus.MOLTEN) {
            // Molten armor: complete immunity to burning/fire
            if (player.isOnFire()) {
                player.clearFire();
            }
        } else if (setBonus == ArmorSetBonus.CRIMSON) {
            // Crimson armor: Rapid Healing (1 HP every 20 ticks)
            if (player.tickCount % 20 == 0 && player.getHealth() < player.getMaxHealth()) {
                player.heal(1.0f);
            }
        }
    }
}
