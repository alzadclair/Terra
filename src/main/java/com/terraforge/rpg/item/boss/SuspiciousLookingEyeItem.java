package com.terraforge.rpg.item.boss;

import com.terraforge.rpg.boss.BossScalingService;
import com.terraforge.rpg.boss.prehardmode.EyeOfCthulhuEntity;
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
 * Boss summoning item for the Eye of Cthulhu.
 * Usable only at night when no other Eye of Cthulhu is active.
 */
public class SuspiciousLookingEyeItem extends Item implements ITerrariaItem {

    public SuspiciousLookingEyeItem(Properties properties) {
        super(properties);
    }

    @Override
    public TerrariaRarity getBaseRarity() {
        return TerrariaRarity.WHITE;
    }

    @Override
    public long getBaseValue() {
        return 0L; // Cannot be sold
    }

    @Override
    public TerrariaPrefixCategory getPrefixCategory() {
        return TerrariaPrefixCategory.UNIVERSAL;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            ServerLevel serverLevel = serverPlayer.serverLevel();

            // Terraria condition: must be night
            long dayTime = serverLevel.getDayTime() % 24000;
            boolean isNight = dayTime >= 13000 && dayTime <= 23000;

            if (!isNight) {
                serverPlayer.displayClientMessage(
                        Component.literal("Can only be used at night!").withStyle(ChatFormatting.RED),
                        true
                );
                return InteractionResultHolder.fail(stack);
            }

            // Check if already active
            AABB searchBox = serverPlayer.getBoundingBox().inflate(128);
            List<EyeOfCthulhuEntity> activeEyes = serverLevel.getEntitiesOfClass(EyeOfCthulhuEntity.class, searchBox, e -> e.isAlive());
            if (!activeEyes.isEmpty()) {
                serverPlayer.displayClientMessage(
                        Component.literal("The Eye of Cthulhu is already watching you!").withStyle(ChatFormatting.RED),
                        true
                );
                return InteractionResultHolder.fail(stack);
            }

            // Calculate multiplayer health scaling
            List<ServerPlayer> nearbyPlayers = serverLevel.getEntitiesOfClass(ServerPlayer.class, searchBox, Player::isAlive);
            double scaledHp = BossScalingService.calculateScaledHealth(EyeOfCthulhuEntity.BASE_HEALTH, nearbyPlayers.size());

            // Spawn boss 20 blocks above player
            EyeOfCthulhuEntity boss = new EyeOfCthulhuEntity(ModEntities.EYE_OF_CTHULHU.get(), serverLevel);
            boss.setPos(serverPlayer.getX(), serverPlayer.getY() + 20.0, serverPlayer.getZ());
            boss.getAttribute(Attributes.MAX_HEALTH).setBaseValue(scaledHp);
            boss.setHealth((float) scaledHp);
            boss.setTarget(serverPlayer);
            serverLevel.addFreshEntity(boss);

            // Audio & broadcast
            serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                    Component.literal("You feel an evil presence watching you...").withStyle(ChatFormatting.DARK_PURPLE),
                    false
            );

            serverLevel.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 2.0f, 0.7f);

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
        tooltipComponents.add(Component.literal("Summons the Eye of Cthulhu").withStyle(ChatFormatting.DARK_PURPLE));
        tooltipComponents.add(Component.literal("Usable only at night").withStyle(ChatFormatting.GRAY));
    }
}
