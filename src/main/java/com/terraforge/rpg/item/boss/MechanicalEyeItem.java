package com.terraforge.rpg.item.boss;

import com.terraforge.rpg.boss.hardmode.RetinazerEntity;
import com.terraforge.rpg.boss.hardmode.SpazmatismEntity;
import com.terraforge.rpg.item.ITerrariaItem;
import com.terraforge.rpg.item.prefix.TerrariaPrefixCategory;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Mechanical Eye - Summons The Twins (Retinazer and Spazmatism) at night (Terraria 1.4.5.8 canonical).
 */
public class MechanicalEyeItem extends Item implements ITerrariaItem {

    public MechanicalEyeItem(Properties properties) {
        super(properties);
    }

    @Override public TerrariaRarity getBaseRarity() { return TerrariaRarity.ORANGE; }
    @Override public long getBaseValue() { return 20_000L; } // 2 Gold
    @Override public TerrariaPrefixCategory getPrefixCategory() { return TerrariaPrefixCategory.UNIVERSAL; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            ServerLevel serverLevel = (ServerLevel) level;

            long dayTime = serverLevel.getDayTime() % 24000;
            if (dayTime < 13000 || dayTime > 23500) {
                player.sendSystemMessage(Component.literal("The Mechanical Eye only works at night.").withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(stack);
            }

            AABB arena = serverPlayer.getBoundingBox().inflate(128.0);
            if (!serverLevel.getEntitiesOfClass(RetinazerEntity.class, arena).isEmpty() ||
                !serverLevel.getEntitiesOfClass(SpazmatismEntity.class, arena).isEmpty()) {
                player.sendSystemMessage(Component.literal("The Twins are already awake!").withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(stack);
            }

            // Spawn Retinazer
            RetinazerEntity retinazer = new RetinazerEntity(ModEntities.RETINAZER.get(), serverLevel);
            retinazer.setPos(serverPlayer.getX() + 15, serverPlayer.getY() + 12, serverPlayer.getZ());
            serverLevel.addFreshEntity(retinazer);

            // Spawn Spazmatism
            SpazmatismEntity spazmatism = new SpazmatismEntity(ModEntities.SPAZMATISM.get(), serverLevel);
            spazmatism.setPos(serverPlayer.getX() - 15, serverPlayer.getY() + 12, serverPlayer.getZ());
            serverLevel.addFreshEntity(spazmatism);

            serverLevel.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.5f, 1.2f);

            serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                    Component.literal("This is going to be a terrible night...").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD),
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
        tooltipComponents.add(Component.literal("Summons The Twins").withStyle(ChatFormatting.BLUE));
        tooltipComponents.add(Component.literal("Can only be used at night").withStyle(ChatFormatting.GRAY));
    }
}
