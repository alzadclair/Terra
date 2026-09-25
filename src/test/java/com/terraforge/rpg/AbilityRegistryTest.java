package com.terraforge.rpg;

import com.terraforge.rpg.race.ability.AbilityRegistry;
import com.terraforge.rpg.race.ability.AbilityType;
import com.terraforge.rpg.race.ability.RaceAbility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AbilityRegistryTest {

    @Test
    @DisplayName("Exactly 30 canonical abilities are registered")
    void testExactAbilityCount() {
        assertEquals(30, AbilityRegistry.getCount());
    }

    @Test
    @DisplayName("Human Evolution is passive and uncapped")
    void testEvolutionAbility() {
        Optional<RaceAbility> ability = AbilityRegistry.get("evolution");
        assertTrue(ability.isPresent());
        assertEquals("human", ability.get().getOwnerRace());
        assertEquals(AbilityType.PASSIVE, ability.get().getType());
        assertEquals(0, ability.get().getCooldownTicks());
        assertEquals(0.0, ability.get().getManaCost());
    }

    @Test
    @DisplayName("Demon, Angel, Phoenix, and Undead abilities conform to specs")
    void testKeyRacialAbilities() {
        Optional<RaceAbility> demon = AbilityRegistry.get("infernal_domain");
        assertTrue(demon.isPresent());
        assertEquals(AbilityType.ACTIVE, demon.get().getType());
        assertEquals(800, demon.get().getCooldownTicks());

        Optional<RaceAbility> angel = AbilityRegistry.get("celestial_grace");
        assertTrue(angel.isPresent());
        assertEquals(AbilityType.ACTIVE, angel.get().getType());
        assertEquals(900, angel.get().getCooldownTicks());

        Optional<RaceAbility> phoenix = AbilityRegistry.get("rebirth");
        assertTrue(phoenix.isPresent());
        assertEquals(AbilityType.CONDITIONAL, phoenix.get().getType());

        Optional<RaceAbility> undead = AbilityRegistry.get("death_denial");
        assertTrue(undead.isPresent());
        assertEquals(AbilityType.CONDITIONAL, undead.get().getType());
    }
}
