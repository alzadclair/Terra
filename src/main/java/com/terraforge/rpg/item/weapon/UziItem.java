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

import java.util.Random;

/**
 * Uzi (Terraria 1.4.5.8 PC).
 * Ultra-fast automatic submachine gun that converts Musket Balls into high-velocity rounds.
 */
public class UziItem extends TerrariaGunItem {

    private static final Random RNG = new Random();

    public UziItem(Properties properties) {
        super(properties, TerrariaRarity.PINK, 250_000L, 30.0, 0.04, 3.5, 9, 0.0);
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

            // Slight spread for SMG burst
            double spreadX = (RNG.nextDouble() - 0.5) * 0.06;
            double spreadY = (RNG.nextDouble() - 0.5) * 0.06;
            double spreadZ = (RNG.nextDouble() - 0.5) * 0.06;
            Vec3 shotDir = look.add(spreadX, spreadY, spreadZ).normalize();

            TerraProjectileEntity bullet = new TerraProjectileEntity(ModEntities.TERRA_PROJECTILE.get(), serverLevel);
            bullet.setPos(eyePos.x, eyePos.y - 0.1, eyePos.z);
            bullet.setOwner(player);
            bullet.setDamage(getTerrariaBaseDamage());
            bullet.setDamageClass(DamageClass.RANGED);
            bullet.setKnockback(getKnockback());
            bullet.setMaxLifeTicks(60);
            bullet.setProjectileGravity(0.0);
            bullet.setDeltaMovement(shotDir.scale(3.6)); // High velocity
            serverLevel.addFreshEntity(bullet);

            // Ammo consumption
            if (!player.isCreative() && !ammoStack.isEmpty()) {
                ammoStack.shrink(1);
            }

            // Rapid fire audio & flash
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.DISPENSER_LAUNCH, SoundSource.PLAYERS, 0.9f, 2.0f);
            serverLevel.sendParticles(ParticleTypes.CRIT, eyePos.x + look.x * 0.8, eyePos.y + look.y * 0.8, eyePos.z + look.z * 0.8,
                    2, 0.02, 0.02, 0.02, 0.05);

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
