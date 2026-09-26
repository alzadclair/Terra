package com.terraforge.rpg.item.weapon;

import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.entity.projectile.TerraProjectileEntity;
import com.terraforge.rpg.item.ammo.AmmoType;
import com.terraforge.rpg.item.ammo.ITerrariaAmmo;
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
 * Phoenix Blaster (Terraria 1.4.5.8 PC).
 * High-velocity handgun firing flaming rounds that pierce and ignite targets.
 */
public class PhoenixBlasterItem extends TerrariaGunItem {

    public PhoenixBlasterItem(Properties properties) {
        super(properties, TerrariaRarity.ORANGE, 100_000L, 24.0, 0.04, 4.0, 17, 0.0);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack gunStack = player.getItemInHand(hand);

        ItemStack ammoStack = findAmmo(player, AmmoType.BULLET);
        if (!player.isCreative() && ammoStack.isEmpty()) {
            return InteractionResultHolder.fail(gunStack);
        }

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            Vec3 eyePos = player.getEyePosition();
            Vec3 look = player.getLookAngle();

            TerraProjectileEntity bullet = new TerraProjectileEntity(ModEntities.TERRA_PROJECTILE.get(), serverLevel);
            bullet.setPos(eyePos.x, eyePos.y - 0.1, eyePos.z);
            bullet.setOwner(player);
            bullet.setDamage(getTerrariaBaseDamage());
            bullet.setDamageClass(DamageClass.RANGED);
            bullet.setKnockback(getKnockback());
            bullet.setMaxPierces(1);
            bullet.setProjectileGravity(0.0);
            bullet.setMaxLifeTicks(60);
            bullet.setDeltaMovement(look.scale(3.2));
            serverLevel.addFreshEntity(bullet);

            // Consume ammo
            if (!player.isCreative() && !ammoStack.isEmpty()) {
                ammoStack.shrink(1);
            }

            // Audio & fire particles
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.9f, 1.3f);
            serverLevel.sendParticles(ParticleTypes.FLAME, eyePos.x + look.x * 0.8, eyePos.y + look.y * 0.8, eyePos.z + look.z * 0.8,
                    4, 0.05, 0.05, 0.05, 0.02);

            player.getCooldowns().addCooldown(this, getUseTime());
        }

        return InteractionResultHolder.sidedSuccess(gunStack, level.isClientSide());
    }

    private ItemStack findAmmo(Player player, AmmoType requiredType) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ITerrariaAmmo ammo) {
                if (ammo.getAmmoType() == requiredType) {
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }
}
