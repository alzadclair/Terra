package com.terraforge.rpg.entity.mob;

import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.experience.ThreatRating;
import com.terraforge.rpg.registry.ModItems;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Terraria Crimson biome hostile mob: Face Monster.
 * Canonical Terraria 1.4.5.8 Stats: 70 HP, 8 Defense, 25 Attack Damage.
 * Fast, terrifying crimson abomination that relentlessly pursues the player.
 */
public class FaceMonsterEntity extends TerraBaseMonster {

    public static final double BASE_HEALTH = 70.0;
    public static final int BASE_DEFENSE = 8;
    public static final double BASE_ATTACK_DAMAGE = 25.0;

    public FaceMonsterEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level, ThreatRating.STRONG, 70L, 200L, BASE_DEFENSE, DamageClass.MELEE);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .add(Attributes.ATTACK_DAMAGE, BASE_ATTACK_DAMAGE)
                .add(Attributes.ARMOR, BASE_DEFENSE)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.25, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        if (!this.level().isClientSide()) {
            int coins = 15 + mobRandom.nextInt(20);
            ItemEntity coinEntity = new ItemEntity(this.level(), this.getX(), this.getY() + 0.5, this.getZ(),
                    new ItemStack(ModItems.COPPER_COIN.get(), coins));
            this.level().addFreshEntity(coinEntity);
        }
    }
}
