package com.terraforge.rpg.item.reforge;

import com.terraforge.rpg.economy.CoinHelper;
import com.terraforge.rpg.item.ITerrariaItem;
import com.terraforge.rpg.item.prefix.PrefixRegistry;
import com.terraforge.rpg.item.prefix.TerrariaPrefix;
import com.terraforge.rpg.registry.ModDataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Random;

/**
 * Server-authoritative service executing Goblin Tinkerer style item reforging.
 * Cost formula: 1/3 of current item value (BaseValue * PrefixValueMultiplier / 3).
 */
public final class ReforgeService {

    private static final Random RANDOM = new Random();

    public record ReforgeResult(
            boolean success,
            long costPaid,
            TerrariaPrefix previousPrefix,
            TerrariaPrefix newPrefix,
            String failureReason
    ) {}

    private ReforgeService() {}

    public static long calculateReforgeCost(long baseValue, double valueMultiplier) {
        long cost = Math.round(baseValue * valueMultiplier / 3.0);
        return Math.max(10L, cost);
    }

    /**
     * Calculates the reforge cost in copper coins for a given item stack.
     */
    public static long calculateReforgeCost(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof ITerrariaItem item)) {
            return 0L;
        }

        TerrariaPrefix currentPrefix = item.getPrefix(stack);
        return calculateReforgeCost(item.getBaseValue(), currentPrefix.valueMultiplier());
    }

    /**
     * Authoritatively reforges the item stack for the player.
     */
    public static ReforgeResult reforge(Player player, ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof ITerrariaItem item)) {
            return new ReforgeResult(false, 0L, TerrariaPrefix.NONE, TerrariaPrefix.NONE, "Item cannot be reforged.");
        }

        long cost = calculateReforgeCost(stack);
        if (!CoinHelper.hasEnoughCoins(player, cost)) {
            return new ReforgeResult(false, cost, item.getPrefix(stack), TerrariaPrefix.NONE, "Insufficient coins.");
        }

        // Deduct coins
        boolean deducted = CoinHelper.deductCoins(player, cost);
        if (!deducted) {
            return new ReforgeResult(false, cost, item.getPrefix(stack), TerrariaPrefix.NONE, "Transaction failed.");
        }

        TerrariaPrefix oldPrefix = item.getPrefix(stack);

        // Roll new prefix
        TerrariaPrefix newPrefix = PrefixRegistry.rollRandomPrefix(item.getPrefixCategory(), RANDOM);

        // Apply new prefix to ItemStack DataComponents
        applyPrefix(stack, item, newPrefix);

        // Increment reforge counter
        int reforgeCount = stack.getOrDefault(ModDataComponents.REFORGE_COUNT, 0);
        stack.set(ModDataComponents.REFORGE_COUNT, reforgeCount + 1);

        // Audio feedback
        if (player.level() != null && !player.level().isClientSide()) {
            player.level().playSound(
                    null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ANVIL_USE, SoundSource.PLAYERS,
                    1.0f, 0.9f + (RANDOM.nextFloat() * 0.2f)
            );
        }

        return new ReforgeResult(true, cost, oldPrefix, newPrefix, "");
    }

    /**
     * Applies a specific prefix to an ItemStack.
     */
    public static void applyPrefix(ItemStack stack, ITerrariaItem item, TerrariaPrefix prefix) {
        if (prefix == null || prefix.isNone()) {
            stack.remove(ModDataComponents.PREFIX);
            stack.remove(ModDataComponents.RARITY_OVERRIDE);
        } else {
            stack.set(ModDataComponents.PREFIX, prefix.id());
            int newRarity = item.getBaseRarity().getLevel() + prefix.rarityTierOffset();
            stack.set(ModDataComponents.RARITY_OVERRIDE, newRarity);
        }
    }
}
