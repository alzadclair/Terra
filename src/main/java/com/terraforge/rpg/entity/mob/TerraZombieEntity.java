package com.terraforge.rpg.entity.mob;

import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.experience.ThreatRating;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Terraria Zombie monster with Fighter AI.
 */
public class TerraZombieEntity extends TerraBaseMonster {

    public TerraZombieEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level, ThreatRating.COMMON, 60L, 150L, 6, DamageClass.MELEE);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 45.0)
                .add(Attributes.MOVEMENT_SPEED, 0.23)
                .add(Attributes.ATTACK_DAMAGE, 14.0)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.15, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        if (!this.level().isClientSide()) {
            // Drop Rotten Flesh
            int fleshCount = 1 + mobRandom.nextInt(2);
            ItemEntity flesh = new ItemEntity(this.level(), this.getX(), this.getY() + 0.5, this.getZ(),
                    new ItemStack(Items.ROTTEN_FLESH, fleshCount));
            this.level().addFreshEntity(flesh);
        }
    }
}
