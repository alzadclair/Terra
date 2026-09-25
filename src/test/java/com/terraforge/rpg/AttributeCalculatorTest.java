package com.terraforge.rpg;

import com.terraforge.rpg.stats.calculation.AttributeCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AttributeCalculatorTest {

    @Test
    @DisplayName("Physical and Magic Attack damage multiplier formulas")
    void testAttackMultipliers() {
        assertEquals(1.0, AttributeCalculator.calculatePhysicalDamageMultiplier(0), 0.001);
        assertEquals(2.0, AttributeCalculator.calculatePhysicalDamageMultiplier(400), 0.001);

        assertEquals(1.0, AttributeCalculator.calculateMagicDamageMultiplier(0), 0.001);
        assertEquals(2.5, AttributeCalculator.calculateMagicDamageMultiplier(500), 0.001);
    }

    @Test
    @DisplayName("Physical and Magic Defense asymptotic reductions")
    void testDefenseReductions() {
        assertEquals(0.0, AttributeCalculator.calculatePhysicalDamageReduction(0), 0.001);
        assertEquals(0.5, AttributeCalculator.calculatePhysicalDamageReduction(400), 0.001);
        assertEquals(2.0 / 3.0, AttributeCalculator.calculatePhysicalDamageReduction(800), 0.001);

        assertEquals(0.0, AttributeCalculator.calculateMagicDamageReduction(0), 0.001);
        assertEquals(0.5, AttributeCalculator.calculateMagicDamageReduction(400), 0.001);
    }

    @Test
    @DisplayName("Critical damage and chance bonuses")
    void testCriticalCalculations() {
        assertEquals(1.5, AttributeCalculator.calculateCriticalMultiplier(0), 0.001);
        assertEquals(2.5, AttributeCalculator.calculateCriticalMultiplier(500), 0.001);

        assertEquals(0.0, AttributeCalculator.calculateCriticalChanceBonus(0), 0.001);
        assertEquals(75.0, AttributeCalculator.calculateCriticalChanceBonus(500), 0.001);
    }

    @Test
    @DisplayName("Movement speed safety clamp and curve")
    void testSpeedCurveAndClamp() {
        assertEquals(0.0, AttributeCalculator.calculateMovementSpeedBonus(0), 0.001);
        assertTrue(AttributeCalculator.calculateMovementSpeedBonus(500) > 0.0);
        // Even for extreme rank (e.g. Evolution rank 100,000), bonus cannot exceed 0.60
        assertEquals(0.60, AttributeCalculator.calculateMovementSpeedBonus(100_000), 0.001);
    }
}
