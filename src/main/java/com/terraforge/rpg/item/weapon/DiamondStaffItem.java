package com.terraforge.rpg.item.weapon;

import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.entity.projectile.TerraProjectileEntity;
import com.terraforge.rpg.item.rarity.TerrariaRarity;
import com.terraforge.rpg.mana.ManaService;
import com.terraforge.rpg.player.data.PlayerRPGData;
import com.terraforge.rpg.registry.ModAttachments;
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
 * Diamond Staff (Terraria 1.4.5.8 PC).
 * Magic staff consuming 8 mana and casting a luminous piercing diamond bolt.
 */
public class DiamondStaffItem extends TerrariaMagicStaffItem {

    public DiamondStaffItem(Properties properties) {
        super(properties, TerrariaRarity.WHITE, 30_000L, 23.0, 0.04, 5.5, 26, 8.0);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        PlayerRPGData data = player.getData(ModAttachments.PLAYER_RPG_DATA);
        if (!player.isCreative() && data.getCurrentMana() < getManaCost()) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            Vec3 eyePos = player.getEyePosition();
            Vec3 look = player.getLookAngle();

            // Diamond Bolt Projectile
            TerraProjectileEntity bolt = new TerraProjectileEntity(ModEntities.TERRA_PROJECTILE.get(), serverLevel);
            bolt.setPos(eyePos.x, eyePos.y - 0.1, eyePos.z);
            bolt.setOwner(player);
            bolt.setDamage(getTerrariaBaseDamage());
            bolt.setDamageClass(DamageClass.MAGIC);
            bolt.setKnockback(getKnockback());
            bolt.setMaxPierces(1);
            bolt.setProjectileGravity(0.0);
            bolt.setMaxLifeTicks(80);
            bolt.setDeltaMovement(look.scale(2.2));
            serverLevel.addFreshEntity(bolt);

            // Deduct mana
            if (!player.isCreative()) {
                ManaService.consumeMana(data, getManaCost());
            }

            // Audio & diamond shimmer particles
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 1.0f, 1.6f);
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, eyePos.x + look.x * 0.8, eyePos.y + look.y * 0.8, eyePos.z + look.z * 0.8,
                    4, 0.05, 0.05, 0.05, 0.02);

            player.getCooldowns().addCooldown(this, getUseTime());
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
