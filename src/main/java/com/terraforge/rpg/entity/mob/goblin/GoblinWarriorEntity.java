package com.terraforge.rpg.entity.mob.goblin;

import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.entity.mob.TerraBaseMonster;
import com.terraforge.rpg.experience.ThreatRating;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Goblin Warrior - heavily armored vanguard of the Goblin Army.
 * Canonical stats: 110 HP, 12 Defense, 25 Damage.
 */
public class GoblinWarriorEntity extends TerraBaseMonster {

    public static final double BASE_HEALTH = 110.0;
    public static final int BASE_DEFENSE = 12;
    public static final double BASE_DAMAGE = 25.0;

    public GoblinWarriorEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level, ThreatRating.ELITE, 50L, 150L, BASE_DEFENSE, DamageClass.MELEE);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, BASE_DAMAGE)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.1, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }
}
