package com.terraforge.rpg;

import com.terraforge.rpg.race.RaceDefinition;
import com.terraforge.rpg.race.RaceRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RaceRegistryTest {

    @Test
    @DisplayName("Exactly 30 canonical races must be registered")
    void testExactRaceCount() {
        assertEquals(30, RaceRegistry.getCount());
        assertEquals(30, RaceRegistry.getAllRaceIds().size());
    }

    @Test
    @DisplayName("Human, Goblin, Slime, and Demon retain canonical specs")
    void testSpecificRaceDefinitions() {
        Optional<RaceDefinition> human = RaceRegistry.get("human");
        assertTrue(human.isPresent());
        assertEquals(1.00, human.get().xpMultiplier());
        assertFalse(human.get().naturalFlight());
        assertEquals("evolution", human.get().abilityId());

        Optional<RaceDefinition> goblin = RaceRegistry.get("goblin");
        assertTrue(goblin.isPresent());
        assertEquals(0.80, goblin.get().xpMultiplier());
        assertFalse(goblin.get().naturalFlight());

        Optional<RaceDefinition> slime = RaceRegistry.get("slime");
        assertTrue(slime.isPresent());
        assertEquals(0.78, slime.get().xpMultiplier());

        Optional<RaceDefinition> demon = RaceRegistry.get("demon");
        assertTrue(demon.isPresent());
        assertEquals(1.50, demon.get().xpMultiplier());
        assertTrue(demon.get().naturalFlight());
    }

    @Test
    @DisplayName("Winged races have natural flight authorized")
    void testNaturalFlightRaces() {
        String[] flyers = {"demon", "angel", "draconic", "fairy", "harpy", "phoenix", "astral", "spectre", "aetherian"};
        for (String id : flyers) {
            Optional<RaceDefinition> race = RaceRegistry.get(id);
            assertTrue(race.isPresent(), "Race missing: " + id);
            assertTrue(race.get().naturalFlight(), "Expected natural flight for: " + id);
        }
    }
}
