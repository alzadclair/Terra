package com.terraforge.rpg.boss.prehardmode;

import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.entity.mob.TerraBaseMonster;
import com.terraforge.rpg.experience.ThreatRating;
import net.minecraft.core.BlockPos;
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
 * The Hungry minion tethered to the Wall of Flesh.
 * Canonical Terraria 1.4.5.8: 240 HP, 10 Defense, 30 Attack Damage.
 */
public class TheHungryEntity extends TerraBaseMonster {

    public static final double BASE_HEALTH = 240.0;
    public static final int DEFENSE = 10;
    public static final double ATTACK_DAMAGE = 30.0;

    private WallOfFleshEntity parentWall;

    public TheHungryEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level, ThreatRating.ELITE, 50L, 150L, DEFENSE, DamageClass.MELEE);
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    public void setParentWall(WallOfFleshEntity parent) {
        this.parentWall = parent;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HEALTH)
                .add(Attributes.FLYING_SPEED, 0.40)
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanFloat(true);
        return nav;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new HungryAttackGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();
        if (parentWall != null) {
            if (!parentWall.isAlive()) {
                this.discard();
                return;
            }
            // Tether: keep within 16 blocks of parent Wall
            double distSq = this.distanceToSqr(parentWall);
            if (distSq > 256.0) {
                Vec3 pull = parentWall.position().subtract(this.position()).normalize().scale(0.3);
                this.setDeltaMovement(this.getDeltaMovement().add(pull));
            }
        }
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState state, BlockPos pos) {
    }

    static class HungryAttackGoal extends Goal {
        private final TheHungryEntity hungry;

        HungryAttackGoal(TheHungryEntity hungry) {
            this.hungry = hungry;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return hungry.getTarget() != null && hungry.getTarget().isAlive();
        }

        @Override
        public void tick() {
            LivingEntity target = hungry.getTarget();
            if (target == null) return;

            hungry.getLookControl().setLookAt(target, 30.0f, 30.0f);
            Vec3 dir = target.getEyePosition().subtract(hungry.position()).normalize();
            hungry.setDeltaMovement(dir.scale(0.32));

            if (hungry.distanceToSqr(target) < 2.5) {
                hungry.doHurtTarget(target);
            }
        }
    }
}
