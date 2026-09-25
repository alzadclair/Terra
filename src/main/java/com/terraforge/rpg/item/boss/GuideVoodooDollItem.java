package com.terraforge.rpg.item.boss;

import com.terraforge.rpg.boss.BossScalingService;
import com.terraforge.rpg.boss.prehardmode.WallOfFleshEntity;
import com.terraforge.rpg.item.ITerrariaItem;
import com.terraforge.rpg.item.prefix.TerrariaPrefixCategory;
import com.terraforge.rpg.item.rarity.TerrariaRarity;
import com.terraforge.rpg.registry.ModEntities;
import com.terraforge.rpg.world.layer.TerrariaLayers;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Guide Voodoo Doll summoning item for the Wall of Flesh.
 * Must be sacrificed in lava within the Underworld layer (Y < -30).
 */
public class GuideVoodooDollItem extends Item implements ITerrariaItem {

    public GuideVoodooDollItem(Properties properties) {
        super(properties);
    }

    @Override
    public TerrariaRarity getBaseRarity() {
        return TerrariaRarity.WHITE;
    }

    @Override
    public long getBaseValue() {
        return 200L; // 2 Silver
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

            // Check if player is in the Underworld
            if (TerrariaLayers.getLayer((int) serverPlayer.getY()) != TerrariaLayers.UNDERWORLD) {
                serverPlayer.sendSystemMessage(
                        Component.literal("The doll remains inert. It must be sacrificed in the lava of the Underworld.")
                                .withStyle(ChatFormatting.RED)
                );
                return InteractionResultHolder.fail(stack);
            }

            // Check if lava is nearby within 5 blocks
            boolean nearLava = false;
            BlockPos playerPos = serverPlayer.blockPosition();
            for (BlockPos check : BlockPos.betweenClosed(playerPos.offset(-4, -4, -4), playerPos.offset(4, 4, 4))) {
                if (serverLevel.getFluidState(check).is(Fluids.LAVA) || serverLevel.getFluidState(check).is(Fluids.FLOWING_LAVA)) {
                    nearLava = true;
                    break;
                }
            }

            if (!nearLava) {
                serverPlayer.sendSystemMessage(
                        Component.literal("The Guide Voodoo Doll must be sacrificed in or directly next to lava!")
                                .withStyle(ChatFormatting.YELLOW)
                );
                return InteractionResultHolder.fail(stack);
            }

            // Check if Wall of Flesh is already present
            AABB arenaBox = serverPlayer.getBoundingBox().inflate(150.0);
            List<WallOfFleshEntity> existing = serverLevel.getEntitiesOfClass(WallOfFleshEntity.class, arenaBox);
            if (!existing.isEmpty()) {
                serverPlayer.sendSystemMessage(
                        Component.literal("The Wall of Flesh is already consuming this realm!")
                                .withStyle(ChatFormatting.RED)
                );
                return InteractionResultHolder.fail(stack);
            }

            // Calculate multiplayer health scaling
            int activePlayers = serverLevel.players().size();
            double scaledHp = BossScalingService.calculateScaledHealth(WallOfFleshEntity.BASE_HEALTH, activePlayers);

            // Spawn Wall of Flesh 30 blocks away horizontally
            WallOfFleshEntity wof = new WallOfFleshEntity(ModEntities.WALL_OF_FLESH.get(), serverLevel);
            double spawnX = serverPlayer.getX() + 30.0;
            wof.setPos(spawnX, serverPlayer.getY(), serverPlayer.getZ());
            wof.setSweepDirection(new Vec3(-1.0, 0.0, 0.0));
            wof.getAttribute(Attributes.MAX_HEALTH).setBaseValue(scaledHp);
            wof.setHealth((float) scaledHp);
            wof.setTarget(serverPlayer);
            serverLevel.addFreshEntity(wof);

            // Broadcast message
            serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                    Component.literal("Wall of Flesh has awoken!").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD),
                    false
            );

            serverLevel.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 2.0f, 0.7f);

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
        tooltipComponents.add(Component.literal("You are a terrible person").withStyle(ChatFormatting.DARK_PURPLE));
        tooltipComponents.add(Component.literal("Sacrifice in Underworld lava to summon the Wall of Flesh").withStyle(ChatFormatting.GRAY));
    }
}
