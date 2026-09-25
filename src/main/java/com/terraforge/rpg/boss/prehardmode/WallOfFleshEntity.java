package com.terraforge.rpg.boss.prehardmode;

import com.terraforge.rpg.boss.BossPhase;
import com.terraforge.rpg.boss.TerraBaseBoss;
import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.entity.projectile.TerraProjectileEntity;
import com.terraforge.rpg.registry.ModBlocks;
import com.terraforge.rpg.registry.ModEntities;
import com.terraforge.rpg.registry.ModItems;
import com.terraforge.rpg.world.progression.WorldProgressionData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Wall of Flesh pre-hardmode final boss.
 * Canonical Terraria 1.4.5.8: 8000 HP, 12 Defense.
 * Sweeps horizontally across the Underworld, unleashing lasers and The Hungry minions.
 * Defeating it releases the ancient spirits of light and dark, initiating Hardmode.
 */
public class WallOfFleshEntity extends TerraBaseBoss {

    public static final double BASE_HEALTH = 8000.0;
    public static final int BASE_DEFENSE = 12;
    public static final double BASE_ATTACK_DAMAGE = 50.0;

    private static final Random RANDOM = new Random();

    private final List<TheHungryEntity> hungryMinions = new ArrayList<>();
    private int laserCooldown = 40;
    private int minionSpawnCooldown = 60;
    private Vec3 sweepDirection = new Vec3(1.0, 0.0, 0.0);

    public WallOfFleshEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level, "wall_of_flesh", 60_000L, 100_000L, BASE_DEFENSE, 120, BossEvent.BossBarColor.RED);
        this.noPhysics = true; // Sweeps through underworld terrain
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, 0.20)
                .add(Attributes.ATTACK_DAMAGE, BASE_ATTACK_DAMAGE)
                .add(Attributes.FOLLOW_RANGE, 120.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    public void setSweepDirection(Vec3 direction) {
        this.sweepDirection = direction.normalize();
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();

        if (this.getTarget() == null || !this.getTarget().isAlive()) {
            Player nearest = this.level().getNearestPlayer(this, 120.0);
            if (nearest != null) {
                this.setTarget(nearest);
            }
        }

        LivingEntity target = this.getTarget();

        // Calculate sweep speed based on remaining health
        double healthRatio = (double) this.getHealth() / (double) this.getMaxHealth();
        double currentSpeed = 0.15; // Phase 1 base speed

        if (healthRatio <= 0.20) {
            currentSpeed = 0.38; // Terraria canonical desperate sprint
            this.setEnraged(true);
        } else if (healthRatio <= 0.50) {
            currentSpeed = 0.25; // Phase 2
        }

        // Steer towards target on perpendicular axis, but sweep continuously forward
        if (target != null) {
            Vec3 toTarget = target.position().subtract(this.position());
            // Align sweep direction towards target generally
            if (toTarget.lengthSqr() > 0.1) {
                Vec3 targetDir = new Vec3(toTarget.x, 0, toTarget.z).normalize();
                sweepDirection = sweepDirection.scale(0.95).add(targetDir.scale(0.05)).normalize();
            }
        }

        this.setDeltaMovement(sweepDirection.scale(currentSpeed));
        this.setPos(this.getX() + this.getDeltaMovement().x, this.getY(), this.getZ() + this.getDeltaMovement().z);

        // Laser attacks
        int currentLaserInterval = healthRatio <= 0.20 ? 10 : (healthRatio <= 0.50 ? 20 : 40);
        laserCooldown--;
        if (laserCooldown <= 0) {
            laserCooldown = currentLaserInterval;
            fireLaserAtTarget();
        }

        // Minion spawning (up to 6 The Hungry)
        minionSpawnCooldown--;
        if (minionSpawnCooldown <= 0) {
            minionSpawnCooldown = 80;
            maintainMinions();
        }
    }

    private void fireLaserAtTarget() {
        LivingEntity target = this.getTarget();
        if (target == null || !(this.level() instanceof ServerLevel serverLevel)) return;

        Vec3 eyePos = this.position().add(0, 4.0, 0);
        Vec3 toTarget = target.getEyePosition().subtract(eyePos).normalize();

        TerraProjectileEntity laser = new TerraProjectileEntity(ModEntities.TERRA_PROJECTILE.get(), serverLevel);
        laser.setPos(eyePos.x, eyePos.y, eyePos.z);
        laser.setOwner(this);
        laser.setDamageClass(DamageClass.MAGIC);
        laser.setDamage(22.0); // 22 laser damage
        laser.setMaxPierces(1);
        laser.setProjectileGravity(0.0); // No gravity laser
        laser.shoot(toTarget.x, toTarget.y, toTarget.z, 1.4f, 0.5f);

        serverLevel.addFreshEntity(laser);
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.5f, 1.8f);
    }

    private void maintainMinions() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        hungryMinions.removeIf(m -> !m.isAlive());

        if (hungryMinions.size() < 6) {
            TheHungryEntity minion = new TheHungryEntity(ModEntities.THE_HUNGRY.get(), serverLevel);
            double offsetX = (RANDOM.nextDouble() - 0.5) * 4.0;
            double offsetY = RANDOM.nextDouble() * 5.0;
            double offsetZ = (RANDOM.nextDouble() - 0.5) * 4.0;
            minion.setPos(this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ);
            minion.setParentWall(this);
            minion.setTarget(this.getTarget());
            serverLevel.addFreshEntity(minion);
            hungryMinions.add(minion);
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        if (!this.level().isClientSide() && this.getServer() != null) {
            ServerLevel serverLevel = (ServerLevel) this.level();

            // 1. Activate Hardmode
            WorldProgressionData progression = WorldProgressionData.get(this.getServer());
            progression.activateHardmode(serverLevel);

            // 2. Build safe box around drops so they never burn in underworld lava
            BlockPos dropPos = this.blockPosition();
            createSafeLootBox(serverLevel, dropPos);

            // 3. Drop guaranteed Pwnhammer
            this.spawnAtLocation(new ItemStack(ModItems.PWNHAMMER.get()));

            // 4. Drop guaranteed random Emblem (Warrior, Ranger, Sorcerer, Summoner)
            ItemStack randomEmblem = switch (RANDOM.nextInt(4)) {
                case 0 -> new ItemStack(ModItems.WARRIOR_EMBLEM.get());
                case 1 -> new ItemStack(ModItems.RANGER_EMBLEM.get());
                case 2 -> new ItemStack(ModItems.SORCERER_EMBLEM.get());
                default -> new ItemStack(ModItems.SUMMONER_EMBLEM.get());
            };
            this.spawnAtLocation(randomEmblem);

            // 5. Drop 8 Gold coins (80,000 copper)
            this.spawnAtLocation(new ItemStack(ModItems.GOLD_COIN.get(), 8));

            // Clean up attached minions
            for (TheHungryEntity minion : hungryMinions) {
                if (minion.isAlive()) {
                    minion.discard();
                }
            }
        }
    }

    private void createSafeLootBox(ServerLevel level, BlockPos center) {
        // Create 5x3x5 ash box with hollow interior to secure loot from lava
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                // Solid floor
                level.setBlock(center.offset(x, -1, z), ModBlocks.ASH_BLOCK.get().defaultBlockState(), 3);
                // Hollow walls
                if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                    for (int y = 0; y <= 2; y++) {
                        level.setBlock(center.offset(x, y, z), ModBlocks.ASH_BLOCK.get().defaultBlockState(), 3);
                    }
                }
            }
        }
    }
}
