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
 * Advanced sword launching beam projectiles upon swinging (e.g. Night's Edge, Excalibur, Terra Blade).
 */
public class TerraBeamSwordItem extends TerrariaSwordItem {

    private final double beamDamage;
    private final int beamPierces;
    private final float beamSpeed;

    public TerraBeamSwordItem(
            Properties properties,
            TerrariaRarity rarity,
            long value,
            double baseDamage,
            double baseCritChance,
            double knockback,
            int useTime,
            double beamDamage,
            int beamPierces,
            float beamSpeed
    ) {
        super(properties, rarity, value, baseDamage, baseCritChance, knockback, useTime);
        this.beamDamage = beamDamage;
        this.beamPierces = beamPierces;
        this.beamSpeed = beamSpeed;
    }

    public double getBeamDamage() {
        return beamDamage;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            Vec3 eyePos = player.getEyePosition();
            Vec3 look = player.getLookAngle();

            TerraProjectileEntity beam = new TerraProjectileEntity(ModEntities.TERRA_PROJECTILE.get(), serverLevel);
            beam.setPos(eyePos.x, eyePos.y, eyePos.z);
            beam.setOwner(player);
            beam.setDamageClass(DamageClass.MELEE);
            beam.setDamage(beamDamage);
            beam.setMaxPierces(beamPierces);
            beam.setProjectileGravity(0.0);
            beam.shoot(look.x, look.y, look.z, beamSpeed, 0.2f);

            serverLevel.addFreshEntity(beam);
            serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT, eyePos.x, eyePos.y, eyePos.z,
                    15, look.x * 0.5, look.y * 0.5, look.z * 0.5, 0.1);
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 1.4f);

            player.getCooldowns().addCooldown(this, getUseTime());
            return InteractionResultHolder.sidedSuccess(stack, false);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
