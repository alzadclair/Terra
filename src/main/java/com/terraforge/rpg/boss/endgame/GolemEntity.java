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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Golem - Ancient animated stone idol of the Lihzahrd Temple (Terraria 1.4.5.8 canonical).
 * Features high physical defense, fireballs, stone fist punches, detached laser head Phase 2,
 * and beetle husk drops.
 */
public class GolemEntity extends TerraBaseBoss {

    public static final double BASE_HEALTH = 16000.0;
    private int attackCycleTimer = 0;

    public GolemEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level, "golem", 150_000L, 350_000L, 24, 80, BossEvent.BossBarColor.YELLOW);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, 70.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 96.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new GolemCombatGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public int getTerrariaDefense() {
        // Phase 1: 24 defense. Phase 2 (detached head): 32 defense for stone body
        return getCurrentPhase().phaseNumber() >= 2 ? 32 : 24;
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();

        // Phase 2 transition at <= 50% HP (detached head)
        if (getCurrentPhase() == BossPhase.PHASE_1 && (getHealth() / getMaxHealth()) <= 0.50) {
            setPhase(BossPhase.PHASE_2);
        }

        attackCycleTimer++;
        if (attackCycleTimer % 35 == 0 && level() instanceof ServerLevel serverLevel) {
            LivingEntity target = getTarget();
            if (target != null) {
                if (getCurrentPhase() == BossPhase.PHASE_2) {
                    fireEyeLasers(serverLevel, target);
                } else {
                    fireFireball(serverLevel, target);
                }
            }
        }
    }

    private void fireFireball(ServerLevel serverLevel, LivingEntity target) {
        Vec3 eyePos = getEyePosition();
        Vec3 dir = target.getEyePosition().subtract(eyePos).normalize();

        TerraProjectileEntity fireball = new TerraProjectileEntity(ModEntities.TERRA_PROJECTILE.get(), serverLevel);
        fireball.setPos(eyePos.x, eyePos.y, eyePos.z);
        fireball.setOwner(this);
        fireball.setDamageClass(DamageClass.MAGIC);
        fireball.setDamage(36.0);
        fireball.setProjectileGravity(0.01);
        fireball.shoot(dir.x, dir.y, dir.z, 1.5f, 0.5f);
        serverLevel.addFreshEntity(fireball);

        serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 1.0f, 0.8f);
    }

    private void fireEyeLasers(ServerLevel serverLevel, LivingEntity target) {
        Vec3 eyePos = getEyePosition().add(0, 1.5, 0); // Head hovering above body
        Vec3 dir = target.getEyePosition().subtract(eyePos).normalize();

        TerraProjectileEntity laser = new TerraProjectileEntity(ModEntities.TERRA_PROJECTILE.get(), serverLevel);
        laser.setPos(eyePos.x, eyePos.y, eyePos.z);
        laser.setOwner(this);
        laser.setDamageClass(DamageClass.MAGIC);
        laser.setDamage(45.0);
        laser.setProjectileGravity(0.0);
        laser.shoot(dir.x, dir.y, dir.z, 2.2f, 0.3f);
        serverLevel.addFreshEntity(laser);

        serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.0f, 1.4f);
        serverLevel.sendParticles(ParticleTypes.LAVA, eyePos.x, eyePos.y, eyePos.z, 5, 0.3, 0.3, 0.3, 0.05);
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        if (!level().isClientSide() && level() instanceof ServerLevel serverLevel) {
            WorldProgressionData progression = WorldProgressionData.get(serverLevel.getServer());
            progression.markBossDefeated("golem");

            // Drops: Beetle Husks (4 - 8)
            int husks = 4 + serverLevel.random.nextInt(5);
            ItemEntity huskDrop = new ItemEntity(serverLevel, getX(), getY(), getZ(),
                    new ItemStack(ModItems.BEETLE_HUSK.get(), husks));
            serverLevel.addFreshEntity(huskDrop);

            serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                    Component.literal("Golem has been defeated!").withStyle(net.minecraft.ChatFormatting.GREEN, net.minecraft.ChatFormatting.BOLD),
                    false
            );

            // Lunatic Cultists message
            serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                    Component.literal("Mysterious devotees have appeared at the Dungeon entrance...").withStyle(net.minecraft.ChatFormatting.DARK_AQUA, net.minecraft.ChatFormatting.ITALIC),
                    false
            );
        }
    }

    static class GolemCombatGoal extends Goal {
        private final GolemEntity golem;
        private int jumpCooldown = 0;

        public GolemCombatGoal(GolemEntity golem) {
            this.golem = golem;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return golem.getTarget() != null && golem.getTarget().isAlive();
        }

        @Override
        public void tick() {
            LivingEntity target = golem.getTarget();
            if (target == null) return;

            golem.getLookControl().setLookAt(target, 30.0f, 30.0f);
            golem.getNavigation().moveTo(target, 1.0);

            jumpCooldown++;
            // High leap stomp attack
            if (jumpCooldown >= 80 && golem.onGround()) {
                jumpCooldown = 0;
                Vec3 jumpVec = target.position().subtract(golem.position()).normalize().scale(0.8);
                golem.setDeltaMovement(jumpVec.x, 0.9, jumpVec.z);
                if (golem.level() instanceof ServerLevel serverLevel) {
                    serverLevel.playSound(null, golem.getX(), golem.getY(), golem.getZ(),
                            SoundEvents.IRON_GOLEM_ATTACK, SoundSource.HOSTILE, 1.5f, 0.6f);
                }
            }

            if (golem.distanceToSqr(target) < 9.0) {
                target.hurt(golem.damageSources().mobAttack(golem), 70.0f);
            }
        }
    }
}
