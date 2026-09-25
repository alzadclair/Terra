package com.terraforge.rpg.entity.mob;

import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.experience.ThreatRating;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Terraria Slime monster with periodic hopping AI and gel drops.
 */
public class TerraSlimeEntity extends TerraBaseMonster {

    public enum SlimeVariant {
        GREEN(14.0, 0, ThreatRating.WEAK_MOB, 25L, 50L, 6.0),
        BLUE(25.0, 2, ThreatRating.COMMON, 35L, 80L, 7.0),
        PURPLE(40.0, 6, ThreatRating.STRONG, 50L, 120L, 12.0);

        final double health;
        final int defense;
        final ThreatRating rating;
        final long minCoins;
        final long maxCoins;
        final double attackDamage;

        SlimeVariant(double health, int defense, ThreatRating rating, long minCoins, long maxCoins, double attackDamage) {
            this.health = health;
            this.defense = defense;
            this.rating = rating;
            this.minCoins = minCoins;
            this.maxCoins = maxCoins;
            this.attackDamage = attackDamage;
        }
    }

    private final SlimeVariant variant;

    public TerraSlimeEntity(EntityType<? extends Monster> entityType, Level level, SlimeVariant variant) {
        super(entityType, level, variant.rating, variant.minCoins, variant.maxCoins, variant.defense, DamageClass.MELEE);
        this.variant = variant;
    }

    public static AttributeSupplier.Builder createAttributes(SlimeVariant variant) {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, variant.health)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, variant.attackDamage)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new SlimeHopGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        // Drop Gel (1-2 Slime Balls)
        if (!this.level().isClientSide()) {
            int gelCount = 1 + mobRandom.nextInt(2);
            ItemEntity gel = new ItemEntity(this.level(), this.getX(), this.getY() + 0.5, this.getZ(),
                    new ItemStack(Items.SLIME_BALL, gelCount));
            this.level().addFreshEntity(gel);
        }
    }

    public SlimeVariant getVariant() {
        return variant;
    }

    static class SlimeHopGoal extends Goal {
        private final TerraSlimeEntity slime;
        private int hopCooldown = 20;

        SlimeHopGoal(TerraSlimeEntity slime) {
            this.slime = slime;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return slime.getTarget() != null && slime.getTarget().isAlive();
        }

        @Override
        public void tick() {
            LivingEntity target = slime.getTarget();
            if (target == null) return;

            slime.getLookControl().setLookAt(target, 30.0f, 30.0f);
            hopCooldown--;

            if (slime.onGround() && hopCooldown <= 0) {
                // Hop towards target
                Vec3 toTarget = target.position().subtract(slime.position()).normalize();
                slime.setDeltaMovement(toTarget.x * 0.35, 0.42, toTarget.z * 0.35);

                if (slime.level() != null) {
                    slime.level().playSound(null, slime.getX(), slime.getY(), slime.getZ(),
                            SoundEvents.SLIME_JUMP, net.minecraft.sounds.SoundSource.HOSTILE, 1.0f, 1.2f);
                }

                hopCooldown = 25 + slime.mobRandom.nextInt(15);
            }

            // Deal contact damage
            if (slime.distanceToSqr(target) < 1.8 && slime.hasLineOfSight(target)) {
                slime.doHurtTarget(target);
            }
        }
    }
}
