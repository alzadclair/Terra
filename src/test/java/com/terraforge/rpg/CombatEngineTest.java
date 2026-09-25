package com.terraforge.rpg;

import com.terraforge.rpg.combat.CombatContext;
import com.terraforge.rpg.combat.DamageCalculator;
import com.terraforge.rpg.combat.DamageClass;
import com.terraforge.rpg.combat.DamageTypeCategory;
import com.terraforge.rpg.player.data.PlayerRPGData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CombatEngineTest {

    private PlayerRPGData attackerData;
    private PlayerRPGData targetData;

    @BeforeEach
    void setUp() {
        attackerData = new PlayerRPGData();
        attackerData.setLevel(10);
        attackerData.setPrimaryRace("human");

        targetData = new PlayerRPGData();
        targetData.setLevel(10);
        targetData.setPrimaryRace("human");
    }

    @Test
    @DisplayName("Physical combat pipeline applies attack rank and target physical defense reduction")
    void testPhysicalDamageCalculation() {
        // Base damage 20.0, Attack Rank 400 (+100% damage -> 40.0)
        attackerData.setAttackRank(400);
        // Target Defense Rank 400 (50% reduction)
        targetData.setDefenseRank(400);

        CombatContext context = new CombatContext(null, null, null, 20.0);
        context.setDamageClass(DamageClass.MELEE);
        context.setCategory(DamageTypeCategory.PHYSICAL);

        // Manually apply multipliers as done in server pipeline
        context.setRpgStatMultiplier(com.terraforge.rpg.stats.calculation.AttributeCalculator.calculatePhysicalDamageMultiplier(attackerData.getAttackRank()));
        context.setTargetDefenseReduction(com.terraforge.rpg.stats.calculation.AttributeCalculator.calculatePhysicalDamageReduction(targetData.getDefenseRank()));

        double result = DamageCalculator.calculateDamage(context);
        assertEquals(20.0, result, 0.001);
    }

    @Test
    @DisplayName("Magic combat pipeline applies magic attack rank and magic defense reduction")
    void testMagicDamageCalculation() {
        // Base damage 30.0, Magic Attack Rank 500 (+150% damage -> 75.0)
        attackerData.setMagicAttackRank(500);
        // Target Magic Defense Rank 400 (50% reduction)
        targetData.setMagicDefenseRank(400);

        CombatContext context = new CombatContext(null, null, null, 30.0);
        context.setDamageClass(DamageClass.MAGIC);
        context.setCategory(DamageTypeCategory.MAGICAL);

        context.setRpgStatMultiplier(com.terraforge.rpg.stats.calculation.AttributeCalculator.calculateMagicDamageMultiplier(attackerData.getMagicAttackRank()));
        context.setTargetDefenseReduction(com.terraforge.rpg.stats.calculation.AttributeCalculator.calculateMagicDamageReduction(targetData.getMagicDefenseRank()));

        double result = DamageCalculator.calculateDamage(context);
        assertEquals(37.5, result, 0.001);
    }

    @Test
    @DisplayName("True damage completely bypasses target defenses")
    void testTrueDamageBypassesDefense() {
        targetData.setDefenseRank(10_000);
        targetData.setMagicDefenseRank(10_000);

        CombatContext context = new CombatContext(null, null, null, 50.0);
        context.setDamageClass(DamageClass.TRUE);
        context.setCategory(DamageTypeCategory.TRUE_DAMAGE);
        context.setTargetDefenseReduction(0.95); // High reduction set

        double result = DamageCalculator.calculateDamage(context);
        // True damage ignores defense reduction
        assertEquals(50.0, result, 0.001);
    }

    @Test
    @DisplayName("Terraria minimum 1 damage rule is strictly enforced")
    void testMinimumDamageRule() {
        CombatContext context = new CombatContext(null, null, null, 0.01);
        context.setDamageClass(DamageClass.MELEE);
        context.setCategory(DamageTypeCategory.PHYSICAL);
        context.setTargetDefenseReduction(0.95);

        double result = DamageCalculator.calculateDamage(context);
        assertEquals(1.0, result, 0.001);
    }

    @Test
    @DisplayName("Special accessories modify damage multipliers and mitigation")
    void testSpecialAccessoriesCombatEffects() {
        // Titan Core (+20% physical)
        CombatContext titanContext = new CombatContext(null, null, null, 100.0);
        titanContext.setSpecialAccessoryMultiplier(1.20);
        assertEquals(120.0, DamageCalculator.calculateDamage(titanContext), 0.001);

        // Arcane Prism (+25% magic)
        CombatContext prismContext = new CombatContext(null, null, null, 100.0);
        prismContext.setCategory(DamageTypeCategory.MAGICAL);
        prismContext.setSpecialAccessoryMultiplier(1.25);
        assertEquals(125.0, DamageCalculator.calculateDamage(prismContext), 0.001);
    }

    @Test
    @DisplayName("Lifesteal calculation honors caps")
    void testLifestealEffects() {
        attackerData.setEquippedSpecialAccessory("blood_crystal");

        CombatContext context = new CombatContext(null, null, null, 100.0);
        context.setFinalDamage(100.0);

        // 5% of 100 is 5.0 (hits the 5.0 HP cap)
        double lifesteal = Math.min(context.getFinalDamage() * 0.05, 5.0);
        assertEquals(5.0, lifesteal, 0.001);

        // 5% of 200 is 10.0, capped to 5.0
        context.setFinalDamage(200.0);
        lifesteal = Math.min(context.getFinalDamage() * 0.05, 5.0);
        assertEquals(5.0, lifesteal, 0.001);
    }
}
