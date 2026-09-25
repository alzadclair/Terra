package com.terraforge.rpg.boss.hardmode;

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
 * Skeletron Prime - Upgraded mechanical skull boss (Terraria 1.4.5.8 canonical).
 * Features spinning skull charge with doubled defense, 4 automated weapons systems
 * (Laser, Cannon, Saw, Vice), and daylight Dungeon Guardian enrage mode.
 */
public class SkeletronPrimeEntity extends TerraBaseBoss {

    public static final double BASE_HEALTH = 28000.0;
    private boolean spinning = false;
    private int spinTimer = 0;
    private int attackCycleTimer = 0;

    public SkeletronPrimeEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level, "skeletron_prime", 120_000L, 250_000L, 24, 96, BossEvent.BossBarColor.RED);
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HEALTH)
                .add(Attributes.FLYING_SPEED, 0.45)
                .add(Attributes.MOVEMENT_SPEED, 0.40)
                .add(Attributes.ATTACK_DAMAGE, 47.0)
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
        this.goalSelector.addGoal(1, new SkeletronPrimeGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public boolean isSpinning() {
        return spinning;
    }

    public void setSpinning(boolean spinning) {
        this.spinning = spinning;
    }

    @Override
    public int getTerrariaDefense() {
        if (isEnraged()) return 9999;
        // Doubles from 24 to 48 while head is spinning (canonical Terraria)
        return spinning ? 48 : 24;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();

        long dayTime = this.level().getDayTime() % 24000;
        // Enrage if daylight arrives (canonical Dungeon Guardian behavior)
        if (dayTime >= 0 && dayTime < 13000 && !isEnraged()) {
            setEnraged(true);
            if (level() instanceof ServerLevel serverLevel) {
                serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                        Component.literal("Skeletron Prime has become enraged by the daylight!").withStyle(net.minecraft.ChatFormatting.DARK_RED, net.minecraft.ChatFormatting.BOLD),
                        false
                );
            }
        }

        // Particle effects when spinning
        if (spinning && level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CRIT, getX(), getY() + 0.5, getZ(), 6, 0.5, 0.5, 0.5, 0.1);
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 0.5, getZ(), 3, 0.4, 0.4, 0.4, 0.05);
        }

        // Weapon systems automated cycling
        attackCycleTimer++;
        if (attackCycleTimer % 45 == 0 && level() instanceof ServerLevel serverLevel && !spinning) {
            firePrimeWeapons(serverLevel);
        }
    }

    private void firePrimeWeapons(ServerLevel serverLevel) {
        LivingEntity target = getTarget();
        if (target == null) return;

        Vec3 eyePos = getEyePosition();
        Vec3 dir = target.getEyePosition().subtract(eyePos).normalize();

        // 1. Prime Laser
        TerraProjectileEntity laser = new TerraProjectileEntity(ModEntities.TERRA_PROJECTILE.get(), serverLevel);
        laser.setPos(eyePos.x + 1.5, eyePos.y, eyePos.z);
        laser.setOwner(this);
        laser.setDamageClass(DamageClass.RANGED);
        laser.setDamage(26.0);
        laser.setProjectileGravity(0.0);
        laser.shoot(dir.x, dir.y, dir.z, 2.0f, 0.5f);
        serverLevel.addFreshEntity(laser);

        // 2. Prime Cannon (Bomb with gravity)
        TerraProjectileEntity bomb = new TerraProjectileEntity(ModEntities.TERRA_PROJECTILE.get(), serverLevel);
        bomb.setPos(eyePos.x - 1.5, eyePos.y, eyePos.z);
        bomb.setOwner(this);
        bomb.setDamageClass(DamageClass.RANGED);
        bomb.setDamage(35.0);
        bomb.setProjectileGravity(0.03);
        bomb.shoot(dir.x, dir.y + 0.2, dir.z, 1.3f, 1.0f);
        serverLevel.addFreshEntity(bomb);

        serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 0.8f, 1.4f);
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        if (!level().isClientSide() && level() instanceof ServerLevel serverLevel) {
            WorldProgressionData progression = WorldProgressionData.get(serverLevel.getServer());
            progression.markBossDefeated("skeletron_prime");

            // Drops: Soul of Fright + Hallowed Bars
            ItemEntity soulDrop = new ItemEntity(serverLevel, getX(), getY(), getZ(),
                    new ItemStack(ModItems.SOUL_OF_FRIGHT.get(), 25 + serverLevel.random.nextInt(15)));
            serverLevel.addFreshEntity(soulDrop);

            ItemEntity barDrop = new ItemEntity(serverLevel, getX(), getY(), getZ(),
                    new ItemStack(ModItems.HALLOWED_BAR.get(), 15 + serverLevel.random.nextInt(15)));
            serverLevel.addFreshEntity(barDrop);

            serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                    Component.literal("Skeletron Prime has been defeated!").withStyle(net.minecraft.ChatFormatting.GREEN, net.minecraft.ChatFormatting.BOLD),
                    false
            );

            // Check if all 3 mechanical bosses are defeated
            if (progression.hasDefeatedAllMechBosses()) {
                serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                        Component.literal("The jungle grows restless...").withStyle(net.minecraft.ChatFormatting.DARK_GREEN, net.minecraft.ChatFormatting.ITALIC),
                        false
                );
            }
        }
    }

    static class SkeletronPrimeGoal extends Goal {
        private final SkeletronPrimeEntity boss;
        private int spinCooldown = 0;
        private int spinDuration = 0;

        public SkeletronPrimeGoal(SkeletronPrimeEntity boss) {
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

            if (boss.isEnraged()) {
                // Enraged Dungeon Guardian mode: hyper-speed lunge
                Vec3 chargeDir = target.getEyePosition().subtract(boss.getEyePosition()).normalize().scale(1.8);
                boss.setDeltaMovement(chargeDir);
                if (boss.distanceToSqr(target) < 9.0) {
                    target.hurt(boss.damageSources().mobAttack(boss), 1000.0f);
                }
                return;
            }

            if (boss.isSpinning()) {
                spinDuration++;
                // Head Spin Charge: charges player directly dealing massive contact damage (94.0)
                Vec3 rush = target.getEyePosition().subtract(boss.getEyePosition()).normalize().scale(1.2);
                boss.setDeltaMovement(rush);

                if (boss.distanceToSqr(target) < 10.0) {
                    target.hurt(boss.damageSources().mobAttack(boss), 94.0f);
                }

                if (spinDuration >= 140) {
                    boss.setSpinning(false);
                    spinDuration = 0;
                    spinCooldown = 0;
                }
                return;
            }

            spinCooldown++;
            // Normal hovering above target
            double targetX = target.getX() + Math.cos(boss.tickCount * 0.04) * 8.0;
            double targetY = target.getY() + 6.0;
            double targetZ = target.getZ() + Math.sin(boss.tickCount * 0.04) * 8.0;
            boss.getMoveControl().setWantedPosition(targetX, targetY, targetZ, 1.1);

            if (spinCooldown >= 200) {
                // Enter skull spin mode
                boss.setSpinning(true);
                spinCooldown = 0;
                if (boss.level() instanceof ServerLevel serverLevel) {
                    serverLevel.playSound(null, boss.getX(), boss.getY(), boss.getZ(),
                            SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 1.5f, 0.8f);
                }
            }
        }
    }
}
