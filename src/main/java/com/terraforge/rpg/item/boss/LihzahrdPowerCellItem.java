package com.terraforge.rpg.item.boss;

import com.terraforge.rpg.boss.endgame.GolemEntity;
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
 * Lihzahrd Power Cell - Activates the Lihzahrd Altar to summon Golem (Terraria 1.4.5.8 canonical).
 */
public class LihzahrdPowerCellItem extends Item implements ITerrariaItem {

    public LihzahrdPowerCellItem(Properties properties) {
        super(properties);
    }

    @Override public TerrariaRarity getBaseRarity() { return TerrariaRarity.YELLOW; }
    @Override public long getBaseValue() { return 30_000L; } // 3 Gold
    @Override public TerrariaPrefixCategory getPrefixCategory() { return TerrariaPrefixCategory.UNIVERSAL; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            ServerLevel serverLevel = (ServerLevel) level;

            AABB arena = serverPlayer.getBoundingBox().inflate(96.0);
            if (!serverLevel.getEntitiesOfClass(GolemEntity.class, arena).isEmpty()) {
                player.sendSystemMessage(Component.literal("Golem is already active!").withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(stack);
            }

            // Spawn Golem
            GolemEntity golem = new GolemEntity(ModEntities.GOLEM.get(), serverLevel);
            golem.setPos(serverPlayer.getX(), serverPlayer.getY() + 1.0, serverPlayer.getZ());
            serverLevel.addFreshEntity(golem);

            serverLevel.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    SoundEvents.IRON_GOLEM_REPAIR, SoundSource.HOSTILE, 2.0f, 0.5f);

            serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                    Component.literal("The ancient machinery whirs to life... Golem has awakened!").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
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
        tooltipComponents.add(Component.literal("Summons Golem").withStyle(ChatFormatting.BLUE));
    }
}
