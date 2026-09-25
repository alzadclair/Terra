package com.terraforge.rpg.entity.mount;

import com.terraforge.rpg.registry.ModEntities;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Slime Mount entity summoned by the Slimy Saddle.
 * Canonical Terraria 1.4.5.8: High bouncing jumps, complete fall damage immunity,
 * liquid floating, and stomping enemies for 40 damage.
 */
public class SlimeMountEntity extends PathfinderMob {

    public SlimeMountEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createMountAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    public LivingEntity getControllingPassenger() {
        return this.getFirstPassenger() instanceof LivingEntity living ? living : null;
    }

    @Override
    public void tick() {
        super.tick();

        // If no passengers remain, despawn the mount
        if (!this.level().isClientSide() && this.getPassengers().isEmpty()) {
            this.discard();
            return;
        }

        // Float on water and lava
        if (this.isInWater() || this.isInLava()) {
            this.setDeltaMovement(this.getDeltaMovement().x, 0.25, this.getDeltaMovement().z);
        }

        // Stomp enemies when descending
        if (this.getDeltaMovement().y < -0.2 && !this.level().isClientSide()) {
            List<LivingEntity> enemies = this.level().getEntitiesOfClass(
                    LivingEntity.class,
                    this.getBoundingBox().inflate(0.5, 0.2, 0.5),
                    e -> e != this && !(e instanceof Player) && e != this.getFirstPassenger()
            );

            for (LivingEntity enemy : enemies) {
                enemy.hurt(this.damageSources().mobAttack(this), 40.0f); // 40 mount damage
                this.setDeltaMovement(this.getDeltaMovement().x, 0.65, this.getDeltaMovement().z); // Bounce back up!
                this.hasImpulse = true;
                break;
            }
        }
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false; // Complete fall damage immunity
    }
}
