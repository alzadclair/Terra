package com.terraforge.rpg.block;

import com.terraforge.rpg.item.weapon.PwnhammerItem;
import com.terraforge.rpg.world.progression.WorldProgressionData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Demon / Crimson Altar block.
 * Can only be smashed in Hardmode using a hammer with >= 80% hammer power (e.g. Pwnhammer).
 * Striking it in Pre-Hardmode inflicts 50% max HP damage to the player as retribution.
 */
public class DemonAltarBlock extends Block {

    private final boolean crimson;

    public DemonAltarBlock(boolean crimson) {
        super(BlockBehaviour.Properties.of()
                .strength(5.0f, 1200.0f)
                .sound(SoundType.STONE)
                .lightLevel(state -> 4));
        this.crimson = crimson;
    }

    public boolean isCrimson() {
        return crimson;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            ServerLevel serverLevel = (ServerLevel) level;
            WorldProgressionData progression = WorldProgressionData.get(serverLevel.getServer());
            ItemStack heldItem = serverPlayer.getMainHandItem();

            boolean hasHammer = heldItem.getItem() instanceof PwnhammerItem;

            if (!progression.isHardmode()) {
                // Pre-Hardmode: Retribution damage!
                serverPlayer.hurt(serverPlayer.damageSources().magic(), serverPlayer.getMaxHealth() * 0.5f);
                serverPlayer.sendSystemMessage(Component.literal("You are not worthy yet!")
                        .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                level.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                        SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.BLOCKS, 1.0f, 1.0f);
            } else if (hasHammer) {
                // Hardmode + Pwnhammer: Altar smashed!
                progression.smashAltar(serverLevel, pos);
            } else {
                serverPlayer.sendSystemMessage(Component.literal("You need a stronger hammer to break this altar!")
                        .withStyle(ChatFormatting.YELLOW));
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
