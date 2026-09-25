package com.terraforge.rpg.item.hook;

import com.terraforge.rpg.item.prefix.TerrariaPrefixCategory;
import com.terraforge.rpg.item.rarity.TerrariaRarity;
import com.terraforge.rpg.entity.projectile.GrapplingHookEntity;
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
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Terraria Grappling Hook item.
 * Fires a grappling hook that pulls the player toward solid blocks.
 */
public class GrapplingHookItem extends Item implements ITerrariaHook {

    private final double range;
    private final double pullVelocity;
    private final int maxHooks;
    private final TerrariaRarity rarity;
    private final long value;

    public GrapplingHookItem(
            Properties properties,
            double range,
            double pullVelocity,
            int maxHooks,
            TerrariaRarity rarity,
            long value
    ) {
        super(properties);
        this.range = range;
        this.pullVelocity = pullVelocity;
        this.maxHooks = maxHooks;
        this.rarity = rarity;
        this.value = value;
    }

    @Override
    public double getHookRange() { return range; }

    @Override
    public double getPullVelocity() { return pullVelocity; }

    @Override
    public int getMaxHooks() { return maxHooks; }

    @Override
    public TerrariaRarity getBaseRarity() { return rarity; }

    @Override
    public long getBaseValue() { return value; }

    @Override
    public TerrariaPrefixCategory getPrefixCategory() { return TerrariaPrefixCategory.UNIVERSAL; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            Vec3 eyePos = player.getEyePosition();
            Vec3 look = player.getLookAngle();

            GrapplingHookEntity hook = new GrapplingHookEntity(serverLevel, player, range, pullVelocity);
            hook.setPos(eyePos.x, eyePos.y, eyePos.z);
            hook.shoot(look.x, look.y, look.z, 2.0f, 0.0f);

            serverLevel.addFreshEntity(hook);
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0f, 1.6f);

            return InteractionResultHolder.sidedSuccess(stack, false);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.literal("Reach: " + (int) range + " blocks").withStyle(ChatFormatting.BLUE));
    }
}
