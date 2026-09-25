package com.terraforge.rpg.item.weapon;

import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.entity.projectile.TerraProjectileEntity;
import com.terraforge.rpg.item.rarity.TerrariaRarity;
import com.terraforge.rpg.registry.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Tsunami - Endgame bow dropped by Duke Fishron (Terraria 1.4.5.8 canonical).
 * Fires a concentrated burst of 5 arrows simultaneously for the cost of a single arrow!
 */
public class TsunamiBowItem extends TerrariaBowItem {

    public TsunamiBowItem(Properties properties) {
        super(properties, TerrariaRarity.CYAN, 500_000L, 60.0, 0.04, 1.5, 24);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft) {
        if (entityLiving instanceof Player player && !level.isClientSide() && level instanceof ServerLevel serverLevel) {
            Vec3 eyePos = player.getEyePosition();
            Vec3 look = player.getLookAngle();

            // Salvo of 5 arrows
            for (int i = -2; i <= 2; i++) {
                Vec3 offsetLook = look.add(look.y * 0.05 * i, 0.02 * i, 0).normalize();

                TerraProjectileEntity arrow = new TerraProjectileEntity(ModEntities.TERRA_PROJECTILE.get(), serverLevel);
                arrow.setPos(eyePos.x, eyePos.y, eyePos.z);
                arrow.setOwner(player);
                arrow.setDamageClass(DamageClass.RANGED);
                arrow.setDamage(60.0);
                arrow.setProjectileGravity(0.015);
                arrow.shoot(offsetLook.x, offsetLook.y, offsetLook.z, 2.5f, 0.5f);
                serverLevel.addFreshEntity(arrow);
            }

            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.2f, 1.5f);

            player.getItemInHand(player.getUsedItemHand()).hurtAndBreak(1, player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
        }
    }
}
