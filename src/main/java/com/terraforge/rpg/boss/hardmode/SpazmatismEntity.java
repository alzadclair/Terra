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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/**
 * Spazmatism - The green-eyed mechanical boss half of The Twins (Terraria 1.4.5.8 canonical).
 * Fires Cursed Fireballs in Phase 1, transforms into an aggressive Cursed Flamethrower with fast charges in Phase 2.
 */
public class SpazmatismEntity extends TerraBaseBoss {

    public static final double BASE_HEALTH = 23000.0;

    public SpazmatismEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level, "spazmatism", 100_000L, 200_000L, 10, 96, BossEvent.BossBarColor.GREEN);
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HEALTH)
                .add(Attributes.FLYING_SPEED, 0.48)
                .add(Attributes.MOVEMENT_SPEED, 0.42)
                .add(Attributes.ATTACK_DAMAGE, 55.0)
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
        this.goalSelector.addGoal(1, new SpazmatismCombatGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public int getTerrariaDefense() {
        // Phase 1: 10 defense. Phase 2 (<= 40% HP): 28 defense.
        return getCurrentPhase().phaseNumber() >= 2 ? 28 : 10;
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

        // Phase transition at 40% HP
        if (getCurrentPhase() == BossPhase.PHASE_1 && (getHealth() / getMaxHealth()) <= 0.40) {
            setPhase(BossPhase.PHASE_2);
        }

        if (getCurrentPhase() == BossPhase.PHASE_2 && level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + 0.5, getZ(), 4, 0.3, 0.3, 0.3, 0.05);
        }
    }

    @Override
    protected void onPhaseTransition(BossPhase newPhase) {
        super.onPhaseTransition(newPhase);
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 2.0f, 1.3f);
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 1.0, getZ(), 12, 0.5, 0.5, 0.5, 0.1);
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

    static class SpazmatismCombatGoal extends Goal {
        private final SpazmatismEntity boss;
        private int timer = 0;
        private int chargeCount = 0;
        private boolean charging = false;
        private Vec3 chargeDir = Vec3.ZERO;

        public SpazmatismCombatGoal(SpazmatismEntity boss) {
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
            timer++;

            boolean isPhase2 = boss.getCurrentPhase().phaseNumber() >= 2;

            if (charging) {
                boss.setDeltaMovement(chargeDir);
                if (timer % 5 == 0 && boss.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, boss.getX(), boss.getY() + 0.5, boss.getZ(), 6, 0.4, 0.4, 0.4, 0.1);
                }

                if (boss.distanceToSqr(target) < 9.0) {
                    target.hurt(boss.damageSources().mobAttack(boss), isPhase2 ? 70.0f : 50.0f);
                    target.setRemainingFireTicks(100);
                }

                if (timer >= 20) {
                    charging = false;
                    timer = 0;
                    chargeCount++;
                }
                return;
            }

            if (!isPhase2) {
                // Phase 1: Shoot cursed fireballs every 40 ticks, then 2 charges
                if (timer % 40 == 0 && chargeCount < 4) {
                    fireCursedBall(target);
                } else if (timer >= 120) {
                    startCharge(target, 1.2);
                }
            } else {
                // Phase 2: Cursed Flamethrower stream for 80 ticks, then 5 fast charges
                if (chargeCount < 5) {
                    // Flamethrower mode
                    streamCursedFlame(target);
                    boss.getMoveControl().setWantedPosition(target.getX(), target.getY() + 4.0, target.getZ(), 1.0);
                    if (timer >= 80) {
                        timer = 0;
                        chargeCount = 5; // initiate charges
                    }
                } else {
                    // Fast dash charges
                    startCharge(target, 1.6);
                    if (chargeCount >= 10) {
                        chargeCount = 0; // return to flame mode
                        timer = 0;
                    }
                }
            }
        }

        private void startCharge(LivingEntity target, double speed) {
            charging = true;
            timer = 0;
            chargeDir = target.getEyePosition().subtract(boss.getEyePosition()).normalize().scale(speed);
            boss.setDeltaMovement(chargeDir);
            if (boss.level() instanceof ServerLevel serverLevel) {
                serverLevel.playSound(null, boss.getX(), boss.getY(), boss.getZ(),
                        SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 1.5f, 0.9f);
            }
        }

        private void fireCursedBall(LivingEntity target) {
            if (!(boss.level() instanceof ServerLevel serverLevel)) return;

            Vec3 eyePos = boss.getEyePosition();
            Vec3 dir = target.getEyePosition().subtract(eyePos).normalize();

            TerraProjectileEntity fireBall = new TerraProjectileEntity(ModEntities.TERRA_PROJECTILE.get(), serverLevel);
            fireBall.setPos(eyePos.x, eyePos.y, eyePos.z);
            fireBall.setOwner(boss);
            fireBall.setDamageClass(DamageClass.MAGIC);
            fireBall.setDamage(32.0);
            fireBall.setProjectileGravity(0.01);
            fireBall.shoot(dir.x, dir.y, dir.z, 1.6f, 0.5f);
            serverLevel.addFreshEntity(fireBall);

            serverLevel.playSound(null, boss.getX(), boss.getY(), boss.getZ(),
                    SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 1.2f, 1.0f);
        }

        private void streamCursedFlame(LivingEntity target) {
            if (!(boss.level() instanceof ServerLevel serverLevel)) return;

            Vec3 eyePos = boss.getEyePosition();
            Vec3 dir = target.getEyePosition().subtract(eyePos).normalize();

            // Emit particles towards target
            for (int i = 1; i <= 6; i++) {
                Vec3 p = eyePos.add(dir.scale(i * 1.5));
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y, p.z, 2, 0.2, 0.2, 0.2, 0.02);
            }

            // Hurt targets in flame cone
            AABB flameCone = new AABB(eyePos, eyePos.add(dir.scale(10.0))).inflate(2.0);
            List<Player> nearby = serverLevel.getEntitiesOfClass(Player.class, flameCone);
            for (Player player : nearby) {
                player.hurt(boss.damageSources().mobAttack(boss), 25.0f);
                player.setRemainingFireTicks(140);
            }
        }
    }
}
