package com.terraforge.rpg.item.fishing;

import com.terraforge.rpg.boss.endgame.DukeFishronEntity;
import com.terraforge.rpg.item.rarity.TerrariaRarity;
import com.terraforge.rpg.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Truffle Worm - Ultra rare underground glowing mushroom critter (Terraria 1.4.5.8 canonical).
 * Has 666% Bait Power. Summons Duke Fishron when cast in ocean waters.
 */
public class TruffleWormItem extends TerrariaBaitItem {

    public TruffleWormItem(Properties properties) {
        super(properties, 666, TerrariaRarity.YELLOW, 100_000L); // 666% bait power, 10 Gold
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            ServerLevel serverLevel = (ServerLevel) level;

            AABB arena = serverPlayer.getBoundingBox().inflate(128.0);
            if (!serverLevel.getEntitiesOfClass(DukeFishronEntity.class, arena).isEmpty()) {
                player.sendSystemMessage(Component.literal("Duke Fishron is already lurking!").withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(stack);
            }

            // Spawn Duke Fishron
            DukeFishronEntity fishron = new DukeFishronEntity(ModEntities.DUKE_FISHRON.get(), serverLevel);
            fishron.setPos(serverPlayer.getX() + 15, serverPlayer.getY() + 5, serverPlayer.getZ() + 15);
            serverLevel.addFreshEntity(fishron);

            serverLevel.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, 2.0f, 0.8f);

            serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                    Component.literal("Duke Fishron has surfaced!").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD),
                    false
            );

            if (!serverPlayer.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.literal("Used as bait in the ocean to summon Duke Fishron").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
