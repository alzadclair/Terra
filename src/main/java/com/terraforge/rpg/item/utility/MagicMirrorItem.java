package com.terraforge.rpg.item.utility;

import com.terraforge.rpg.economy.CoinHelper;
import com.terraforge.rpg.item.ITerrariaItem;
import com.terraforge.rpg.item.prefix.TerrariaPrefixCategory;
import com.terraforge.rpg.item.rarity.TerrariaRarity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Magic Mirror iconic Terraria utility item.
 * Teleports the player back to their spawn point / bed upon channeled completion.
 */
public class MagicMirrorItem extends Item implements ITerrariaItem {

    public MagicMirrorItem(Properties properties) {
        super(properties);
    }

    @Override
    public TerrariaRarity getBaseRarity() {
        return TerrariaRarity.BLUE;
    }

    @Override
    public long getBaseValue() {
        return 10_000L; // 1 Gold
    }

    @Override
    public TerrariaPrefixCategory getPrefixCategory() {
        return TerrariaPrefixCategory.UNIVERSAL;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 20; // 1 second channeling
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            ServerLevel respawnLevel = player.server.getLevel(player.getRespawnDimension());
            if (respawnLevel == null) respawnLevel = player.server.overworld();

            BlockPos respawnPos = player.getRespawnPosition();
            if (respawnPos == null) {
                respawnPos = respawnLevel.getSharedSpawnPos();
            }

            // Teleport to spawn
            player.teleportTo(
                    respawnLevel,
                    respawnPos.getX() + 0.5,
                    respawnPos.getY() + 0.1,
                    respawnPos.getZ() + 0.5,
                    player.getYRot(),
                    player.getXRot()
            );

            // Audio & visual feedback
            respawnLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 1.0f, 1.3f);

            respawnLevel.sendParticles(
                    ParticleTypes.PORTAL,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    30, 0.5, 0.5, 0.5, 0.1
            );
        }

        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.literal("Gaze into the mirror to return home").withStyle(ChatFormatting.BLUE));
        tooltipComponents.add(Component.literal("Price: " + CoinHelper.formatCoins(getBaseValue())).withStyle(ChatFormatting.GOLD));
    }
}
