package com.terraforge.rpg.item.weapon;

import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.entity.projectile.TerraProjectileEntity;
import com.terraforge.rpg.item.prefix.TerrariaPrefix;
import com.terraforge.rpg.item.prefix.TerrariaPrefixCategory;
import com.terraforge.rpg.item.rarity.TerrariaRarity;
import com.terraforge.rpg.item.tooltip.TerrariaTooltipHelper;
import com.terraforge.rpg.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
import java.util.Random;

/**
 * Celebration Mk2 (Terraria 1.4.5.8 PC).
 * Heavy endgame rocket launcher firing festive multicolored explosive fireworks.
 */
public class CelebrationMk2Item extends TerrariaGunItem {

    private static final Random RNG = new Random();

    public CelebrationMk2Item(Properties properties) {
        super(properties, TerrariaRarity.RED, 100_000L, 40.0, 0.10, 5.0, 30, 0.0);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            Vec3 eyePos = player.getEyePosition();
            Vec3 look = player.getLookAngle();

            // Fires a cluster of 3 festive rockets
            for (int i = 0; i < 3; i++) {
                double spreadX = (RNG.nextDouble() - 0.5) * 0.12;
                double spreadY = (RNG.nextDouble() - 0.5) * 0.12;
                double spreadZ = (RNG.nextDouble() - 0.5) * 0.12;
                Vec3 rocketDir = look.add(spreadX, spreadY, spreadZ).normalize();

                TerraProjectileEntity rocket = new TerraProjectileEntity(ModEntities.TERRA_PROJECTILE.get(), serverLevel);
                rocket.setPos(eyePos.x, eyePos.y - 0.1, eyePos.z);
                rocket.setOwner(player);
                rocket.setDamage(getTerrariaBaseDamage() * 1.5);
                rocket.setDamageClass(DamageClass.RANGED);
                rocket.setKnockback(getKnockback());
                rocket.setMaxLifeTicks(80);
                rocket.setProjectileGravity(0.01);
                rocket.setDeltaMovement(rocketDir.scale(2.5));
                serverLevel.addFreshEntity(rocket);
            }

            // Festive audio and rainbow sparkles
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 1.2f, 1.0f);
            serverLevel.sendParticles(ParticleTypes.FIREWORK, eyePos.x + look.x, eyePos.y + look.y, eyePos.z + look.z,
                    12, 0.2, 0.2, 0.2, 0.08);

            player.getCooldowns().addCooldown(this, getUseTime());
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        TerrariaTooltipHelper.buildTooltip(stack, this, tooltip);
        tooltip.add(Component.literal("Lança um festival de fogos de artifício explosivos").withStyle(ChatFormatting.GOLD));
    }
}
