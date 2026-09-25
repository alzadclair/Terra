package com.terraforge.rpg.item.boss;

import com.terraforge.rpg.boss.BossScalingService;
import com.terraforge.rpg.boss.prehardmode.KingSlimeEntity;
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
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Slime Crown boss summoning item for King Slime.
 * Can be used at any time to summon King Slime immediately.
 */
public class SlimeCrownItem extends Item implements ITerrariaItem {

    public SlimeCrownItem(Properties properties) {
        super(properties);
    }

    @Override
    public TerrariaRarity getBaseRarity() {
        return TerrariaRarity.GREEN;
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
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            ServerLevel serverLevel = (ServerLevel) level;

            // Check if King Slime is already alive
            AABB arenaBox = serverPlayer.getBoundingBox().inflate(120.0);
            List<KingSlimeEntity> existing = serverLevel.getEntitiesOfClass(KingSlimeEntity.class, arenaBox);
            if (!existing.isEmpty()) {
                serverPlayer.sendSystemMessage(
                        Component.literal("King Slime is already present!").withStyle(ChatFormatting.RED)
                );
                return InteractionResultHolder.fail(stack);
            }

            int activePlayers = serverLevel.players().size();
            double scaledHp = BossScalingService.calculateScaledHealth(KingSlimeEntity.BASE_HEALTH, activePlayers);

            KingSlimeEntity king = new KingSlimeEntity(ModEntities.KING_SLIME.get(), serverLevel);
            king.setPos(serverPlayer.getX() + 10.0, serverPlayer.getY() + 5.0, serverPlayer.getZ() + 10.0);
            king.getAttribute(Attributes.MAX_HEALTH).setBaseValue(scaledHp);
            king.setHealth((float) scaledHp);
            king.setTarget(serverPlayer);
            serverLevel.addFreshEntity(king);

            serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                    Component.literal("King Slime has awoken!").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD),
                    false
            );

            serverLevel.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 2.5f, 0.6f);

            if (!serverPlayer.isCreative()) {
                stack.shrink(1);
            }

            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.literal("Summons King Slime").withStyle(ChatFormatting.BLUE));
    }
}
