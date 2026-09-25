package com.terraforge.rpg.item.accessory.standard;

import com.terraforge.rpg.item.accessory.TerrariaAccessoryItem;
import com.terraforge.rpg.item.rarity.TerrariaRarity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Canonical Terraria accessories providing passive movement and survivability benefits.
 */
public final class StandardTerrariaAccessories {

    public static class HermesBootsItem extends TerrariaAccessoryItem {
        public HermesBootsItem(Properties properties) {
            super(properties, TerrariaRarity.BLUE, 10_000L); // 1 Gold
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
            super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            tooltipComponents.add(Component.literal("The wearer can run super fast").withStyle(ChatFormatting.BLUE));
        }
    }

    public static class BandOfRegenerationItem extends TerrariaAccessoryItem {
        public BandOfRegenerationItem(Properties properties) {
            super(properties, TerrariaRarity.BLUE, 10_000L); // 1 Gold
        }

        @Override
        public void tickAccessory(Player player, ItemStack stack) {
            super.tickAccessory(player, stack);
            if (!player.level().isClientSide() && player.tickCount % 20 == 0 && player.getHealth() < player.getMaxHealth()) {
                player.heal(1.0f);
            }
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
            super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            tooltipComponents.add(Component.literal("Slowly regenerates life").withStyle(ChatFormatting.BLUE));
        }
    }

    public static class TerrasparkBootsItem extends TerrariaAccessoryItem {
        public TerrasparkBootsItem(Properties properties) {
            super(properties, TerrariaRarity.PINK, 100_000L); // 10 Gold
        }

        @Override
        public void tickAccessory(Player player, ItemStack stack) {
            super.tickAccessory(player, stack);
            player.fallDistance = 0.0f;
            if (player.isOnFire()) {
                player.clearFire();
            }
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
            super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            tooltipComponents.add(Component.literal("Provides lightning sprint, flight boost, and immunity to lava").withStyle(ChatFormatting.BLUE));
        }
    }

    private StandardTerrariaAccessories() {}
}
