package com.terraforge.rpg.boss.prehardmode;

import com.terraforge.rpg.boss.TerraBaseBoss;
import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.entity.mob.TerraSlimeEntity;
import com.terraforge.rpg.registry.ModEntities;
import com.terraforge.rpg.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * King Slime pre-hardmode boss.
 * Canonical Terraria 1.4.5.8: 2000 HP, 10 Defense, 40 Damage.
 * Teleports directly onto target when distant or obstructed.
 * Spawns subordinate slimes as it takes damage.
 */
public class KingSlimeEntity extends TerraBaseBoss {

    public static final double BASE_HEALTH = 2000.0;
    public static final int BASE_DEFENSE = 10;
    public static final double BASE_ATTACK_DAMAGE = 40.0;

    private int teleportCooldown = 200; // 10 seconds between teleports
    private int jumpCooldown = 30;

    public KingSlimeEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level, "king_slime", 10_000L, 25_000L, BASE_DEFENSE, 100, BossEvent.BossBarColor.BLUE);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, BASE_ATTACK_DAMAGE)
                .add(Attributes.FOLLOW_RANGE, 100.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            Player nearest = this.level().getNearestPlayer(this, 100.0);
            if (nearest != null) {
                this.setTarget(nearest);
            }
            return;
        }

        this.getLookControl().setLookAt(target, 30.0f, 30.0f);

        // Giant hopping mechanics
        jumpCooldown--;
        if (this.onGround() && jumpCooldown <= 0) {
            jumpCooldown = 40;
            Vec3 jumpDir = target.position().subtract(this.position()).normalize();
            this.setDeltaMovement(jumpDir.x * 0.5, 0.7, jumpDir.z * 0.5);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.SLIME_JUMP, SoundSource.HOSTILE, 2.0f, 0.7f);
        }

        // Teleportation when obstructed or distant
        teleportCooldown--;
        double distSq = this.distanceToSqr(target);
        if (teleportCooldown <= 0 && distSq > 144.0) { // > 12 blocks
            teleportCooldown = 240;
            teleportOntoTarget(target);
        }
    }

    private void teleportOntoTarget(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        // Particle effect at old location
        serverLevel.sendParticles(ParticleTypes.ITEM_SLIME, this.getX(), this.getY() + 1.0, this.getZ(),
                50, 1.0, 1.0, 1.0, 0.2);

        // Teleport 5 blocks above target
        this.teleportTo(target.getX(), target.getY() + 5.0, target.getZ());
        this.setDeltaMovement(0, -0.8, 0);

        serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 2.5f, 0.5f);
        serverLevel.sendParticles(ParticleTypes.ITEM_SLIME, target.getX(), target.getY() + 5.0, target.getZ(),
                60, 1.5, 1.5, 1.5, 0.2);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean damaged = super.hurt(source, amount);
        if (damaged && !this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            // Spawn subordinate slime occasionally when damaged
            if (this.random.nextFloat() < 0.35f) {
                TerraSlimeEntity minion = new TerraSlimeEntity(ModEntities.BLUE_SLIME.get(), serverLevel, TerraSlimeEntity.SlimeVariant.BLUE);
                minion.setPos(this.getX() + (this.random.nextDouble() - 0.5) * 2.0, this.getY(), this.getZ() + (this.random.nextDouble() - 0.5) * 2.0);
                minion.setTarget(this.getTarget());
                serverLevel.addFreshEntity(minion);
            }
        }
        return damaged;
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        if (!this.level().isClientSide()) {
            // Drop 1 Gold coin
            this.spawnAtLocation(new ItemStack(ModItems.GOLD_COIN.get(), 1));
        }
    }
}
