package com.terraforge.rpg.item.weapon;

import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.entity.projectile.TerraProjectileEntity;
import com.terraforge.rpg.item.rarity.TerrariaRarity;
import com.terraforge.rpg.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * True Night's Edge (Terraria 1.4.5.8 PC).
 * Upgraded night blade launching a spinning green twilight beam slash that pierces 2 enemies.
 */
public class TrueNightsEdgeItem extends TerraBeamSwordItem {

    public TrueNightsEdgeItem(Properties properties) {
        super(properties, TerrariaRarity.PINK, 400_000L, 70.0, 0.04, 4.75, 26, 70.0, 2, 1.8f);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            Vec3 eyePos = player.getEyePosition();
            Vec3 look = player.getLookAngle();

            TerraProjectileEntity slash = new TerraProjectileEntity(ModEntities.TERRA_PROJECTILE.get(), serverLevel);
            slash.setPos(eyePos.x, eyePos.y, eyePos.z);
            slash.setOwner(player);
            slash.setDamage(getBeamDamage());
            slash.setDamageClass(DamageClass.MELEE);
            slash.setKnockback(getKnockback());
            slash.setMaxPierces(2);
            slash.setProjectileGravity(0.0);
            slash.setMaxLifeTicks(60);
            slash.setDeltaMovement(look.scale(1.8));
            serverLevel.addFreshEntity(slash);

            // Audio & particles
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.7f);
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, eyePos.x + look.x, eyePos.y + look.y, eyePos.z + look.z,
                    6, 0.2, 0.2, 0.2, 0.05);

            player.getCooldowns().addCooldown(this, getUseTime());
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
