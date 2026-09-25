package com.terraforge.rpg.item.utility;

import com.terraforge.rpg.item.ITerrariaItem;
import com.terraforge.rpg.item.prefix.TerrariaPrefixCategory;
import com.terraforge.rpg.item.rarity.TerrariaRarity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Portal Gun - Iconic endgame tool dropped by the Moon Lord (Terraria 1.4.5.8 canonical).
 * Teleports the player to aimed surfaces with portal FX and high terminal velocity.
 */
public class PortalGunItem extends Item implements ITerrariaItem {

    public PortalGunItem(Properties properties) {
        super(properties);
    }

    @Override public TerrariaRarity getBaseRarity() { return TerrariaRarity.RED; }
    @Override public long getBaseValue() { return 100_000L; } // 10 Gold
    @Override public TerrariaPrefixCategory getPrefixCategory() { return TerrariaPrefixCategory.UNIVERSAL; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            Vec3 eyePos = player.getEyePosition();
            Vec3 look = player.getLookAngle();
            Vec3 reach = eyePos.add(look.scale(32.0));

            BlockHitResult hit = level.clip(new ClipContext(eyePos, reach, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            if (hit.getType() == HitResult.Type.BLOCK) {
                Vec3 targetPos = hit.getLocation().add(hit.getDirection().step().x() * 0.5,
                        hit.getDirection().step().y() * 0.5 + 0.1,
                        hit.getDirection().step().z() * 0.5);

                // Teleport
                serverLevel.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 20, 0.5, 0.5, 0.5, 0.2);
                player.teleportTo(targetPos.x, targetPos.y, targetPos.z);
                player.fallDistance = 0.0f;

                serverLevel.sendParticles(ParticleTypes.PORTAL, targetPos.x, targetPos.y + 1.0, targetPos.z, 20, 0.5, 0.5, 0.5, 0.2);
                serverLevel.playSound(null, targetPos.x, targetPos.y, targetPos.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.2f, 1.4f);

                player.getCooldowns().addCooldown(this, 15);
                return InteractionResultHolder.success(stack);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.literal("Right click to shoot quantum portals and travel across space").withStyle(ChatFormatting.AQUA));
    }
}
