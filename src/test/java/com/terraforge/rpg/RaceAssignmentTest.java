package com.terraforge.rpg;

import com.terraforge.rpg.player.data.PlayerRPGData;
import com.terraforge.rpg.race.RaceAssignmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RaceAssignmentTest {

    @Test
    @DisplayName("Natural flight is granted to winged races and denied to terrestrial races")
    void testNaturalFlightDetection() {
        PlayerRPGData demon = new PlayerRPGData();
        demon.setPrimaryRace("demon");
        assertTrue(RaceAssignmentService.isNaturallyWinged(demon));

        PlayerRPGData human = new PlayerRPGData();
        human.setPrimaryRace("human");
        assertFalse(RaceAssignmentService.isNaturallyWinged(human));

        PlayerRPGData goblin = new PlayerRPGData();
        goblin.setPrimaryRace("goblin");
        assertFalse(RaceAssignmentService.isNaturallyWinged(goblin));
    }

    @Test
    @DisplayName("Hybrid race inherits natural flight if either parent race has it")
    void testHybridFlightInheritance() {
        PlayerRPGData hybridHumanDemon = new PlayerRPGData();
        hybridHumanDemon.setPrimaryRace("human");
        hybridHumanDemon.setSecondaryRace("demon");
        hybridHumanDemon.setHybrid(true);
        assertTrue(RaceAssignmentService.isNaturallyWinged(hybridHumanDemon));

        PlayerRPGData hybridAngelSlime = new PlayerRPGData();
        hybridAngelSlime.setPrimaryRace("slime");
        hybridAngelSlime.setSecondaryRace("angel");
        hybridAngelSlime.setHybrid(true);
        assertTrue(RaceAssignmentService.isNaturallyWinged(hybridAngelSlime));

        PlayerRPGData hybridSlimeGoblin = new PlayerRPGData();
        hybridSlimeGoblin.setPrimaryRace("slime");
        hybridSlimeGoblin.setSecondaryRace("goblin");
        hybridSlimeGoblin.setHybrid(true);
        assertFalse(RaceAssignmentService.isNaturallyWinged(hybridSlimeGoblin));
    }
}
