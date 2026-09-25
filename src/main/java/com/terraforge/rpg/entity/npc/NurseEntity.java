package com.terraforge.rpg.entity.npc;

import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.economy.CoinHelper;
import com.terraforge.rpg.entity.projectile.TerraProjectileEntity;
import com.terraforge.rpg.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * The Nurse - medical practitioner Town NPC.
 * Moves in when player maximum health exceeds base and Merchant is present.
 * Heals the player to full health in exchange for Terraria coins.
 */
public class NurseEntity extends TerraBaseTownNPC {

    public NurseEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level, "nurse");
    }

    @Override
    protected int getAttackInterval() {
        return 30; // Throws syringes every 1.5s
    }

    @Override
    protected void performDefenseAttack(LivingEntity target, ServerLevel level) {
        Vec3 eyePos = this.getEyePosition();
        Vec3 dir = target.getEyePosition().subtract(eyePos).normalize();

        TerraProjectileEntity syringe = new TerraProjectileEntity(ModEntities.TERRA_PROJECTILE.get(), level);
        syringe.setPos(eyePos.x, eyePos.y, eyePos.z);
        syringe.setOwner(this);
        syringe.setDamageClass(DamageClass.RANGED);
        syringe.setDamage(10.0);
        syringe.setProjectileGravity(0.015);
        syringe.shoot(dir.x, dir.y + 0.05, dir.z, 1.3f, 1.0f);

        level.addFreshEntity(syringe);
        level.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.NEUTRAL, 0.8f, 1.6f);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            float missingHp = player.getMaxHealth() - player.getHealth();

            if (missingHp <= 0.05f) {
                player.sendSystemMessage(
                        Component.literal("[Nurse] You are already in peak physical condition!")
                                .withStyle(ChatFormatting.GREEN)
                );
                return InteractionResult.SUCCESS;
            }

            // Treatment cost: 2 copper coins per missing HP (minimum 10 copper)
            long cost = Math.max(10L, Math.round(missingHp * 2.0));

            if (!CoinHelper.hasEnoughCoins(player, cost)) {
                player.sendSystemMessage(
                        Component.literal("[Nurse] You need " + CoinHelper.formatCoins(cost) + " for treatment!")
                                .withStyle(ChatFormatting.RED)
                );
                return InteractionResult.CONSUME;
            }

            // Deduct coins and heal
            CoinHelper.deductCoins(player, cost);
            player.setHealth(player.getMaxHealth());

            serverLevel.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.0, player.getZ(),
                    10, 0.5, 0.5, 0.5, 0.1);
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.4f);

            player.sendSystemMessage(
                    Component.literal("[Nurse] All patched up! That will be " + CoinHelper.formatCoins(cost) + ".")
                            .withStyle(ChatFormatting.GREEN)
            );
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }
}
