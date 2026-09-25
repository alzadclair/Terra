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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/**
 * Duke Fishron - Mutant aquatic terror boss (Terraria 1.4.5.8 canonical).
 * Features 5 explosive dashes, Detonating Bubbles, Sharknados / Cthulhunados,
 * and high-speed ocean enrage dynamics.
 */
public class DukeFishronEntity extends TerraBaseBoss {

    public static final double BASE_HEALTH = 60000.0;

    public DukeFishronEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level, "duke_fishron", 200_000L, 500_000L, 50, 128, BossEvent.BossBarColor.BLUE);
        this.moveControl = new FlyingMoveControl(this, 25, true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HEALTH)
                .add(Attributes.FLYING_SPEED, 0.55)
                .add(Attributes.MOVEMENT_SPEED, 0.50)
                .add(Attributes.ATTACK_DAMAGE, 90.0)
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
        this.goalSelector.addGoal(1, new DukeFishronCombatGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public int getTerrariaDefense() {
        if (isEnraged()) return 100;
        // Phase 1: 50 defense. Phase 2 (<= 50% HP): 40 defense.
        return getCurrentPhase().phaseNumber() >= 2 ? 40 : 50;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();

        // Phase 2 transition at <= 50% HP
        if (getCurrentPhase() == BossPhase.PHASE_1 && (getHealth() / getMaxHealth()) <= 0.50) {
            setPhase(BossPhase.PHASE_2);
        }

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SPLASH, getX(), getY() + 0.5, getZ(), 6, 0.5, 0.5, 0.5, 0.1);
            if (getCurrentPhase() == BossPhase.PHASE_2) {
                serverLevel.sendParticles(ParticleTypes.GLOW, getX(), getY() + 1.2, getZ(), 2, 0.2, 0.2, 0.2, 0.02);
            }
        }
    }

    @Override
    protected void onPhaseTransition(BossPhase newPhase) {
        super.onPhaseTransition(newPhase);
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, 2.0f, 1.2f);
            serverLevel.sendParticles(ParticleTypes.NAUTILUS, getX(), getY() + 1.0, getZ(), 25, 1.0, 1.0, 1.0, 0.2);
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        if (!level().isClientSide() && level() instanceof ServerLevel serverLevel) {
            WorldProgressionData progression = WorldProgressionData.get(serverLevel.getServer());
            progression.markBossDefeated("duke_fishron");

            // Drops: Tsunami bow
            ItemEntity bowDrop = new ItemEntity(serverLevel, getX(), getY(), getZ(),
                    new ItemStack(ModItems.TSUNAMI.get(), 1));
            serverLevel.addFreshEntity(bowDrop);

            serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                    Component.literal("Duke Fishron has been defeated!").withStyle(net.minecraft.ChatFormatting.GREEN, net.minecraft.ChatFormatting.BOLD),
                    false
            );
        }
    }

    static class DukeFishronCombatGoal extends Goal {
        private final DukeFishronEntity fishron;
        private int timer = 0;
        private int chargeCount = 0;
        private boolean charging = false;
        private Vec3 chargeDir = Vec3.ZERO;

        public DukeFishronCombatGoal(DukeFishronEntity fishron) {
            this.fishron = fishron;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return fishron.getTarget() != null && fishron.getTarget().isAlive();
        }

        @Override
        public void tick() {
            LivingEntity target = fishron.getTarget();
            if (target == null) return;

            fishron.getLookControl().setLookAt(target, 30.0f, 30.0f);
            timer++;

            boolean isPhase2 = fishron.getCurrentPhase().phaseNumber() >= 2;
            int maxCharges = isPhase2 ? 3 : 5;

            if (charging) {
                fishron.setDeltaMovement(chargeDir);
                if (fishron.distanceToSqr(target) < 12.0) {
                    target.hurt(fishron.damageSources().mobAttack(fishron), isPhase2 ? 100.0f : 80.0f);
                }

                if (timer >= 18) {
                    charging = false;
                    timer = 0;
                    chargeCount++;
                }
                return;
            }

            if (chargeCount < maxCharges) {
                // Perform fast charge
                charging = true;
                timer = 0;
                double speed = isPhase2 ? 1.7 : 1.3;
                chargeDir = target.getEyePosition().subtract(fishron.getEyePosition()).normalize().scale(speed);
                fishron.setDeltaMovement(chargeDir);

                if (fishron.level() instanceof ServerLevel serverLevel) {
                    serverLevel.playSound(null, fishron.getX(), fishron.getY(), fishron.getZ(),
                            SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.HOSTILE, 1.5f, 1.2f);
                }
            } else {
                // Reposition and spawn Sharknado / Bubbles
                double angle = fishron.tickCount * 0.05;
                fishron.getMoveControl().setWantedPosition(target.getX() + Math.cos(angle) * 12.0, target.getY() + 6.0, target.getZ() + Math.sin(angle) * 12.0, 1.1);

                if (timer >= 60) {
                    timer = 0;
                    chargeCount = 0;
                    spawnSharknado(target, isPhase2);
                }
            }
        }

        private void spawnSharknado(LivingEntity target, boolean isCthulhunado) {
            if (!(fishron.level() instanceof ServerLevel serverLevel)) return;

            // Spawns water cyclone vortex particles and damages nearby players
            Vec3 pos = target.position();
            for (int y = 0; y < 8; y++) {
                double radius = 1.0 + (y * 0.4);
                for (int i = 0; i < 6; i++) {
                    double angle = (Math.PI * 2.0 / 6.0) * i + (y * 0.3);
                    serverLevel.sendParticles(ParticleTypes.SPLASH,
                            pos.x + Math.cos(angle) * radius,
                            pos.y + y,
                            pos.z + Math.sin(angle) * radius,
                            2, 0.1, 0.1, 0.1, 0.05);
                }
            }

            serverLevel.playSound(null, pos.x, pos.y, pos.z, SoundEvents.GENERIC_SPLASH, SoundSource.HOSTILE, 2.0f, 0.8f);

            AABB tornadoBox = new AABB(pos.x - 3.0, pos.y, pos.z - 3.0, pos.x + 3.0, pos.y + 8.0, pos.z + 3.0);
            List<Player> hit = serverLevel.getEntitiesOfClass(Player.class, tornadoBox);
            for (Player p : hit) {
                p.hurt(fishron.damageSources().mobAttack(fishron), isCthulhunado ? 65.0f : 45.0f);
            }
        }
    }
}
