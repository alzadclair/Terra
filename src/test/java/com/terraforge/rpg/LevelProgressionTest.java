package com.terraforge.rpg;

import com.terraforge.rpg.experience.ThreatRating;
import com.terraforge.rpg.level.LevelService;
import com.terraforge.rpg.player.data.PlayerRPGData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LevelProgressionTest {

    @Test
    @DisplayName("Fractional kill point accumulation converts to whole status points")
    void testFractionalPointConversion() {
        PlayerRPGData data = new PlayerRPGData();
        data.setAvailableStatusPoints(0);
        data.setKillPointProgress(0.0);

        // Accumulate 0.25 four times (e.g. 4 common mob kills)
        double currentProgress = 0.0;
        int wholePointsAwarded = 0;

        for (int i = 0; i < 4; i++) {
            currentProgress += ThreatRating.COMMON.getDefaultPointYield();
            int whole = (int) Math.floor(currentProgress);
            if (whole > 0) {
                wholePointsAwarded += whole;
                currentProgress -= whole;
            }
        }

        assertEquals(1, wholePointsAwarded);
        assertEquals(0.0, currentProgress, 0.001);
    }

    @Test
    @DisplayName("Effective XP multiplier for hybrid includes hybrid penalty")
    void testHybridXpMultiplier() {
        PlayerRPGData hybrid = new PlayerRPGData();
        hybrid.setPrimaryRace("human"); // 1.00
        hybrid.setSecondaryRace("demon"); // 1.50
        hybrid.setHybrid(true);

        // (1.00 + 1.50) / 2 + 0.10 penalty = 1.25 + 0.10 = 1.35
        double multiplier = LevelService.getEffectiveXpMultiplier(hybrid);
        assertEquals(1.35, multiplier, 0.001);
    }

    @Test
    @DisplayName("Level 1000 rules: Non-evolution stops gaining points; Evolution continues")
    void testLevel1000Rules() {
        PlayerRPGData goblin = new PlayerRPGData();
        goblin.setPrimaryRace("goblin");
        goblin.setLevel(1000);
        assertFalse(goblin.hasEvolution());

        PlayerRPGData human = new PlayerRPGData();
        human.setPrimaryRace("human");
        human.setLevel(1000);
        assertTrue(human.hasEvolution());

        PlayerRPGData hybridDemonHuman = new PlayerRPGData();
        hybridDemonHuman.setPrimaryRace("demon");
        hybridDemonHuman.setSecondaryRace("human");
        hybridDemonHuman.setHybrid(true);
        hybridDemonHuman.setLevel(1000);
        assertTrue(hybridDemonHuman.hasEvolution());
    }
}
