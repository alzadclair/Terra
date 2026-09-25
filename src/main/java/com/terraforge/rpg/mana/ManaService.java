package com.terraforge.rpg.mana;

import com.terraforge.rpg.network.TerraNetwork;
import com.terraforge.rpg.player.data.PlayerRPGData;
import com.terraforge.rpg.registry.ModAttachments;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Server-authoritative service managing Mana consumption, natural Mana regeneration,
 * and Terraria Health modifications (Life Crystals and Life Fruits).
 */
public final class ManaService {
    private static final ResourceLocation HEALTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("terraforge_rpg", "life_crystals_fruits");

    private ManaService() {}

    /**
     * Consumes mana if the player has sufficient amount.
     * @return true if mana was successfully consumed
     */
    public static boolean consumeMana(PlayerRPGData data, double amount) {
        if (amount <= 0.0) return true;
        if (data.getCurrentMana() < amount) return false;

        data.setCurrentMana(data.getCurrentMana() - amount);
        return true;
    }

    /**
     * Restores mana up to the player's maximum.
     */
    public static void restoreMana(PlayerRPGData data, double amount) {
        if (amount <= 0.0) return;
        data.setCurrentMana(data.getCurrentMana() + amount);
    }

    /**
     * Ticks player mana regeneration and applies max health modifiers.
     */
    public static void tick(ServerPlayer player) {
        if (player == null || !player.isAlive()) return;

        PlayerRPGData data = player.getData(ModAttachments.PLAYER_RPG_DATA);

        // Natural mana regeneration
        if (data.getCurrentMana() < data.getMaxMana()) {
            double regenRate = "arcane_prism".equalsIgnoreCase(data.getEquippedSpecialAccessory()) ? 0.40 : 0.15;
            data.setCurrentMana(data.getCurrentMana() + regenRate);
        }

        // Synchronize health modifiers every second (20 ticks)
        if (player.tickCount % 20 == 0) {
            updateMaxHealth(player, data);
            TerraNetwork.sync(player);
        }
    }

    /**
     * Authoritatively updates player's Max Health based on Life Crystals and Life Fruits.
     */
    public static void updateMaxHealth(ServerPlayer player, PlayerRPGData data) {
        AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr == null) return;

        double bonusHealth = (data.getLifeCrystalsUsed() * 2.0) + (data.getLifeFruitsUsed() * 1.0);

        AttributeModifier existing = maxHealthAttr.getModifier(HEALTH_MODIFIER_ID);
        if (existing != null) {
            if (Double.compare(existing.amount(), bonusHealth) != 0) {
                maxHealthAttr.removeModifier(HEALTH_MODIFIER_ID);
                if (bonusHealth > 0.0) {
                    maxHealthAttr.addPermanentModifier(new AttributeModifier(
                            HEALTH_MODIFIER_ID, bonusHealth, AttributeModifier.Operation.ADD_VALUE));
                }
            }
        } else if (bonusHealth > 0.0) {
            maxHealthAttr.addPermanentModifier(new AttributeModifier(
                    HEALTH_MODIFIER_ID, bonusHealth, AttributeModifier.Operation.ADD_VALUE));
        }
    }
}
