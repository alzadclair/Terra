package com.terraforge.rpg.item.boss;

import com.terraforge.rpg.boss.endgame.MoonLordEntity;
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
 * Celestial Sigil - Summons the Moon Lord immediately (Terraria 1.4.5.8 canonical).
 */
public class CelestialSigilItem extends Item implements ITerrariaItem {

    public CelestialSigilItem(Properties properties) {
        super(properties);
    }

    @Override public TerrariaRarity getBaseRarity() { return TerrariaRarity.CYAN; }
    @Override public long getBaseValue() { return 100_000L; } // 10 Gold
    @Override public TerrariaPrefixCategory getPrefixCategory() { return TerrariaPrefixCategory.UNIVERSAL; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            ServerLevel serverLevel = (ServerLevel) level;

            AABB arena = serverPlayer.getBoundingBox().inflate(160.0);
            if (!serverLevel.getEntitiesOfClass(MoonLordEntity.class, arena).isEmpty()) {
                player.sendSystemMessage(Component.literal("The Moon Lord is already present!").withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(stack);
            }

            // Spawn Moon Lord
            MoonLordEntity moonLord = new MoonLordEntity(ModEntities.MOON_LORD.get(), serverLevel);
            moonLord.setPos(serverPlayer.getX(), serverPlayer.getY() + 18, serverPlayer.getZ());
            serverLevel.addFreshEntity(moonLord);

            serverLevel.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    SoundEvents.ENDER_DRAGON_DEATH, SoundSource.HOSTILE, 3.0f, 0.6f);

            serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                    Component.literal("Impending doom approaches...").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
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
        tooltipComponents.add(Component.literal("Summons the Moon Lord").withStyle(ChatFormatting.BLUE));
    }
}
