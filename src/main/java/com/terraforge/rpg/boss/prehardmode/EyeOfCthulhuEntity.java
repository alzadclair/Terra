package com.terraforge.rpg.boss.prehardmode;

import com.terraforge.rpg.boss.BossPhase;
import com.terraforge.rpg.boss.TerraBaseBoss;
import com.terraforge.rpg.entity.mob.ServantOfCthulhuEntity;
import com.terraforge.rpg.registry.ModEntities;
import com.terraforge.rpg.registry.ModItems;
import com.terraforge.rpg.registry.ModSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
 * Features Phase 1 servant spawning/hovering, Phase 2 rapid chain dashing,
 * server-authoritative skeletal animation state synchronization, and dramatic phase transition.
 */
public class EyeOfCthulhuEntity extends TerraBaseBoss {

    public static final double BASE_HEALTH = 2800.0;

    public enum EyeAnimState {
        SPAWN,
        IDLE,
        HOVER,
        LOOK,
        SUMMON_SERVANT,
        CHARGE_PREPARE,
        CHARGE,
        CHARGE_RECOVER,
        HURT,
        TRANSITIONING,
        PHASE2_IDLE,
        PHASE2_CHARGE_PREPARE,
        PHASE2_CHARGE,
        PHASE2_BITE,
        ENRAGED,
        DYING
    }

    public enum EyeVisualPhase {
        PHASE_1,
        TRANSITIONING_P1,
        PHASE_2
    }

    public static final int TRANSITION_DURATION_TICKS = 60;
    public static final int TRANSITION_MESH_SWAP_TICK = 40;

    private static final EntityDataAccessor<Integer> DATA_ANIM_STATE =
            SynchedEntityData.defineId(EyeOfCthulhuEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> DATA_VISUAL_PHASE =
            SynchedEntityData.defineId(EyeOfCthulhuEntity.class, EntityDataSerializers.INT);

    private boolean isTransitioning = false;
    private int transitionTicks = 0;

    public EyeOfCthulhuEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level, "eye_of_cthulhu", 30_000L, 70_000L, 12, 80, BossEvent.BossBarColor.RED);
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ANIM_STATE, EyeAnimState.IDLE.ordinal());
        builder.define(DATA_VISUAL_PHASE, EyeVisualPhase.PHASE_1.ordinal());
    }

    public EyeAnimState getAnimState() {
        int ordinal = this.entityData.get(DATA_ANIM_STATE);
        EyeAnimState[] states = EyeAnimState.values();
        return (ordinal >= 0 && ordinal < states.length) ? states[ordinal] : EyeAnimState.IDLE;
    }

    public void setAnimState(EyeAnimState state) {
        this.entityData.set(DATA_ANIM_STATE, state.ordinal());
    }

    public EyeVisualPhase getVisualPhase() {
        int ordinal = this.entityData.get(DATA_VISUAL_PHASE);
        EyeVisualPhase[] phases = EyeVisualPhase.values();
        return (ordinal >= 0 && ordinal < phases.length) ? phases[ordinal] : EyeVisualPhase.PHASE_1;
    }

    public void setVisualPhase(EyeVisualPhase phase) {
        this.entityData.set(DATA_VISUAL_PHASE, phase.ordinal());
    }

    public boolean isRenderPhase2() {
        return getVisualPhase() == EyeVisualPhase.PHASE_2;
    }

    public boolean isTransitioning() {
        return isTransitioning;
    }

    public int getTransitionTicks() {
        return transitionTicks;
    }

    public void startPhaseTransition() {
        this.isTransitioning = true;
        this.transitionTicks = TRANSITION_DURATION_TICKS;
        setVisualPhase(EyeVisualPhase.TRANSITIONING_P1);
        setAnimState(EyeAnimState.TRANSITIONING);
    }

    public void tickTransition() {
        if (!isTransitioning) return;
        transitionTicks--;
        int elapsed = TRANSITION_DURATION_TICKS - transitionTicks;
        if (elapsed >= TRANSITION_MESH_SWAP_TICK && getVisualPhase() != EyeVisualPhase.PHASE_2) {
            setVisualPhase(EyeVisualPhase.PHASE_2);
        }
        if (transitionTicks <= 0) {
            isTransitioning = false;
            setVisualPhase(EyeVisualPhase.PHASE_2);
            setAnimState(EyeAnimState.PHASE2_IDLE);
        }
    }

    public void normalizeVisualPhase() {
        if (!isTransitioning) {
            if (getCurrentPhase().phaseNumber() >= 2) {
                setVisualPhase(EyeVisualPhase.PHASE_2);
            } else {
                setVisualPhase(EyeVisualPhase.PHASE_1);
            }
        }
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
    protected void onPhaseTransition(BossPhase newPhase) {
        super.onPhaseTransition(newPhase);

        if (newPhase.phaseNumber() >= 2 && (this.level() == null || !this.level().isClientSide())) {
            startPhaseTransition();

            if (this.level() != null) {
                this.level().playSound(
                        null, this.getX(), this.getY(), this.getZ(),
                        ModSoundEvents.BOSS_ROAR.get(), SoundSource.HOSTILE,
                        3.0f, 0.8f
                );
            }
        }
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();

        // Handle server-authoritative Phase 2 transformation
        if (isTransitioning) {
            tickTransition();

            // Convulsive shaking
            this.setDeltaMovement(
                    (mobRandom.nextDouble() - 0.5) * 0.25,
                    (mobRandom.nextDouble() - 0.5) * 0.25,
                    (mobRandom.nextDouble() - 0.5) * 0.25
            );

            if (this.level() instanceof ServerLevel serverLevel) {
                if (transitionTicks % 8 == 0) {
                    serverLevel.sendParticles(
                            ParticleTypes.CRIT,
                            this.getX(), this.getY() + 1.0, this.getZ(),
                            20, 0.8, 0.8, 0.8, 0.15
                    );
                    serverLevel.sendParticles(
                            ParticleTypes.DAMAGE_INDICATOR,
                            this.getX(), this.getY() + 1.0, this.getZ(),
                            12, 0.6, 0.6, 0.6, 0.1
                    );
                    serverLevel.playSound(
                            null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.SLIME_BLOCK_BREAK, SoundSource.HOSTILE,
                            1.5f, 0.7f
                    );
                }

                if (!isTransitioning) {
                    serverLevel.sendParticles(
                            ParticleTypes.EXPLOSION,
                            this.getX(), this.getY() + 1.0, this.getZ(),
                            3, 0.5, 0.5, 0.5, 0.0
                    );
                }
            }
            return;
        }

        if (this.level() == null || !this.level().isClientSide()) {
            normalizeVisualPhase();
        }

        // Daytime departure (Terraria rule: despawns if night ends)
        long dayTime = this.level().getDayTime() % 24000;
        if (dayTime < 13000 || dayTime > 23000) {
            setAnimState(EyeAnimState.ENRAGED);
            this.setDeltaMovement(0, 1.2, 0);
            if (this.getY() > 300) {
                this.discard();
            }
        }
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        if (this.position() != null) {
            super.addAdditionalSaveData(tag);
        }
        tag.putString("VisualPhase", getVisualPhase().name());
        tag.putBoolean("IsTransitioning", this.isTransitioning);
        tag.putInt("TransitionTicks", this.transitionTicks);
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        if (this.position() != null) {
            super.readAdditionalSaveData(tag);
        }
        if (tag.contains("VisualPhase")) {
            try {
                setVisualPhase(EyeVisualPhase.valueOf(tag.getString("VisualPhase")));
            } catch (IllegalArgumentException e) {
                normalizeVisualPhase();
            }
        } else {
            normalizeVisualPhase();
        }
        if (tag.contains("IsTransitioning")) {
            this.isTransitioning = tag.getBoolean("IsTransitioning");
            this.transitionTicks = tag.getInt("TransitionTicks");
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        setAnimState(EyeAnimState.DYING);
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
            if (eye.isTransitioning()) {
                return;
            }

            LivingEntity target = eye.getTarget();
            if (target == null) return;

            eye.getLookControl().setLookAt(target, 40.0f, 40.0f);
            boolean isPhase2 = eye.getCurrentPhase().phaseNumber() >= 2;
            attackTimer--;

            if (isCharging) {
                // High speed dash
                eye.setDeltaMovement(chargeVector);

                double distSqr = eye.distanceToSqr(target);
                if (distSqr < 4.0) {
                    eye.doHurtTarget(target);
                    if (isPhase2) {
                        eye.setAnimState(EyeAnimState.PHASE2_BITE);
                    }
                } else {
                    eye.setAnimState(isPhase2 ? EyeAnimState.PHASE2_CHARGE : EyeAnimState.CHARGE);
                }

                if (attackTimer <= 0) {
                    isCharging = false;
                    chargeCount++;
                    eye.setAnimState(EyeAnimState.CHARGE_RECOVER);
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

                // Prepare charge animation cue 12 ticks before launch
                if (attackTimer <= 12 && attackTimer > 0) {
                    eye.setAnimState(isPhase2 ? EyeAnimState.PHASE2_CHARGE_PREPARE : EyeAnimState.CHARGE_PREPARE);
                } else if (attackTimer > 12) {
                    eye.setAnimState(isPhase2 ? EyeAnimState.PHASE2_IDLE : EyeAnimState.HOVER);
                }

                // Phase 1: Spawn Servants of Cthulhu periodically
                if (!isPhase2 && attackTimer == 40 && eye.level() instanceof ServerLevel serverLevel) {
                    eye.setAnimState(EyeAnimState.SUMMON_SERVANT);
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
                    eye.setAnimState(isPhase2 ? EyeAnimState.PHASE2_CHARGE : EyeAnimState.CHARGE);

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
