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
 * Seedler (Terraria 1.4.5.8 PC).
 * Melee sword that fires a Seedler Nut projectile exploding into high-velocity thorn shards.
 */
public class SeedlerItem extends TerrariaSwordItem {

    public SeedlerItem(Properties properties) {
        super(properties, TerrariaRarity.PINK, 300_000L, 50.0, 0.04, 6.0, 28);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            Vec3 eyePos = player.getEyePosition();
            Vec3 look = player.getLookAngle();

            // Main Seedler Pod
            TerraProjectileEntity pod = new TerraProjectileEntity(ModEntities.TERRA_PROJECTILE.get(), serverLevel);
            pod.setPos(eyePos.x, eyePos.y, eyePos.z);
            pod.setOwner(player);
            pod.setDamage(getTerrariaBaseDamage());
            pod.setDamageClass(DamageClass.MELEE);
            pod.setKnockback(getKnockback());
            pod.setProjectileGravity(0.02);
            pod.setMaxLifeTicks(40);
            pod.setDeltaMovement(look.scale(1.4));
            serverLevel.addFreshEntity(pod);

            // Audio & muzzle particles
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SNOW_GOLEM_SHOOT, SoundSource.PLAYERS, 1.0f, 0.8f);
            serverLevel.sendParticles(ParticleTypes.ITEM_SLIME, eyePos.x + look.x, eyePos.y + look.y, eyePos.z + look.z,
                    5, 0.1, 0.1, 0.1, 0.05);

            player.getCooldowns().addCooldown(this, getUseTime());
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
