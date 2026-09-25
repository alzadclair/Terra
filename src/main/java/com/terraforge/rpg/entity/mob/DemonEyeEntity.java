package com.terraforge.rpg.entity.mob;

import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.experience.ThreatRating;
import com.terraforge.rpg.registry.ModEntities;
import net.minecraft.core.BlockPos;
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
 * Terraria Demon Eye flying monster with swooping and dash attack behaviors.
 */
public class DemonEyeEntity extends TerraBaseMonster {

    public DemonEyeEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level, ThreatRating.STRONG, 75L, 200L, 12, DamageClass.MELEE);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.noPhysics = false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.FLYING_SPEED, 0.40)
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .add(Attributes.ATTACK_DAMAGE, 18.0)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        nav.setCanPassDoors(false);
        return nav;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new DemonEyeSwoopGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState state, BlockPos pos) {
        // Flying mob ignores fall damage
    }

    static class DemonEyeSwoopGoal extends Goal {
        private final DemonEyeEntity eye;
        private int chargeCooldown = 40;
        private boolean charging = false;
        private Vec3 chargeVector = Vec3.ZERO;

        DemonEyeSwoopGoal(DemonEyeEntity eye) {
            this.eye = eye;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return eye.getTarget() != null && eye.getTarget().isAlive();
        }

        @Override
        public void tick() {
            LivingEntity target = eye.getTarget();
            if (target == null) return;

            eye.getLookControl().setLookAt(target, 30.0f, 30.0f);
            chargeCooldown--;

            if (charging) {
                // Moving forward rapidly on charge vector
                eye.setDeltaMovement(chargeVector);

                if (eye.distanceToSqr(target) < 2.5) {
                    eye.doHurtTarget(target);
                    // Rebound backward
                    eye.setDeltaMovement(chargeVector.scale(-0.5).add(0, 0.3, 0));
                    charging = false;
                    chargeCooldown = 40 + eye.mobRandom.nextInt(30);
                } else if (chargeCooldown <= 0) {
                    charging = false;
                    chargeCooldown = 30 + eye.mobRandom.nextInt(20);
                }
            } else {
                // Hover circling above target
                Vec3 targetPos = target.position().add(0, 3.5, 0);
                Vec3 toTarget = targetPos.subtract(eye.position());
                double dist = toTarget.length();

                if (dist > 1.0) {
                    Vec3 hoverMotion = toTarget.normalize().scale(0.25);
                    eye.setDeltaMovement(eye.getDeltaMovement().scale(0.8).add(hoverMotion));
                }

                // Initiate charge dash when cooldown ready
                if (chargeCooldown <= 0 && dist < 16.0) {
                    charging = true;
                    chargeVector = target.getEyePosition().subtract(eye.position()).normalize().scale(0.65);
                    chargeCooldown = 25; // 25 ticks max charge duration
                    if (eye.level() != null) {
                        eye.level().playSound(null, eye.getX(), eye.getY(), eye.getZ(),
                                SoundEvents.PHANTOM_BITE, SoundSource.HOSTILE, 1.0f, 0.8f);
                    }
                }
            }
        }
    }
}
