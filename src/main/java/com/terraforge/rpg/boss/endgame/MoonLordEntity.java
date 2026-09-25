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
 * Moon Lord - The ultimate cosmic endgame boss of Terraria 1.4.5.8.
 * Features 145,000 HP, Phantasmal Deathray from the forehead eye, Phantasmal Spheres & Bolts,
 * orbiting True Eyes of Cthulhu in Phase 2, and legendary endgame loot.
 */
public class MoonLordEntity extends TerraBaseBoss {

    public static final double BASE_HEALTH = 145000.0;
    private int deathrayTimer = 0;
    private int attackCycleTimer = 0;

    public MoonLordEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level, "moon_lord", 500_000L, 2_000_000L, 50, 160, BossEvent.BossBarColor.PURPLE);
        this.moveControl = new FlyingMoveControl(this, 30, true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HEALTH)
                .add(Attributes.FLYING_SPEED, 0.40)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, 100.0)
                .add(Attributes.FOLLOW_RANGE, 160.0);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanFloat(true);
        return nav;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MoonLordCombatGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public int getTerrariaDefense() {
        // Phase 1: 50 defense. Phase 2 (Core exposed): 70 defense.
        return getCurrentPhase().phaseNumber() >= 2 ? 70 : 50;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();

        // Phase 2 transition at <= 50% HP (Core exposed, True Eyes detached)
        if (getCurrentPhase() == BossPhase.PHASE_1 && (getHealth() / getMaxHealth()) <= 0.50) {
            setPhase(BossPhase.PHASE_2);
        }

        deathrayTimer++;
        if (deathrayTimer >= 220 && level() instanceof ServerLevel serverLevel) {
            deathrayTimer = 0;
            firePhantasmalDeathray(serverLevel);
        }

        attackCycleTimer++;
        if (attackCycleTimer % 40 == 0 && level() instanceof ServerLevel serverLevel) {
            firePhantasmalBolts(serverLevel);
        }

        if (level() instanceof ServerLevel serverLevel && tickCount % 5 == 0) {
            serverLevel.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 4.0, getZ(), 4, 1.0, 1.0, 1.0, 0.05);
            serverLevel.sendParticles(ParticleTypes.PORTAL, getX(), getY() + 2.0, getZ(), 6, 1.5, 1.5, 1.5, 0.1);
        }
    }

    private void firePhantasmalDeathray(ServerLevel serverLevel) {
        Player target = level().getNearestPlayer(this, 128.0);
        if (target == null) return;

        Vec3 eyePos = getEyePosition().add(0, 3.0, 0); // Forehead eye
        Vec3 dir = target.getEyePosition().subtract(eyePos).normalize();

        serverLevel.playSound(null, getX(), getY(), getZ(), com.terraforge.rpg.registry.ModSoundEvents.DEATHRAY.get(), SoundSource.HOSTILE, 4.0f, 0.7f);

        // Sweeping colossal deathray beam
        for (int i = 1; i <= 30; i++) {
            Vec3 beamPoint = eyePos.add(dir.scale(i * 1.5));
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, beamPoint.x, beamPoint.y, beamPoint.z, 3, 0.3, 0.3, 0.3, 0.05);
            serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, beamPoint.x, beamPoint.y, beamPoint.z, 1, 0, 0, 0, 0);
        }

        AABB rayBox = new AABB(eyePos, eyePos.add(dir.scale(45.0))).inflate(3.0);
        List<Player> hitPlayers = serverLevel.getEntitiesOfClass(Player.class, rayBox);
        for (Player p : hitPlayers) {
            p.hurt(damageSources().mobAttack(this), 150.0f);
        }
    }

    private void firePhantasmalBolts(ServerLevel serverLevel) {
        Player target = level().getNearestPlayer(this, 96.0);
        if (target == null) return;

        Vec3 eyePos = getEyePosition();
        for (int i = -1; i <= 1; i += 2) {
            Vec3 handPos = eyePos.add(i * 4.0, 0, 0);
            Vec3 dir = target.getEyePosition().subtract(handPos).normalize();

            TerraProjectileEntity bolt = new TerraProjectileEntity(ModEntities.TERRA_PROJECTILE.get(), serverLevel);
            bolt.setPos(handPos.x, handPos.y, handPos.z);
            bolt.setOwner(this);
            bolt.setDamageClass(DamageClass.MAGIC);
            bolt.setDamage(40.0);
            bolt.setProjectileGravity(0.0);
            bolt.shoot(dir.x, dir.y, dir.z, 2.0f, 0.5f);
            serverLevel.addFreshEntity(bolt);
        }

        serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.HOSTILE, 1.2f, 1.8f);
    }

    @Override
    protected void onPhaseTransition(BossPhase newPhase) {
        super.onPhaseTransition(newPhase);
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, getX(), getY(), getZ(), com.terraforge.rpg.registry.ModSoundEvents.BOSS_ROAR.get(), SoundSource.HOSTILE, 3.5f, 0.65f);
            serverLevel.sendParticles(ParticleTypes.FLASH, getX(), getY() + 3.0, getZ(), 20, 1.0, 1.0, 1.0, 0.1);
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        if (!level().isClientSide() && level() instanceof ServerLevel serverLevel) {
            WorldProgressionData progression = WorldProgressionData.get(serverLevel.getServer());
            progression.markBossDefeated("moon_lord");

            // Drops: Meowmere + Luminite Ore (70 - 90) + Portal Gun
            ItemEntity meowDrop = new ItemEntity(serverLevel, getX(), getY(), getZ(),
                    new ItemStack(ModItems.MEOWMERE.get(), 1));
            serverLevel.addFreshEntity(meowDrop);

            int luminiteCount = 70 + serverLevel.random.nextInt(21);
            ItemEntity luminiteDrop = new ItemEntity(serverLevel, getX(), getY(), getZ(),
                    new ItemStack(ModItems.LUMINITE_ORE_ITEM.get(), Math.min(luminiteCount, 64)));
            serverLevel.addFreshEntity(luminiteDrop);

            ItemEntity portalGunDrop = new ItemEntity(serverLevel, getX(), getY(), getZ(),
                    new ItemStack(ModItems.PORTAL_GUN.get(), 1));
            serverLevel.addFreshEntity(portalGunDrop);

            serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                    Component.literal("The celestial threat has been vanquished!").withStyle(net.minecraft.ChatFormatting.GOLD, net.minecraft.ChatFormatting.BOLD),
                    false
            );

            serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                    Component.literal("Moon Lord has been defeated! TerraForge RPG world has reached the pinnacle of triumph!").withStyle(net.minecraft.ChatFormatting.AQUA, net.minecraft.ChatFormatting.BOLD),
                    false
            );
        }
    }

    static class MoonLordCombatGoal extends Goal {
        private final MoonLordEntity moonLord;

        public MoonLordCombatGoal(MoonLordEntity moonLord) {
            this.moonLord = moonLord;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return moonLord.getTarget() != null && moonLord.getTarget().isAlive();
        }

        @Override
        public void tick() {
            LivingEntity target = moonLord.getTarget();
            if (target == null) return;

            moonLord.getLookControl().setLookAt(target, 20.0f, 20.0f);

            // Follows player from towering height
            double targetX = target.getX();
            double targetY = target.getY() + 10.0;
            double targetZ = target.getZ();
            moonLord.getMoveControl().setWantedPosition(targetX, targetY, targetZ, 0.95);
        }
    }
}
