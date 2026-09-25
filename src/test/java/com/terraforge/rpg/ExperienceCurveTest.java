package com.terraforge.rpg;

import com.terraforge.rpg.experience.DefaultExperienceCurve;
import com.terraforge.rpg.experience.ExperienceCurve;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExperienceCurveTest {

    @Test
    @DisplayName("Level 1 baseline required XP calculation")
    void testLevelOneRequiredXp() {
        ExperienceCurve curve = new DefaultExperienceCurve(100.0, 1.35);
        double xp = curve.getRequiredXp(1, 1.0);
        assertEquals(100.0, xp, 0.001);
    }

    @Test
    @DisplayName("Race XP multiplier scales required XP proportionally")
    void testRaceMultiplierScaling() {
        ExperienceCurve curve = new DefaultExperienceCurve(100.0, 1.35);
        double humanXp = curve.getRequiredXp(5, 1.0);
        double goblinXp = curve.getRequiredXp(5, 0.80);
        double demonXp = curve.getRequiredXp(5, 1.50);

        assertTrue(goblinXp < humanXp);
        assertTrue(demonXp > humanXp);
        assertEquals(humanXp * 0.80, goblinXp, 0.001);
        assertEquals(humanXp * 1.50, demonXp, 0.001);
    }
}
