package com.terraforge.rpg.entity.projectile;

import com.terraforge.rpg.combat.CombatContext;
import com.terraforge.rpg.combat.DamageCalculator;
import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.combat.DamageTypeCategory;
import com.terraforge.rpg.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Server-authoritative entity for Terraria Yoyos.
 * Handles continuous flight tethering, block bouncing, and multihit tick rates.
 */
public class YoyoEntity extends Entity {

    private static final EntityDataAccessor<Boolean> RETRACTING =
            SynchedEntityData.defineId(YoyoEntity.class, EntityDataSerializers.BOOLEAN);

    private UUID ownerUuid;
    private double reach = 9.0;
    private int maxFlightTicks = 80; // 4 seconds default
    private double damage = 9.0;
    private double critChance = 4.0;
    private double knockback = 3.0;

    private final Map<UUID, Integer> hitCooldowns = new HashMap<>();

    public YoyoEntity(EntityType<? extends YoyoEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public YoyoEntity(Level level, Player owner, double reach, int flightTicks, double damage, double crit, double kb) {
        this(ModEntities.YOYO.get(), level);
        this.ownerUuid = owner.getUUID();
        this.reach = reach;
        this.maxFlightTicks = flightTicks;
        this.damage = damage;
        this.critChance = crit;
        this.knockback = kb;
        this.setPos(owner.getX(), owner.getEyeY() - 0.2, owner.getZ());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RETRACTING, false);
    }

    @Override
    public void tick() {
        super.tick();

        Player owner = getOwnerPlayer();
        if (owner == null || !owner.isAlive()) {
            this.discard();
            return;
        }

        if (this.level().isClientSide()) {
            // Client visual spin particles
            this.level().addParticle(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
            return;
        }

        boolean isRetracting = this.entityData.get(RETRACTING);

        // Check if player released use or flight time ended
        if (!isRetracting) {
            boolean holdingUse = owner.isUsingItem();
            boolean flightExpired = maxFlightTicks > 0 && this.tickCount >= maxFlightTicks;

            if (!holdingUse || flightExpired) {
                isRetracting = true;
                this.entityData.set(RETRACTING, true);
            }
        }

        if (isRetracting) {
            // Rapid return to owner
            Vec3 toOwner = owner.getEyePosition().subtract(this.position());
            if (toOwner.lengthSqr() < 1.0) {
                this.discard();
                return;
            }
            Vec3 move = toOwner.normalize().scale(0.85);
            this.setPos(this.getX() + move.x, this.getY() + move.y, this.getZ() + move.z);
        } else {
            // Fly toward crosshair target up to reach distance
            Vec3 eyePos = owner.getEyePosition();
            Vec3 look = owner.getLookAngle();
            Vec3 targetPos = eyePos.add(look.scale(this.reach));

            Vec3 toTarget = targetPos.subtract(this.position());
            double dist = toTarget.length();
            if (dist > 0.1) {
                Vec3 move = toTarget.normalize().scale(Math.min(dist, 0.45));
                this.setPos(this.getX() + move.x, this.getY() + move.y, this.getZ() + move.z);
            }

            // Damage nearby enemies every 6 ticks (3.33 hits/sec)
            tickHitEntities(owner);
        }

        // Decrement hit cooldowns
        hitCooldowns.entrySet().removeIf(entry -> {
            entry.setValue(entry.getValue() - 1);
            return entry.getValue() <= 0;
        });
    }

    private void tickHitEntities(Player owner) {
        AABB box = this.getBoundingBox().inflate(0.3);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, box, e -> !e.is(owner) && e.isAlive());

        for (LivingEntity target : targets) {
            if (hitCooldowns.containsKey(target.getUUID())) continue;

            DamageSource damageSource = this.damageSources().playerAttack(owner);
            CombatContext context = new CombatContext(owner, target, damageSource, this.damage);
            context.setDamageClass(DamageClass.MELEE);
            context.setCategory(DamageTypeCategory.PHYSICAL);

            double finalDamage = DamageCalculator.calculateDamage(context);
            target.hurt(damageSource, (float) finalDamage);

            // Apply slight knockback outward
            Vec3 kb = target.position().subtract(this.position()).normalize().scale(this.knockback * 0.15);
            target.push(kb.x, 0.05, kb.z);

            hitCooldowns.put(target.getUUID(), 6); // 6 ticks cooldown between hits
        }
    }

    public Player getOwnerPlayer() {
        if (ownerUuid == null) return null;
        return this.level().getPlayerByUUID(ownerUuid);
    }

    public double getReach() { return reach; }
    public int getMaxFlightTicks() { return maxFlightTicks; }
    public double getDamage() { return damage; }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("owner")) {
            this.ownerUuid = tag.getUUID("owner");
        }
        this.reach = tag.getDouble("reach");
        this.maxFlightTicks = tag.getInt("max_flight_ticks");
        this.damage = tag.getDouble("damage");
        this.critChance = tag.getDouble("crit_chance");
        this.knockback = tag.getDouble("knockback");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUuid != null) {
            tag.putUUID("owner", ownerUuid);
        }
        tag.putDouble("reach", reach);
        tag.putInt("max_flight_ticks", maxFlightTicks);
        tag.putDouble("damage", damage);
        tag.putDouble("crit_chance", critChance);
        tag.putDouble("knockback", knockback);
    }
}
