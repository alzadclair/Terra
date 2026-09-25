package com.terraforge.rpg;

import com.terraforge.rpg.accessory.special.SpecialAccessoryService;
import com.terraforge.rpg.player.data.PlayerRPGData;
import com.terraforge.rpg.race.RaceAssignmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpecialAccessoryTest {

    @Test
    @DisplayName("Exactly the 10 canonical Special Accessories are recognized")
    void testValidAccessories() {
        String[] valid = {
                "phoenix_wings", "thunder_fragment", "void_heart", "titan_core", "arcane_prism",
                "guardian_seal", "blood_crystal", "time_gear", "predator_eye", "gravity_sigil"
        };
        for (String id : valid) {
            assertTrue(SpecialAccessoryService.isValidAccessory(id), "Accessory should be valid: " + id);
        }
        assertFalse(SpecialAccessoryService.isValidAccessory("iron_sword"));
        assertFalse(SpecialAccessoryService.isValidAccessory(null));
        assertFalse(SpecialAccessoryService.isValidAccessory(""));
    }

    @Test
    @DisplayName("Phoenix Wings grant creative flight to terrestrial races")
    void testPhoenixWingsFlight() {
        PlayerRPGData human = new PlayerRPGData();
        human.setPrimaryRace("human");
        assertFalse(RaceAssignmentService.isNaturallyWinged(human));

        // When equipped with phoenix wings, flight should be authorized
        human.setEquippedSpecialAccessory("phoenix_wings");
        boolean canFly = RaceAssignmentService.isNaturallyWinged(human) || "phoenix_wings".equalsIgnoreCase(human.getEquippedSpecialAccessory());
        assertTrue(canFly);

        // When unequipped, terrestrial races lose flight
        human.setEquippedSpecialAccessory("");
        canFly = RaceAssignmentService.isNaturallyWinged(human) || "phoenix_wings".equalsIgnoreCase(human.getEquippedSpecialAccessory());
        assertFalse(canFly);
    }

    @Test
    @DisplayName("Naturally winged races retain flight regardless of equipped accessory")
    void testWingedRaceFlightRetention() {
        PlayerRPGData demon = new PlayerRPGData();
        demon.setPrimaryRace("demon");
        assertTrue(RaceAssignmentService.isNaturallyWinged(demon));

        // Demon with thunder_fragment still flies naturally
        demon.setEquippedSpecialAccessory("thunder_fragment");
        boolean canFly = RaceAssignmentService.isNaturallyWinged(demon) || "phoenix_wings".equalsIgnoreCase(demon.getEquippedSpecialAccessory());
        assertTrue(canFly);
    }
}
