package com.terraforge.rpg.entity.mob.goblin;

import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.entity.mob.TerraBaseMonster;
import com.terraforge.rpg.experience.ThreatRating;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Goblin Thief - agile and aggressive scout of the Goblin Army.
 * Canonical stats: 80 HP, 8 Defense, 20 Damage.
 */
public class GoblinThiefEntity extends TerraBaseMonster {

    public static final double BASE_HEALTH = 80.0;
    public static final int BASE_DEFENSE = 8;
    public static final double BASE_DAMAGE = 20.0;

    public GoblinThiefEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level, ThreatRating.STRONG, 20L, 80L, BASE_DEFENSE, DamageClass.MELEE);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, BASE_DAMAGE)
                .add(Attributes.FOLLOW_RANGE, 36.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LeapAtTargetGoal(this, 0.4f));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.3, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }
}
