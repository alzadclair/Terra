package com.terraforge.rpg.boss.prehardmode;

import com.terraforge.rpg.boss.BossPhase;
import com.terraforge.rpg.boss.BossScalingService;
import com.terraforge.rpg.boss.TerraBaseBoss;
import com.terraforge.rpg.entity.mob.ServantOfCthulhuEntity;
import com.terraforge.rpg.registry.ModEntities;
import com.terraforge.rpg.registry.ModItems;
import net.minecraft.core.BlockPos;
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
 * Eye of Cthulhu canonical boss implementation (Terraria 1.4.5.8).
 * Features Phase 1 servant spawning/hovering, Phase 2 rapid chain dashing, and daylight despawn.
 */
public class EyeOfCthulhuEntity extends TerraBaseBoss {

    public static final double BASE_HEALTH = 2800.0;

    public EyeOfCthulhuEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level, "eye_of_cthulhu", 30_000L, 70_000L, 12, 80, BossEvent.BossBarColor.RED);
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HEALTH)
                .add(Attributes.FLYING_SPEED, 0.40)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, 15.0)
                .add(Attributes.FOLLOW_RANGE, 96.0);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanFloat(true);
        return nav;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new EyeOfCthulhuCombatGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public int getTerrariaDefense() {
        // In Phase 2, Eye of Cthulhu's defense drops to 0
        return getCurrentPhase().phaseNumber() >= 2 ? 0 : 12;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState state, BlockPos pos) {
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();

        // Daytime departure (Terraria rule: despawns if night ends)
        long dayTime = this.level().getDayTime() % 24000;
        if (dayTime < 13000 || dayTime > 23000) {
            // Ascend rapidly into space
            this.setDeltaMovement(0, 1.2, 0);
            if (this.getY() > 300) {
                this.discard();
            }
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        if (!this.level().isClientSide()) {
            // Drop Unholy Arrows (116 - 178)
            int arrowCount = 116 + mobRandom.nextInt(63);
            dropItem(new ItemStack(ModItems.UNHOLY_ARROW.get(), Math.min(arrowCount, 99)));
            if (arrowCount > 99) {
                dropItem(new ItemStack(ModItems.UNHOLY_ARROW.get(), arrowCount - 99));
            }

            // Broadcast victory
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                        Component.literal("The Eye of Cthulhu has been defeated!").withStyle(net.minecraft.ChatFormatting.GREEN),
                        false
                );
            }
        }
    }

    private void dropItem(ItemStack stack) {
        ItemEntity entity = new ItemEntity(this.level(), this.getX(), this.getY() + 0.5, this.getZ(), stack);
        entity.setDefaultPickUpDelay();
        this.level().addFreshEntity(entity);
    }

    static class EyeOfCthulhuCombatGoal extends Goal {
        private final EyeOfCthulhuEntity eye;
        private int attackTimer = 60;
        private int chargeCount = 0;
        private boolean isCharging = false;
        private Vec3 chargeVector = Vec3.ZERO;

        EyeOfCthulhuCombatGoal(EyeOfCthulhuEntity eye) {
            this.eye = eye;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return eye.getTarget() != null && eye.getTarget().isAlive();
        }

        @Override
        public void tick() {
            LivingEntity target = eye.getTarget();
            if (target == null) return;

            eye.getLookControl().setLookAt(target, 40.0f, 40.0f);
            boolean isPhase2 = eye.getCurrentPhase().phaseNumber() >= 2;
            attackTimer--;

            if (isCharging) {
                // High speed dash
                eye.setDeltaMovement(chargeVector);

                if (eye.distanceToSqr(target) < 4.0) {
                    eye.doHurtTarget(target);
                }

                if (attackTimer <= 0) {
                    isCharging = false;
                    chargeCount++;
                    int maxCharges = isPhase2 ? 5 : 3;

                    if (chargeCount >= maxCharges) {
                        chargeCount = 0;
                        attackTimer = isPhase2 ? 40 : 80; // Rest period
                    } else {
                        attackTimer = isPhase2 ? 10 : 20; // Rapid pause between chain dashes
                    }
                }
            } else {
                // Hovering positioning
                double hoverHeight = isPhase2 ? 3.0 : 6.0;
                Vec3 hoverPos = target.position().add(0, hoverHeight, 0);
                Vec3 toHover = hoverPos.subtract(eye.position());

                if (toHover.length() > 1.5) {
                    eye.setDeltaMovement(toHover.normalize().scale(isPhase2 ? 0.40 : 0.25));
                }

                // Phase 1: Spawn Servants of Cthulhu periodically
                if (!isPhase2 && attackTimer % 40 == 0 && eye.level() instanceof ServerLevel serverLevel) {
                    ServantOfCthulhuEntity servant = new ServantOfCthulhuEntity(ModEntities.SERVANT_OF_CTHULHU.get(), eye.level());
                    servant.setPos(eye.getX(), eye.getY() - 0.5, eye.getZ());
                    servant.setTarget(target);
                    serverLevel.addFreshEntity(servant);

                    serverLevel.playSound(null, eye.getX(), eye.getY(), eye.getZ(),
                            SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 1.0f, 1.4f);
                }

                // Launch charge dash
                if (attackTimer <= 0) {
                    isCharging = true;
                    double chargeSpeed = isPhase2 ? 0.85 : 0.55;
                    chargeVector = target.getEyePosition().subtract(eye.position()).normalize().scale(chargeSpeed);
                    attackTimer = isPhase2 ? 20 : 30; // Max charge duration

                    if (eye.level() != null) {
                        eye.level().playSound(null, eye.getX(), eye.getY(), eye.getZ(),
                                isPhase2 ? SoundEvents.ENDER_DRAGON_GROWL : SoundEvents.PHANTOM_BITE,
                                SoundSource.HOSTILE, 1.5f, isPhase2 ? 1.2f : 0.8f);
                    }
                }
            }
        }
    }
}
