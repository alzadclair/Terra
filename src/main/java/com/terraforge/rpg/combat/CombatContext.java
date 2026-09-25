package com.terraforge.rpg.combat;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Immutable/state-tracking context for a single combat damage calculation.
 */
public final class CombatContext {

    private final @Nullable LivingEntity attacker;
    private final LivingEntity target;
    private final DamageSource damageSource;

    private DamageClass damageClass = DamageClass.GENERIC;
    private DamageTypeCategory category = DamageTypeCategory.PHYSICAL;

    private double baseDamage;
    private double rpgStatMultiplier = 1.0;
    private double equipmentMultiplier = 1.0;
    private double buffMultiplier = 1.0;
    private double racialMultiplier = 1.0;
    private double specialAccessoryMultiplier = 1.0;

    private boolean critical = false;
    private double critMultiplier = 1.5;

    private double targetDefenseReduction = 0.0;
    private double rawDamage = 0.0;
    private double finalDamage = 0.0;

    private double lifestealAmount = 0.0;

    public CombatContext(@Nullable LivingEntity attacker, LivingEntity target, DamageSource damageSource, double baseDamage) {
        this.attacker = attacker;
        this.target = target;
        this.damageSource = damageSource;
        this.baseDamage = Math.max(0.0, baseDamage);
    }

    public @Nullable LivingEntity getAttacker() {
        return attacker;
    }

    public LivingEntity getTarget() {
        return target;
    }

    public DamageSource getDamageSource() {
        return damageSource;
    }

    public DamageClass getDamageClass() {
        return damageClass;
    }

    public void setDamageClass(DamageClass damageClass) {
        this.damageClass = damageClass;
    }

    public DamageTypeCategory getCategory() {
        return category;
    }

    public void setCategory(DamageTypeCategory category) {
        this.category = category;
    }

    public double getBaseDamage() {
        return baseDamage;
    }

    public void setBaseDamage(double baseDamage) {
        this.baseDamage = Math.max(0.0, baseDamage);
    }

    public double getRpgStatMultiplier() {
        return rpgStatMultiplier;
    }

    public void setRpgStatMultiplier(double rpgStatMultiplier) {
        this.rpgStatMultiplier = Math.max(0.0, rpgStatMultiplier);
    }

    public double getEquipmentMultiplier() {
        return equipmentMultiplier;
    }

    public void setEquipmentMultiplier(double equipmentMultiplier) {
        this.equipmentMultiplier = Math.max(0.0, equipmentMultiplier);
    }

    public double getBuffMultiplier() {
        return buffMultiplier;
    }

    public void setBuffMultiplier(double buffMultiplier) {
        this.buffMultiplier = Math.max(0.0, buffMultiplier);
    }

    public double getRacialMultiplier() {
        return racialMultiplier;
    }

    public void setRacialMultiplier(double racialMultiplier) {
        this.racialMultiplier = Math.max(0.0, racialMultiplier);
    }

    public double getSpecialAccessoryMultiplier() {
        return specialAccessoryMultiplier;
    }

    public void setSpecialAccessoryMultiplier(double specialAccessoryMultiplier) {
        this.specialAccessoryMultiplier = Math.max(0.0, specialAccessoryMultiplier);
    }

    public boolean isCritical() {
        return critical;
    }

    public void setCritical(boolean critical) {
        this.critical = critical;
    }

    public double getCritMultiplier() {
        return critMultiplier;
    }

    public void setCritMultiplier(double critMultiplier) {
        this.critMultiplier = Math.max(1.0, critMultiplier);
    }

    public double getTargetDefenseReduction() {
        return targetDefenseReduction;
    }

    public void setTargetDefenseReduction(double targetDefenseReduction) {
        this.targetDefenseReduction = Math.clamp(targetDefenseReduction, 0.0, 0.95); // Max 95% reduction
    }

    public double getRawDamage() {
        return rawDamage;
    }

    public void setRawDamage(double rawDamage) {
        this.rawDamage = rawDamage;
    }

    public double getFinalDamage() {
        return finalDamage;
    }

    public void setFinalDamage(double finalDamage) {
        this.finalDamage = finalDamage;
    }

    public double getLifestealAmount() {
        return lifestealAmount;
    }

    public void setLifestealAmount(double lifestealAmount) {
        this.lifestealAmount = Math.max(0.0, lifestealAmount);
    }
}
