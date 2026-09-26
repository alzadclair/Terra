package com.terraforge.rpg.entity.projectile;

import com.terraforge.rpg.combat.CombatContext;
import com.terraforge.rpg.combat.DamageCalculator;
import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.combat.DamageTypeCategory;
import com.terraforge.rpg.registry.ModEntities;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Server-authoritative modular projectile framework for Terraria 1.4.5.8 weapons.
 * Supports customizable piercing, bouncing, gravity, range, and particle trails.
 */
public class TerraProjectileEntity extends Projectile {

    private DamageClass damageClass = DamageClass.RANGED;
    private double damage = 10.0;
    private double critChance = 4.0;
    private double knockback = 2.0;

    private int maxPierces = 0; // 0 = despawns on first hit
    private int currentPierces = 0;

    private int maxBounces = 0; // 0 = despawns on wall contact
    private int currentBounces = 0;

    private int maxLifeTicks = 200; // 10 seconds default
    private double gravity = 0.03; // Standard arrow gravity (0.0 for magic / lasers)

    private final Set<UUID> hitEntities = new HashSet<>();

    public TerraProjectileEntity(EntityType<? extends TerraProjectileEntity> type, Level level) {
        super(type, level);
    }

    public TerraProjectileEntity(Level level, LivingEntity shooter, double damage, DamageClass damageClass) {
        this(ModEntities.TERRA_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());
        this.damage = damage;
        this.damageClass = damageClass;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // No custom synced values required on the entity data tracker yet
    }

    @Override
    public void tick() {
        super.tick();

        if (this.tickCount >= maxLifeTicks) {
            this.discard();
            return;
        }

        // Raycast hit detection
        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitResult.getType() != HitResult.Type.MISS) {
            onHit(hitResult);
        }

        // Apply movement vector
        Vec3 motion = this.getDeltaMovement();
        double nextX = this.getX() + motion.x;
        double nextY = this.getY() + motion.y;
        double nextZ = this.getZ() + motion.z;

        this.setPos(nextX, nextY, nextZ);

        // Apply gravity and drag
        if (gravity > 0.0 && !this.isNoGravity()) {
            this.setDeltaMovement(motion.x * 0.99, motion.y - gravity, motion.z * 0.99);
        }

        // Spawn particles
        if (this.level().isClientSide()) {
            spawnTrailParticles();
        }
    }

    protected void spawnTrailParticles() {
        this.level().addParticle(
                ParticleTypes.CRIT,
                this.getX(), this.getY(), this.getZ(),
                0.0, 0.0, 0.0
        );
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        if (!super.canHitEntity(target)) return false;
        if (hitEntities.contains(target.getUUID())) return false;
        Entity owner = getOwner();
        return owner == null || !target.is(owner);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide()) return;

        Entity entity = result.getEntity();
        if (entity instanceof LivingEntity target) {
            hitEntities.add(target.getUUID());
            Entity owner = getOwner();
            LivingEntity attacker = owner instanceof LivingEntity living ? living : null;

            DamageSource damageSource;
            if (attacker instanceof net.minecraft.world.entity.player.Player player) {
                damageSource = this.damageSources().thrown(this, player);
            } else if (attacker != null) {
                damageSource = this.damageSources().mobAttack(attacker);
            } else {
                damageSource = this.damageSources().thrown(this, this);
            }

            CombatContext context = new CombatContext(attacker, target, damageSource, this.damage);
            context.setDamageClass(this.damageClass);
            context.setCategory(this.damageClass.isPhysicalDefault() ? DamageTypeCategory.PHYSICAL : DamageTypeCategory.MAGICAL);

            boolean isCrit = (this.random.nextDouble() * 100.0) < this.critChance;
            if (isCrit) {
                context.setCritical(true);
                context.setCritMultiplier(2.0); // Canonical Terraria 200% critical hit damage
            }

            double finalDamage = DamageCalculator.calculateDamage(context);
            target.hurt(damageSource, (float) finalDamage);

            if (isCrit && this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.CRIT,
                        target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                        12, 0.3, 0.3, 0.3, 0.15
                );
            }

            // Apply knockback
            Vec3 motion = this.getDeltaMovement().normalize().scale(this.knockback * 0.2);
            target.push(motion.x, 0.1, motion.z);

            // Check piercing limit
            currentPierces++;
            if (currentPierces > maxPierces) {
                this.discard();
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (this.level().isClientSide()) return;

        if (currentBounces < maxBounces) {
            currentBounces++;
            // Reflect velocity based on hit normal
            Vec3 normal = Vec3.atLowerCornerOf(result.getDirection().getNormal());
            Vec3 motion = this.getDeltaMovement();
            double dot = motion.dot(normal);
            Vec3 bounce = motion.subtract(normal.scale(2 * dot)).scale(0.85); // 15% velocity loss on bounce
            this.setDeltaMovement(bounce);
        } else {
            this.discard();
        }
    }

    // Configuration getters & setters
    public DamageClass getDamageClass() { return damageClass; }
    public void setDamageClass(DamageClass damageClass) { this.damageClass = damageClass; }

    public double getDamage() { return damage; }
    public void setDamage(double damage) { this.damage = damage; }

    public double getCritChance() { return critChance; }
    public void setCritChance(double critChance) { this.critChance = critChance; }

    public double getKnockback() { return knockback; }
    public void setKnockback(double knockback) { this.knockback = knockback; }

    public int getMaxPierces() { return maxPierces; }
    public void setMaxPierces(int maxPierces) { this.maxPierces = maxPierces; }

    public int getMaxBounces() { return maxBounces; }
    public void setMaxBounces(int maxBounces) { this.maxBounces = maxBounces; }

    public int getMaxLifeTicks() { return maxLifeTicks; }
    public void setMaxLifeTicks(int maxLifeTicks) { this.maxLifeTicks = maxLifeTicks; }

    public double getProjectileGravity() { return gravity; }
    public void setProjectileGravity(double gravity) { this.gravity = gravity; }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putDouble("damage", damage);
        tag.putString("damage_class", damageClass.name());
        tag.putDouble("crit_chance", critChance);
        tag.putDouble("knockback", knockback);
        tag.putInt("max_pierces", maxPierces);
        tag.putInt("current_pierces", currentPierces);
        tag.putInt("max_bounces", maxBounces);
        tag.putInt("current_bounces", currentBounces);
        tag.putInt("max_life", maxLifeTicks);
        tag.putDouble("gravity", gravity);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.damage = tag.getDouble("damage");
        try {
            this.damageClass = DamageClass.valueOf(tag.getString("damage_class"));
        } catch (Exception e) {
            this.damageClass = DamageClass.RANGED;
        }
        this.critChance = tag.getDouble("crit_chance");
        this.knockback = tag.getDouble("knockback");
        this.maxPierces = tag.getInt("max_pierces");
        this.currentPierces = tag.getInt("current_pierces");
        this.maxBounces = tag.getInt("max_bounces");
        this.currentBounces = tag.getInt("current_bounces");
        this.maxLifeTicks = tag.getInt("max_life");
        this.gravity = tag.getDouble("gravity");
    }
}
