package com.terraforge.rpg.entity.mob.goblin;

import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.entity.mob.TerraBaseMonster;
import com.terraforge.rpg.entity.projectile.TerraProjectileEntity;
import com.terraforge.rpg.experience.ThreatRating;
import com.terraforge.rpg.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Goblin Sorcerer - spellcaster of the Goblin Army.
 * Canonical stats: 40 HP, 2 Defense, 20 Magic Damage.
 * Teleports and launches Chaos Ball projectiles.
 */
public class GoblinSorcererEntity extends TerraBaseMonster {

    public static final double BASE_HEALTH = 40.0;
    public static final int BASE_DEFENSE = 2;
    public static final double BASE_DAMAGE = 20.0;

    private int attackCooldown = 50;
    private int teleportCooldown = 120;

    public GoblinSorcererEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level, ThreatRating.ELITE, 60L, 200L, BASE_DEFENSE, DamageClass.MAGIC);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, 0.20)
                .add(Attributes.ATTACK_DAMAGE, BASE_DAMAGE)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new SorcererCastGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            teleportCooldown--;
            if (teleportCooldown <= 0) {
                teleportCooldown = 140;
                teleportRelocate(target);
            }
        }
    }

    private void teleportRelocate(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        double angle = this.random.nextDouble() * Math.PI * 2;
        double dist = 8.0 + this.random.nextDouble() * 8.0;
        double targetX = target.getX() + Math.cos(angle) * dist;
        double targetZ = target.getZ() + Math.sin(angle) * dist;
        double targetY = target.getY();

        serverLevel.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 1.0, this.getZ(),
                30, 0.5, 0.5, 0.5, 0.1);

        this.teleportTo(targetX, targetY, targetZ);

        serverLevel.playSound(null, targetX, targetY, targetZ,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0f, 1.2f);
        serverLevel.sendParticles(ParticleTypes.PORTAL, targetX, targetY + 1.0, targetZ,
                30, 0.5, 0.5, 0.5, 0.1);
    }

    static class SorcererCastGoal extends Goal {
        private final GoblinSorcererEntity sorcerer;

        SorcererCastGoal(GoblinSorcererEntity sorcerer) {
            this.sorcerer = sorcerer;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return sorcerer.getTarget() != null && sorcerer.getTarget().isAlive();
        }

        @Override
        public void tick() {
            LivingEntity target = sorcerer.getTarget();
            if (target == null) return;

            sorcerer.getLookControl().setLookAt(target, 30.0f, 30.0f);

            sorcerer.attackCooldown--;
            if (sorcerer.attackCooldown <= 0) {
                sorcerer.attackCooldown = 50;
                castChaosBall(target);
            }
        }

        private void castChaosBall(LivingEntity target) {
            if (!(sorcerer.level() instanceof ServerLevel serverLevel)) return;

            Vec3 eyePos = sorcerer.getEyePosition();
            Vec3 dir = target.getEyePosition().subtract(eyePos).normalize();

            TerraProjectileEntity ball = new TerraProjectileEntity(ModEntities.TERRA_PROJECTILE.get(), serverLevel);
            ball.setPos(eyePos.x, eyePos.y, eyePos.z);
            ball.setOwner(sorcerer);
            ball.setDamageClass(DamageClass.MAGIC);
            ball.setDamage(BASE_DAMAGE);
            ball.setProjectileGravity(0.01);
            ball.shoot(dir.x, dir.y, dir.z, 0.9f, 1.0f);

            serverLevel.addFreshEntity(ball);
            serverLevel.playSound(null, sorcerer.getX(), sorcerer.getY(), sorcerer.getZ(),
                    SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 1.0f, 0.8f);
        }
    }
}
