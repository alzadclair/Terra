package com.terraforge.rpg.item.fishing;

import com.terraforge.rpg.economy.CoinHelper;
import com.terraforge.rpg.item.ITerrariaItem;
import com.terraforge.rpg.item.prefix.TerrariaPrefixCategory;
import com.terraforge.rpg.item.rarity.TerrariaRarity;
import com.terraforge.rpg.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Random;

/**
 * Terraria fishing crate that drops currency, ores, and consumables when opened.
 */
public class TerrariaCrateItem extends Item implements ITerrariaItem {

    public enum CrateTier {
        WOODEN(TerrariaRarity.WHITE, 1000L, 50, 150),
        IRON(TerrariaRarity.GREEN, 5000L, 200, 800),
        GOLDEN(TerrariaRarity.LIGHT_RED, 20000L, 1000, 5000);

        private final TerrariaRarity rarity;
        private final long baseValue;
        private final int minCoins;
        private final int maxCoins;

        CrateTier(TerrariaRarity rarity, long baseValue, int minCoins, int maxCoins) {
            this.rarity = rarity;
            this.baseValue = baseValue;
            this.minCoins = minCoins;
            this.maxCoins = maxCoins;
        }

        public TerrariaRarity getRarity() { return rarity; }
        public long getBaseValue() { return baseValue; }
        public int getMinCoins() { return minCoins; }
        public int getMaxCoins() { return maxCoins; }
    }

    private static final Random RANDOM = new Random();
    private final CrateTier tier;

    public TerrariaCrateItem(Properties properties, CrateTier tier) {
        super(properties);
        this.tier = tier;
    }

    public CrateTier getTier() {
        return tier;
    }

    @Override
    public TerrariaRarity getBaseRarity() {
        return tier.getRarity();
    }

    @Override
    public long getBaseValue() {
        return tier.getBaseValue();
    }

    @Override
    public TerrariaPrefixCategory getPrefixCategory() {
        return TerrariaPrefixCategory.UNIVERSAL;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            ServerLevel serverLevel = (ServerLevel) level;

            // Grant random coins
            int coins = tier.getMinCoins() + RANDOM.nextInt(tier.getMaxCoins() - tier.getMinCoins() + 1);
            CoinHelper.addCoins(serverPlayer, coins);

            // Grant items based on tier
            if (tier == CrateTier.WOODEN) {
                serverPlayer.getInventory().add(new ItemStack(ModItems.COPPER_ORE_ITEM.get(), 4 + RANDOM.nextInt(8)));
                serverPlayer.getInventory().add(new ItemStack(ModItems.WOODEN_ARROW.get(), 10 + RANDOM.nextInt(20)));
            } else if (tier == CrateTier.IRON) {
                serverPlayer.getInventory().add(new ItemStack(ModItems.LESSER_MANA_POTION.get(), 1 + RANDOM.nextInt(3)));
                serverPlayer.getInventory().add(new ItemStack(ModItems.FLAMING_ARROW.get(), 15 + RANDOM.nextInt(25)));
            } else {
                serverPlayer.getInventory().add(new ItemStack(ModItems.GOLD_COIN.get(), 1 + RANDOM.nextInt(3)));
                serverPlayer.getInventory().add(new ItemStack(ModItems.LIFE_CRYSTAL.get(), 1));
            }

            // Audio and particle feedback
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0, player.getZ(),
                    15, 0.5, 0.5, 0.5, 0.1);
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BARREL_OPEN, SoundSource.PLAYERS, 1.0f, 1.2f);

            if (!serverPlayer.isCreative()) {
                stack.shrink(1);
            }

            serverPlayer.sendSystemMessage(
                    Component.literal("Opened " + tier.name() + " Crate and received " + CoinHelper.formatCoins(coins) + " and supplies!")
                            .withStyle(ChatFormatting.GOLD)
            );

            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.literal("Right click to open").withStyle(ChatFormatting.GRAY));
    }
}
