package com.terraforge.rpg.economy;

import com.terraforge.rpg.registry.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Server-authoritative utility for Terraria 1.4.5.8 coin economy transactions.
 * Exchange rates:
 * 1 Platinum = 100 Gold = 10,000 Silver = 1,000,000 Copper
 */
public final class CoinHelper {

    public static final long COPPER_PER_SILVER = 100L;
    public static final long COPPER_PER_GOLD = 10_000L;
    public static final long COPPER_PER_PLATINUM = 1_000_000L;

    private CoinHelper() {}

    /**
     * Counts the total value of coins in a player's inventory, expressed in copper coins.
     */
    public static long countTotalCopper(Player player) {
        if (player == null) return 0L;

        long total = 0L;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.isEmpty()) continue;

            if (stack.is(ModItems.COPPER_COIN.get())) {
                total += stack.getCount();
            } else if (stack.is(ModItems.SILVER_COIN.get())) {
                total += stack.getCount() * COPPER_PER_SILVER;
            } else if (stack.is(ModItems.GOLD_COIN.get())) {
                total += stack.getCount() * COPPER_PER_GOLD;
            } else if (stack.is(ModItems.PLATINUM_COIN.get())) {
                total += stack.getCount() * COPPER_PER_PLATINUM;
            }
        }
        return total;
    }

    /**
     * Checks if the player has at least the required amount of copper coins.
     */
    public static boolean hasEnoughCoins(Player player, long copperRequired) {
        return countTotalCopper(player) >= copperRequired;
    }

    /**
     * Deducts coins from the player's inventory authoritatively, converting change cleanly.
     * @return true if coins were successfully deducted, false if insufficient funds
     */
    public static boolean deductCoins(Player player, long copperToDeduct) {
        if (copperToDeduct <= 0) return true;
        if (!hasEnoughCoins(player, copperToDeduct)) return false;

        long currentTotal = countTotalCopper(player);
        long remaining = currentTotal - copperToDeduct;

        // Clear existing coins from inventory
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (stack.is(ModItems.COPPER_COIN.get()) ||
                stack.is(ModItems.SILVER_COIN.get()) ||
                stack.is(ModItems.GOLD_COIN.get()) ||
                stack.is(ModItems.PLATINUM_COIN.get())) {
                player.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }

        // Grant back the change
        addCoins(player, remaining);
        return true;
    }

    /**
     * Distributes copper coins into the player's inventory as optimal platinum, gold, silver, copper stacks.
     */
    public static void addCoins(Player player, long copperToAdd) {
        if (player == null || copperToAdd <= 0) return;

        long platinum = copperToAdd / COPPER_PER_PLATINUM;
        long remAfterPlat = copperToAdd % COPPER_PER_PLATINUM;

        long gold = remAfterPlat / COPPER_PER_GOLD;
        long remAfterGold = remAfterPlat % COPPER_PER_GOLD;

        long silver = remAfterGold / COPPER_PER_SILVER;
        long copper = remAfterGold % COPPER_PER_SILVER;

        giveCoinItem(player, ModItems.PLATINUM_COIN.get(), platinum);
        giveCoinItem(player, ModItems.GOLD_COIN.get(), gold);
        giveCoinItem(player, ModItems.SILVER_COIN.get(), silver);
        giveCoinItem(player, ModItems.COPPER_COIN.get(), copper);
    }

    private static void giveCoinItem(Player player, net.minecraft.world.item.Item coinItem, long count) {
        while (count > 0) {
            int stackCount = (int) Math.min(count, 99);
            ItemStack stack = new ItemStack(coinItem, stackCount);
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
            count -= stackCount;
        }
    }

    /**
     * Formats a copper amount into readable Terraria coin strings.
     */
    public static String formatCoins(long copperTotal) {
        if (copperTotal <= 0) return "0 Copper";

        long platinum = copperTotal / COPPER_PER_PLATINUM;
        long remPlat = copperTotal % COPPER_PER_PLATINUM;

        long gold = remPlat / COPPER_PER_GOLD;
        long remGold = remPlat % COPPER_PER_GOLD;

        long silver = remGold / COPPER_PER_SILVER;
        long copper = remGold % COPPER_PER_SILVER;

        StringBuilder sb = new StringBuilder();
        if (platinum > 0) sb.append(platinum).append(" Platinum ");
        if (gold > 0) sb.append(gold).append(" Gold ");
        if (silver > 0) sb.append(silver).append(" Silver ");
        if (copper > 0 || sb.isEmpty()) sb.append(copper).append(" Copper");

        return sb.toString().trim();
    }
}
