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
 * The Destroyer - Colossal mechanical worm boss (Terraria 1.4.5.8 canonical).
 * Has 80,000 HP, high contact damage, releases autonomous Probes upon taking damage,
 * and unleashes barrages of red laser fire.
 */
public class TheDestroyerEntity extends TerraBaseBoss {

    public static final double BASE_HEALTH = 80000.0;
    private int laserBarrageTimer = 0;

    public TheDestroyerEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level, "the_destroyer", 120_000L, 250_000L, 0, 128, BossEvent.BossBarColor.RED);
        this.moveControl = new FlyingMoveControl(this, 25, true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HEALTH)
                .add(Attributes.FLYING_SPEED, 0.50)
                .add(Attributes.MOVEMENT_SPEED, 0.45)
                .add(Attributes.ATTACK_DAMAGE, 72.0)
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
        this.goalSelector.addGoal(1, new DestroyerWormGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public int getTerrariaDefense() {
        return 0; // Head has 0 defense canonical
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean damaged = super.hurt(source, amount);
        if (damaged && !level().isClientSide() && level() instanceof ServerLevel serverLevel) {
            // Chance to release a Probe on hit (Terraria rule)
            if (serverLevel.random.nextFloat() < 0.15f) {
                spawnProbe(serverLevel);
            }
        }
        return damaged;
    }

    private void spawnProbe(ServerLevel serverLevel) {
        DestroyerProbeEntity probe = new DestroyerProbeEntity(ModEntities.DESTROYER_PROBE.get(), serverLevel);
        probe.setPos(getX() + (serverLevel.random.nextDouble() - 0.5) * 4.0,
                getY() + serverLevel.random.nextDouble() * 2.0,
                getZ() + (serverLevel.random.nextDouble() - 0.5) * 4.0);
        serverLevel.addFreshEntity(probe);
        serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.IRON_GOLEM_HURT, SoundSource.HOSTILE, 1.0f, 1.8f);
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();

        // Despawn at daytime
        long dayTime = this.level().getDayTime() % 24000;
        if (dayTime < 13000 || dayTime > 23500) {
            this.setDeltaMovement(0, -1.5, 0); // Burrows into the ground to despawn
            if (this.getY() < -60) {
                this.discard();
            }
            return;
        }

        laserBarrageTimer++;
        if (laserBarrageTimer >= 60 && level() instanceof ServerLevel serverLevel) {
            laserBarrageTimer = 0;
            fireBodyLasers(serverLevel);
        }

        // Particle trail simulating mechanical segments
        if (level() instanceof ServerLevel serverLevel && tickCount % 2 == 0) {
            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, getX(), getY() + 0.5, getZ(), 2, 0.5, 0.5, 0.5, 0.02);
            serverLevel.sendParticles(ParticleTypes.LAVA, getX(), getY() + 0.3, getZ(), 1, 0.2, 0.2, 0.2, 0.05);
        }
    }

    private void fireBodyLasers(ServerLevel serverLevel) {
        Player target = level().getNearestPlayer(this, 64.0);
        if (target == null) return;

        // Radial burst of lasers from segments
        for (int i = 0; i < 4; i++) {
            double angle = (Math.PI * 2.0 / 4.0) * i + (tickCount * 0.1);
            Vec3 dir = new Vec3(Math.cos(angle), (serverLevel.random.nextDouble() - 0.5) * 0.5, Math.sin(angle)).normalize();

            TerraProjectileEntity laser = new TerraProjectileEntity(ModEntities.TERRA_PROJECTILE.get(), serverLevel);
            laser.setPos(getX(), getY() + 1.0, getZ());
            laser.setOwner(this);
            laser.setDamageClass(DamageClass.RANGED);
            laser.setDamage(22.0);
            laser.setProjectileGravity(0.0);
            laser.shoot(dir.x, dir.y, dir.z, 1.6f, 1.0f);
            serverLevel.addFreshEntity(laser);
        }

        serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.0f, 1.6f);
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        if (!level().isClientSide() && level() instanceof ServerLevel serverLevel) {
            WorldProgressionData progression = WorldProgressionData.get(serverLevel.getServer());
            progression.markBossDefeated("the_destroyer");

            // Drops: Soul of Might + Hallowed Bars
            ItemEntity soulDrop = new ItemEntity(serverLevel, getX(), getY(), getZ(),
                    new ItemStack(ModItems.SOUL_OF_MIGHT.get(), 25 + serverLevel.random.nextInt(15)));
            serverLevel.addFreshEntity(soulDrop);

            ItemEntity barDrop = new ItemEntity(serverLevel, getX(), getY(), getZ(),
                    new ItemStack(ModItems.HALLOWED_BAR.get(), 15 + serverLevel.random.nextInt(15)));
            serverLevel.addFreshEntity(barDrop);

            serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                    Component.literal("The Destroyer has been defeated!").withStyle(net.minecraft.ChatFormatting.GREEN, net.minecraft.ChatFormatting.BOLD),
                    false
            );
        }
    }

    static class DestroyerWormGoal extends Goal {
        private final TheDestroyerEntity worm;
        private double swoopPhase = 0;

        public DestroyerWormGoal(TheDestroyerEntity worm) {
            this.worm = worm;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return worm.getTarget() != null && worm.getTarget().isAlive();
        }

        @Override
        public void tick() {
            LivingEntity target = worm.getTarget();
            if (target == null) return;

            worm.getLookControl().setLookAt(target, 30.0f, 30.0f);
            swoopPhase += 0.05;

            // Fluid sinusoidal burrow-and-charge wave trajectory
            double targetX = target.getX() + Math.cos(swoopPhase) * 6.0;
            double targetY = target.getY() + Math.sin(swoopPhase * 2.0) * 8.0;
            double targetZ = target.getZ() + Math.sin(swoopPhase) * 6.0;

            worm.getMoveControl().setWantedPosition(targetX, targetY, targetZ, 1.35);

            if (worm.distanceToSqr(target) < 12.0) {
                target.hurt(worm.damageSources().mobAttack(worm), 72.0f);
            }
        }
    }
}
