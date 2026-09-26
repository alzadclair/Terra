package com.terraforge.rpg.item.weapon;

import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.entity.projectile.TerraProjectileEntity;
import com.terraforge.rpg.item.ammo.AmmoType;
import com.terraforge.rpg.item.ammo.ITerrariaAmmo;
import com.terraforge.rpg.item.rarity.TerrariaRarity;
import com.terraforge.rpg.registry.ModEntities;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Vortex Beater (Terraria 1.4.5.8 PC).
 * Endgame celestial firearm featuring 66% ammo conservation and firing explosive Vortex Rockets.
 * Uses ItemStack CustomData component to store shot counter without singleton mutable state.
 */
public class VortexBeaterItem extends TerrariaGunItem {

    public VortexBeaterItem(Properties properties) {
        super(properties, TerrariaRarity.RED, 1_000_000L, 50.0, 0.04, 2.5, 12, 0.66);
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

            // Track shot count safely on ItemStack component
            CustomData customData = gunStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            CompoundTag tag = customData.copyTag();
            int shotCounter = tag.getInt("ShotCount") + 1;
            tag.putInt("ShotCount", shotCounter);
            gunStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

            // Main high-tech celestial bullet
            TerraProjectileEntity bullet = new TerraProjectileEntity(ModEntities.TERRA_PROJECTILE.get(), serverLevel);
            bullet.setPos(eyePos.x, eyePos.y - 0.1, eyePos.z);
            bullet.setOwner(player);
            bullet.setDamage(getTerrariaBaseDamage());
            bullet.setDamageClass(DamageClass.RANGED);
            bullet.setKnockback(getKnockback());
            bullet.setMaxLifeTicks(80);
            bullet.setProjectileGravity(0.0);
            bullet.setDeltaMovement(look.scale(3.5));
            serverLevel.addFreshEntity(bullet);

            // Vortex Rocket every 4th shot
            if (shotCounter % 4 == 0) {
                TerraProjectileEntity rocket = new TerraProjectileEntity(ModEntities.TERRA_PROJECTILE.get(), serverLevel);
                rocket.setPos(eyePos.x, eyePos.y, eyePos.z);
                rocket.setOwner(player);
                rocket.setDamage(getTerrariaBaseDamage() * 2.0); // 100 base damage rocket
                rocket.setDamageClass(DamageClass.RANGED);
                rocket.setKnockback(5.0);
                rocket.setMaxLifeTicks(100);
                rocket.setProjectileGravity(0.0);
                rocket.setDeltaMovement(look.scale(2.2));
                serverLevel.addFreshEntity(rocket);

                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.FIREWORK_ROCKET_LARGE_BLAST, SoundSource.PLAYERS, 1.0f, 1.2f);
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, eyePos.x + look.x, eyePos.y + look.y, eyePos.z + look.z,
                        6, 0.1, 0.1, 0.1, 0.04);
            }

            // 66% ammo conservation using player's random
            if (!player.isCreative() && !ammoStack.isEmpty() && player.getRandom().nextDouble() >= 0.66) {
                ammoStack.shrink(1);
            }

            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 0.8f, 1.8f);
            serverLevel.sendParticles(ParticleTypes.GLOW, eyePos.x + look.x * 0.8, eyePos.y + look.y * 0.8, eyePos.z + look.z * 0.8,
                    3, 0.05, 0.05, 0.05, 0.02);

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
