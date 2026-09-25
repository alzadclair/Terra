package com.terraforge.rpg.boss.endgame;

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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
 * Plantera - Ancient jungle guardian boss (Terraria 1.4.5.8 canonical).
 * Features Phase 1 thorny seed barrage, Phase 2 carnivorous mouth sprint with tentacles,
 * and surface enrage mode.
 */
public class PlanteraEntity extends TerraBaseBoss {

    public static final double BASE_HEALTH = 30000.0;
    private int attackTimer = 0;

    public PlanteraEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level, "plantera", 150_000L, 350_000L, 36, 96, BossEvent.BossBarColor.GREEN);
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HEALTH)
                .add(Attributes.FLYING_SPEED, 0.40)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
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
        this.goalSelector.addGoal(1, new PlanteraCombatGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public int getTerrariaDefense() {
        if (isEnraged()) return 72; // Enraged outside underground jungle
        // Phase 1: 36 defense. Phase 2 (<= 50% HP): 10 defense.
        return getCurrentPhase().phaseNumber() >= 2 ? 10 : 36;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();

        // Enrage if above underground level (Y >= 60 in vanilla MC)
        boolean aboveUnderground = this.getY() >= 60;
        if (aboveUnderground != isEnraged()) {
            setEnraged(aboveUnderground);
            if (aboveUnderground && level() instanceof ServerLevel serverLevel) {
                serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                        Component.literal("Plantera has become enraged by leaving the underground jungle!").withStyle(net.minecraft.ChatFormatting.DARK_RED, net.minecraft.ChatFormatting.BOLD),
                        false
                );
            }
        }

        // Phase 2 transition at <= 50% HP
        if (getCurrentPhase() == BossPhase.PHASE_1 && (getHealth() / getMaxHealth()) <= 0.50) {
            setPhase(BossPhase.PHASE_2);
        }

        if (getCurrentPhase() == BossPhase.PHASE_2 && level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, getX(), getY() + 1.0, getZ(), 6, 0.5, 0.5, 0.5, 0.05);
        }
    }

    @Override
    protected void onPhaseTransition(BossPhase newPhase) {
        super.onPhaseTransition(newPhase);
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 2.0f, 1.2f);
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 1.0, getZ(), 15, 0.8, 0.8, 0.8, 0.1);
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        if (!level().isClientSide() && level() instanceof ServerLevel serverLevel) {
            WorldProgressionData progression = WorldProgressionData.get(serverLevel.getServer());
            progression.markBossDefeated("plantera");

            // Drops: Temple Key
            ItemEntity keyDrop = new ItemEntity(serverLevel, getX(), getY(), getZ(),
                    new ItemStack(ModItems.TEMPLE_KEY.get(), 1));
            serverLevel.addFreshEntity(keyDrop);

            serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                    Component.literal("Plantera has been defeated!").withStyle(net.minecraft.ChatFormatting.GREEN, net.minecraft.ChatFormatting.BOLD),
                    false
            );

            // Canonical Terraria post-Plantera Dungeon message
            serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                    Component.literal("Screams echo from the dungeon...").withStyle(net.minecraft.ChatFormatting.DARK_PURPLE, net.minecraft.ChatFormatting.ITALIC),
                    false
            );
        }
    }

    static class PlanteraCombatGoal extends Goal {
        private final PlanteraEntity plantera;
        private int timer = 0;

        public PlanteraCombatGoal(PlanteraEntity plantera) {
            this.plantera = plantera;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return plantera.getTarget() != null && plantera.getTarget().isAlive();
        }

        @Override
        public void tick() {
            LivingEntity target = plantera.getTarget();
            if (target == null) return;

            plantera.getLookControl().setLookAt(target, 30.0f, 30.0f);
            timer++;

            boolean isPhase2 = plantera.getCurrentPhase().phaseNumber() >= 2;
            boolean enraged = plantera.isEnraged();

            double speed = enraged ? 1.8 : (isPhase2 ? 1.4 : 0.9);

            // In Phase 2: aggressive chasing directly
            if (isPhase2 || enraged) {
                plantera.getMoveControl().setWantedPosition(target.getX(), target.getY() + 1.0, target.getZ(), speed);
                if (plantera.distanceToSqr(target) < 10.0) {
                    target.hurt(plantera.damageSources().mobAttack(plantera), (float) (enraged ? 100.0 : 62.0));
                    target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
                }
            } else {
                // Phase 1: stays suspended around target
                double angle = plantera.tickCount * 0.03;
                double targetX = target.getX() + Math.cos(angle) * 8.0;
                double targetY = target.getY() + 3.0 + Math.sin(angle * 2.0) * 2.0;
                double targetZ = target.getZ() + Math.sin(angle) * 8.0;
                plantera.getMoveControl().setWantedPosition(targetX, targetY, targetZ, speed);

                // Shoot thorn balls and poisonous seeds
                if (timer % 30 == 0) {
                    fireSeeds(target);
                }
            }
        }

        private void fireSeeds(LivingEntity target) {
            if (!(plantera.level() instanceof ServerLevel serverLevel)) return;

            Vec3 eyePos = plantera.getEyePosition();
            Vec3 dir = target.getEyePosition().subtract(eyePos).normalize();

            TerraProjectileEntity seed = new TerraProjectileEntity(ModEntities.TERRA_PROJECTILE.get(), serverLevel);
            seed.setPos(eyePos.x, eyePos.y, eyePos.z);
            seed.setOwner(plantera);
            seed.setDamageClass(DamageClass.RANGED);
            seed.setDamage(28.0);
            seed.setProjectileGravity(0.02);
            seed.shoot(dir.x, dir.y + 0.1, dir.z, 1.6f, 1.0f);
            serverLevel.addFreshEntity(seed);

            serverLevel.playSound(null, plantera.getX(), plantera.getY(), plantera.getZ(),
                    SoundEvents.ARROW_SHOOT, SoundSource.HOSTILE, 1.0f, 1.4f);
        }
    }
}
