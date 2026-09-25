package com.terraforge.rpg.entity.npc;

import com.terraforge.rpg.entity.npc.housing.HousingService;
import com.terraforge.rpg.world.progression.WorldProgressionData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Base class for all friendly Terraria Town NPCs.
 * Canonical Terraria 1.4.5.8: 250 Base HP, 15 Defense (+12 in Hardmode).
 * Defends town against monsters, returns home at night, and provides services to players.
 */
public abstract class TerraBaseTownNPC extends PathfinderMob {

    public static final double BASE_HEALTH = 250.0;
    public static final int BASE_DEFENSE = 15;

    private final String npcId;
    private int attackCooldown = 30;

    protected TerraBaseTownNPC(EntityType<? extends PathfinderMob> entityType, Level level, String npcId) {
        super(entityType, level);
        this.npcId = npcId;
    }

    public String getNpcId() {
        return npcId;
    }

    public static AttributeSupplier.Builder createTownNpcAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HEALTH)
                .add(Attributes.ARMOR, BASE_DEFENSE)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false,
                entity -> entity instanceof Enemy && !(entity instanceof Player)));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();

        if (this.level() instanceof ServerLevel serverLevel) {
            // Housing homecoming check
            HousingService.getInstance().tickNpcHomecoming(serverLevel, npcId, this);

            // Passive HP regeneration (1 HP every 20 ticks if full peace)
            if (this.tickCount % 20 == 0 && this.getHealth() < this.getMaxHealth()) {
                this.heal(1.0f);
            }

            // Self-defense against hostile monsters
            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive() && this.distanceToSqr(target) <= 144.0) {
                attackCooldown--;
                if (attackCooldown <= 0) {
                    attackCooldown = getAttackInterval();
                    performDefenseAttack(target, serverLevel);
                }
            }
        }
    }

    protected abstract int getAttackInterval();

    protected abstract void performDefenseAttack(LivingEntity target, ServerLevel level);

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        HousingService.getInstance().vacateHouse(npcId);

        if (!this.level().isClientSide() && this.level().getServer() != null) {
            this.level().getServer().getPlayerList().broadcastSystemMessage(
                    Component.literal(this.getName().getString() + " was slain...").withStyle(ChatFormatting.RED),
                    false
            );
        }
    }

    public int getEffectiveDefense() {
        int defense = BASE_DEFENSE;
        if (this.level() != null && this.getServer() != null) {
            if (WorldProgressionData.get(this.getServer()).isHardmode()) {
                defense += 12; // +12 Defense in Hardmode
            }
        }
        return defense;
    }
}
