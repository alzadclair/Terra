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
 * Boomstick (Terraria 1.4.5.8 PC).
 * Early-game shotgun firing a 3-4 pellet spread with heavy knockback.
 */
public class BoomstickItem extends TerrariaGunItem {

    private static final Random RNG = new Random();

    public BoomstickItem(Properties properties) {
        super(properties, TerrariaRarity.GREEN, 20_000L, 14.0, 0.04, 5.5, 40, 0.0);
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

            int pellets = 3 + RNG.nextInt(2); // 3 to 4 pellets
            for (int i = 0; i < pellets; i++) {
                double spreadX = (RNG.nextDouble() - 0.5) * 0.18;
                double spreadY = (RNG.nextDouble() - 0.5) * 0.18;
                double spreadZ = (RNG.nextDouble() - 0.5) * 0.18;
                Vec3 shotDir = look.add(spreadX, spreadY, spreadZ).normalize();

                TerraProjectileEntity pellet = new TerraProjectileEntity(ModEntities.TERRA_PROJECTILE.get(), serverLevel);
                pellet.setPos(eyePos.x, eyePos.y - 0.1, eyePos.z);
                pellet.setOwner(player);
                pellet.setDamage(getTerrariaBaseDamage());
                pellet.setDamageClass(DamageClass.RANGED);
                pellet.setKnockback(getKnockback());
                pellet.setMaxLifeTicks(30);
                pellet.setProjectileGravity(0.01);
                pellet.setDeltaMovement(shotDir.scale(2.2));
                serverLevel.addFreshEntity(pellet);
            }

            // Consume 1 ammo
            if (!player.isCreative() && !ammoStack.isEmpty()) {
                ammoStack.shrink(1);
            }

            // Shotgun blast audio & muzzle smoke
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.9f, 1.4f);
            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, eyePos.x + look.x * 0.8, eyePos.y + look.y * 0.8, eyePos.z + look.z * 0.8,
                    8, 0.1, 0.1, 0.1, 0.03);

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
