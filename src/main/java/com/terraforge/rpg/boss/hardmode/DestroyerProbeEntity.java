package com.terraforge.rpg.boss.hardmode;

import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.entity.mob.TerraBaseMonster;
import com.terraforge.rpg.entity.projectile.TerraProjectileEntity;
import com.terraforge.rpg.experience.ThreatRating;
import com.terraforge.rpg.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Autonomous flying probe detached from The Destroyer segments (Terraria 1.4.5.8 canonical).
 * Has 200 HP, flies around and fires red death lasers at players.
 */
public class DestroyerProbeEntity extends TerraBaseMonster {

    public DestroyerProbeEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level, ThreatRating.STRONG, 500L, 2_000L, 20, DamageClass.RANGED);
        this.moveControl = new FlyingMoveControl(this, 15, true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 200.0)
                .add(Attributes.FLYING_SPEED, 0.40)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, 25.0)
                .add(Attributes.FOLLOW_RANGE, 64.0);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanFloat(true);
        return nav;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new ProbeAttackGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    static class ProbeAttackGoal extends Goal {
        private final DestroyerProbeEntity probe;
        private int shootTimer = 0;

        public ProbeAttackGoal(DestroyerProbeEntity probe) {
            this.probe = probe;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return probe.getTarget() != null && probe.getTarget().isAlive();
        }

        @Override
        public void tick() {
            LivingEntity target = probe.getTarget();
            if (target == null) return;

            probe.getLookControl().setLookAt(target, 30.0f, 30.0f);
            shootTimer++;

            // Circle target
            double angle = probe.tickCount * 0.08;
            double targetX = target.getX() + Math.cos(angle) * 8.0;
            double targetY = target.getY() + 3.0 + Math.sin(angle * 0.5) * 2.0;
            double targetZ = target.getZ() + Math.sin(angle) * 8.0;
            probe.getMoveControl().setWantedPosition(targetX, targetY, targetZ, 1.2);

            if (shootTimer >= 40) {
                shootTimer = 0;
                fireLaser(target);
            }
        }

        private void fireLaser(LivingEntity target) {
            if (!(probe.level() instanceof ServerLevel serverLevel)) return;

            Vec3 eyePos = probe.getEyePosition();
            Vec3 dir = target.getEyePosition().subtract(eyePos).normalize();

            TerraProjectileEntity laser = new TerraProjectileEntity(ModEntities.TERRA_PROJECTILE.get(), serverLevel);
            laser.setPos(eyePos.x, eyePos.y, eyePos.z);
            laser.setOwner(probe);
            laser.setDamageClass(DamageClass.RANGED);
            laser.setDamage(18.0);
            laser.setProjectileGravity(0.0);
            laser.shoot(dir.x, dir.y, dir.z, 1.8f, 0.5f);
            serverLevel.addFreshEntity(laser);

            serverLevel.playSound(null, probe.getX(), probe.getY(), probe.getZ(),
                    SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 0.8f, 2.0f);
        }
    }
}
