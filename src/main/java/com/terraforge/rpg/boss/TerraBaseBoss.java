package com.terraforge.rpg.boss;

import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.entity.mob.TerraBaseMonster;
import com.terraforge.rpg.experience.ThreatRating;
import com.terraforge.rpg.level.LevelService;
import com.terraforge.rpg.world.progression.WorldProgressionData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Abstract base class for all Terraria 1.4.5.8 boss encounters.
 * Handles BossBar synchronization, multiplayer scaling, phase transitions, and despawning.
 */
public abstract class TerraBaseBoss extends TerraBaseMonster implements ITerrariaBoss {

    private static final EntityDataAccessor<Integer> DATA_PHASE_NUMBER =
            SynchedEntityData.defineId(TerraBaseBoss.class, EntityDataSerializers.INT);

    private final String bossId;
    private final int arenaRadius;
    private final ServerBossEvent bossEvent;

    private boolean enraged = false;
    private int ticksWithoutPlayers = 0;

    protected TerraBaseBoss(
            EntityType<? extends Monster> entityType,
            Level level,
            String bossId,
            long minCoins,
            long maxCoins,
            int defense,
            int arenaRadius,
            BossEvent.BossBarColor barColor
    ) {
        super(entityType, level, ThreatRating.BOSS, minCoins, maxCoins, defense, DamageClass.MELEE);
        this.bossId = bossId;
        this.arenaRadius = arenaRadius;
        this.bossEvent = new ServerBossEvent(
                Component.translatable("entity.terraforge_rpg." + bossId),
                barColor,
                BossEvent.BossBarOverlay.PROGRESS
        );
    }

    @Override
    public String getBossId() {
        return bossId;
    }

    @Override
    public Component getName() {
        if (hasCustomName()) {
            return getCustomName();
        }
        return Component.translatable("entity.terraforge_rpg." + bossId);
    }

    @Override
    public Component getDisplayName() {
        return getName();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PHASE_NUMBER, 1);
    }

    @Override
    public BossPhase getCurrentPhase() {
        int num = this.entityData.get(DATA_PHASE_NUMBER);
        return switch (num) {
            case 2 -> BossPhase.PHASE_2;
            case 3 -> BossPhase.ENRAGED;
            default -> BossPhase.PHASE_1;
        };
    }

    @Override
    public void setPhase(BossPhase phase) {
        this.entityData.set(DATA_PHASE_NUMBER, phase.phaseNumber());
        onPhaseTransition(phase);
    }

    @Override
    public boolean isEnraged() {
        return enraged;
    }

    public void setEnraged(boolean enraged) {
        this.enraged = enraged;
    }

    @Override
    public int getArenaRadius() {
        return arenaRadius;
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();

        // Update Boss Bar progress
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        // Check health threshold for phase transitions
        double healthRatio = (double) this.getHealth() / (double) this.getMaxHealth();
        if (healthRatio <= 0.5 && getCurrentPhase().phaseNumber() == 1) {
            setPhase(BossPhase.PHASE_2);
        }

        // Multiplayer player tracking for BossBar
        updateBossBarPlayers();

        // Despawn if no players in arena
        if (bossEvent.getPlayers().isEmpty()) {
            ticksWithoutPlayers++;
            if (ticksWithoutPlayers > 200) { // 10 seconds empty arena
                this.discard();
            }
        } else {
            ticksWithoutPlayers = 0;
        }
    }

    private void updateBossBarPlayers() {
        AABB arenaBox = this.getBoundingBox().inflate(arenaRadius);
        List<ServerPlayer> nearbyPlayers = this.level().getEntitiesOfClass(
                ServerPlayer.class, arenaBox, Player::isAlive);

        Set<ServerPlayer> currentTracked = new HashSet<>(this.bossEvent.getPlayers());

        for (ServerPlayer player : nearbyPlayers) {
            if (!currentTracked.contains(player)) {
                this.bossEvent.addPlayer(player);
            }
        }

        for (ServerPlayer tracked : currentTracked) {
            if (!nearbyPlayers.contains(tracked) || !tracked.isAlive()) {
                this.bossEvent.removePlayer(tracked);
            }
        }
    }

    /**
     * Hook called when the boss transitions into a new phase.
     */
    protected void onPhaseTransition(BossPhase newPhase) {
        if (this.level() != null && !this.level().isClientSide()) {
            this.level().playSound(
                    null, this.getX(), this.getY(), this.getZ(),
                    com.terraforge.rpg.registry.ModSoundEvents.BOSS_ROAR.get(), SoundSource.HOSTILE,
                    2.5f, 0.85f
            );

            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.EXPLOSION,
                        this.getX(), this.getY() + (this.getBbHeight() * 0.5), this.getZ(),
                        35, 1.2, 1.2, 1.2, 0.25
                );
            }
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        this.bossEvent.removeAllPlayers();

        if (!this.level().isClientSide() && this.getServer() != null) {
            WorldProgressionData worldData = WorldProgressionData.get(this.getServer());
            boolean firstKill = !worldData.isBossDefeated(bossId);
            if (firstKill) {
                worldData.markBossDefeated(bossId);
            }

            if (damageSource.getEntity() instanceof ServerPlayer killer) {
                LevelService.handleKillReward(killer, ThreatRating.BOSS, firstKill);
            }
        }
    }
}
