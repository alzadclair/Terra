package com.terraforge.rpg.entity.mob;

import com.terraforge.rpg.combat.DamageClass;
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
 * Minion spawned by the Eye of Cthulhu boss.
 */
public class ServantOfCthulhuEntity extends TerraBaseMonster {

    public ServantOfCthulhuEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level, ThreatRating.WEAK_MOB, 5L, 15L, 0, DamageClass.MELEE);
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 8.0)
                .add(Attributes.FLYING_SPEED, 0.45)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, 12.0)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanFloat(true);
        return nav;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new ServantFlyGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState state, BlockPos pos) {
    }

    static class ServantFlyGoal extends Goal {
        private final ServantOfCthulhuEntity servant;

        ServantFlyGoal(ServantOfCthulhuEntity servant) {
            this.servant = servant;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return servant.getTarget() != null && servant.getTarget().isAlive();
        }

        @Override
        public void tick() {
            LivingEntity target = servant.getTarget();
            if (target == null) return;

            servant.getLookControl().setLookAt(target, 30.0f, 30.0f);
            Vec3 toTarget = target.getEyePosition().subtract(servant.position()).normalize();
            servant.setDeltaMovement(toTarget.scale(0.35));

            if (servant.distanceToSqr(target) < 1.8) {
                servant.doHurtTarget(target);
            }
        }
    }
}
