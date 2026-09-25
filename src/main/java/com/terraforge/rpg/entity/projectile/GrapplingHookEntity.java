package com.terraforge.rpg.entity.projectile;

import com.terraforge.rpg.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Server-authoritative Grappling Hook entity.
 * Anchors into solid blocks and pulls the owner towards the anchor point.
 */
public class GrapplingHookEntity extends Projectile {

    private boolean anchored = false;
    private double maxRange = 18.0;
    private double pullSpeed = 0.8;
    private int lifeTicks = 0;

    public GrapplingHookEntity(EntityType<? extends GrapplingHookEntity> type, Level level) {
        super(type, level);
    }

    public GrapplingHookEntity(Level level, LivingEntity owner, double maxRange, double pullSpeed) {
        this(ModEntities.GRAPPLING_HOOK.get(), level);
        this.setOwner(owner);
        this.setPos(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
        this.maxRange = maxRange;
        this.pullSpeed = pullSpeed;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    public boolean isAnchored() {
        return anchored;
    }

    @Override
    public void tick() {
        super.tick();
        lifeTicks++;

        Entity owner = this.getOwner();
        if (owner == null || !owner.isAlive() || lifeTicks > 200) {
            this.discard();
            return;
        }

        double distSq = this.distanceToSqr(owner);
        if (distSq > (maxRange * maxRange * 1.5)) {
            this.discard();
            return;
        }

        if (anchored) {
            // Pull owner toward hook position
            Vec3 toHook = this.position().subtract(owner.position());
            double dist = toHook.length();

            if (dist < 1.8) {
                // Arrived at hook
                owner.setDeltaMovement(0, 0, 0);
                this.discard();
                return;
            }

            Vec3 pull = toHook.normalize().scale(pullSpeed);
            owner.setDeltaMovement(pull);
            owner.fallDistance = 0.0f;
            owner.hasImpulse = true;
        } else {
            // In flight hit-detection
            HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                onHitBlock((BlockHitResult) hitResult);
            } else {
                this.setPos(this.getX() + this.getDeltaMovement().x,
                        this.getY() + this.getDeltaMovement().y,
                        this.getZ() + this.getDeltaMovement().z);
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        BlockPos hitPos = result.getBlockPos();
        if (this.level().getBlockState(hitPos).isSolid()) {
            this.anchored = true;
            this.setDeltaMovement(0, 0, 0);

            if (!this.level().isClientSide()) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.METAL_FALL, SoundSource.PLAYERS, 1.0f, 1.5f);
            }
        }
    }
}
