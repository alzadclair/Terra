package com.terraforge.rpg.item.mount;

import com.terraforge.rpg.item.prefix.TerrariaPrefixCategory;
import com.terraforge.rpg.item.rarity.TerrariaRarity;
import com.terraforge.rpg.entity.mount.SlimeMountEntity;
import com.terraforge.rpg.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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

/**
 * Slimy Saddle mount summoning item.
 * Drops from King Slime (25% chance).
 * Summons a bouncy Slime mount granting high jumps and fall damage immunity.
 */
public class SlimySaddleItem extends Item implements ITerrariaMount {

    public SlimySaddleItem(Properties properties) {
        super(properties);
    }

    @Override
    public double getMountSpeed() { return 0.35; }

    @Override
    public double getJumpHeightBoost() { return 0.65; }

    @Override
    public boolean hasFallDamageImmunity() { return true; }

    @Override
    public boolean canFloatOnLiquids() { return true; }

    @Override
    public TerrariaRarity getBaseRarity() { return TerrariaRarity.GREEN; }

    @Override
    public long getBaseValue() { return 50_000L; } // 5 Gold

    @Override
    public TerrariaPrefixCategory getPrefixCategory() { return TerrariaPrefixCategory.UNIVERSAL; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            if (player.getVehicle() instanceof SlimeMountEntity currentMount) {
                // Dismount
                player.stopRiding();
                currentMount.discard();
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS, 1.0f, 1.2f);
                return InteractionResultHolder.sidedSuccess(stack, false);
            }

            // Spawn and ride slime mount
            SlimeMountEntity mount = new SlimeMountEntity(ModEntities.SLIME_MOUNT.get(), serverLevel);
            mount.setPos(player.getX(), player.getY(), player.getZ());
            serverLevel.addFreshEntity(mount);
            player.startRiding(mount);

            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SLIME_JUMP, SoundSource.PLAYERS, 1.2f, 0.8f);

            return InteractionResultHolder.sidedSuccess(stack, false);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.literal("Summons a rideable slime mount").withStyle(ChatFormatting.BLUE));
        tooltipComponents.add(Component.literal("Grants high bounce and fall damage immunity").withStyle(ChatFormatting.GRAY));
    }
}
