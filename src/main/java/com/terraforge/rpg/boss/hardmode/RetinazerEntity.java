package com.terraforge.rpg.boss.hardmode;

import com.terraforge.rpg.boss.BossPhase;
import com.terraforge.rpg.boss.TerraBaseBoss;
import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.entity.projectile.TerraProjectileEntity;
import com.terraforge.rpg.registry.ModEntities;
import com.terraforge.rpg.registry.ModItems;
import com.terraforge.rpg.world.progression.WorldProgressionData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Retinazer - The red-eyed mechanical boss half of The Twins (Terraria 1.4.5.8 canonical).
 * Fires precision lasers in Phase 1, transforms into a high-frequency laser cannon in Phase 2.
 */
public class RetinazerEntity extends TerraBaseBoss {

    public static final double BASE_HEALTH = 24000.0;

    public RetinazerEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level, "retinazer", 100_000L, 200_000L, 10, 96, BossEvent.BossBarColor.RED);
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HEALTH)
                .add(Attributes.FLYING_SPEED, 0.45)
                .add(Attributes.MOVEMENT_SPEED, 0.40)
                .add(Attributes.ATTACK_DAMAGE, 50.0)
                .add(Attributes.FOLLOW_RANGE, 128.0);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanFloat(true);
        return nav;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new RetinazerCombatGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public int getTerrariaDefense() {
        // Phase 1: 10 defense. Phase 2 (<= 40% HP): 20 defense.
        return getCurrentPhase().phaseNumber() >= 2 ? 20 : 10;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();

        // Despawn at daytime
        long dayTime = this.level().getDayTime() % 24000;
        if (dayTime < 13000 || dayTime > 23500) {
            this.setDeltaMovement(0, 1.2, 0);
            if (this.getY() > 320) {
                this.discard();
            }
            return;
        }

        // Phase transition at 40% HP (Terraria canonical)
        if (getCurrentPhase() == BossPhase.PHASE_1 && (getHealth() / getMaxHealth()) <= 0.40) {
            setPhase(BossPhase.PHASE_2);
        }

        if (getCurrentPhase() == BossPhase.PHASE_2 && level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CRIT, getX(), getY() + 0.5, getZ(), 3, 0.3, 0.3, 0.3, 0.05);
        }
    }

    @Override
    protected void onPhaseTransition(BossPhase newPhase) {
        super.onPhaseTransition(newPhase);
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.HOSTILE, 2.0f, 1.5f);
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 1.0, getZ(), 10, 0.5, 0.5, 0.5, 0.1);
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        if (!level().isClientSide() && level() instanceof ServerLevel serverLevel) {
            WorldProgressionData progression = WorldProgressionData.get(serverLevel.getServer());
            progression.markBossDefeated("the_twins");

            // Drops: Souls of Sight + Hallowed Bars
            ItemEntity soulDrop = new ItemEntity(serverLevel, getX(), getY(), getZ(),
                    new ItemStack(ModItems.SOUL_OF_SIGHT.get(), 25 + serverLevel.random.nextInt(15)));
            serverLevel.addFreshEntity(soulDrop);

            ItemEntity barDrop = new ItemEntity(serverLevel, getX(), getY(), getZ(),
                    new ItemStack(ModItems.HALLOWED_BAR.get(), 15 + serverLevel.random.nextInt(15)));
            serverLevel.addFreshEntity(barDrop);

            serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                    Component.literal("The Twins have been defeated!").withStyle(net.minecraft.ChatFormatting.GREEN, net.minecraft.ChatFormatting.BOLD),
                    false
            );
        }
    }

    static class RetinazerCombatGoal extends Goal {
        private final RetinazerEntity boss;
        private int attackTimer = 0;

        public RetinazerCombatGoal(RetinazerEntity boss) {
            this.boss = boss;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return boss.getTarget() != null && boss.getTarget().isAlive();
        }

        @Override
        public void tick() {
            LivingEntity target = boss.getTarget();
            if (target == null) return;

            boss.getLookControl().setLookAt(target, 30.0f, 30.0f);
            attackTimer++;

            boolean isPhase2 = boss.getCurrentPhase().phaseNumber() >= 2;
            int shootInterval = isPhase2 ? 15 : 35; // Rapid fire in Phase 2

            // Hover positioning: stays diagonally above target
            double targetX = target.getX() + Math.cos(boss.tickCount * 0.05) * 12.0;
            double targetY = target.getY() + 8.0;
            double targetZ = target.getZ() + Math.sin(boss.tickCount * 0.05) * 12.0;
            boss.getMoveControl().setWantedPosition(targetX, targetY, targetZ, isPhase2 ? 1.4 : 1.1);

            if (attackTimer >= shootInterval) {
                attackTimer = 0;
                fireLaser(target, isPhase2);
            }
        }

        private void fireLaser(LivingEntity target, boolean isPhase2) {
            if (!(boss.level() instanceof ServerLevel serverLevel)) return;

            Vec3 eyePos = boss.getEyePosition();
            Vec3 dir = target.getEyePosition().subtract(eyePos).normalize();

            TerraProjectileEntity laser = new TerraProjectileEntity(ModEntities.TERRA_PROJECTILE.get(), serverLevel);
            laser.setPos(eyePos.x, eyePos.y, eyePos.z);
            laser.setOwner(boss);
            laser.setDamageClass(DamageClass.MAGIC);
            laser.setDamage(isPhase2 ? 30.0 : 20.0);
            laser.setProjectileGravity(0.0);
            laser.shoot(dir.x, dir.y, dir.z, isPhase2 ? 2.5f : 1.8f, 0.5f);
            serverLevel.addFreshEntity(laser);

            serverLevel.playSound(null, boss.getX(), boss.getY(), boss.getZ(),
                    SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.0f, 1.8f);
        }
    }
}
