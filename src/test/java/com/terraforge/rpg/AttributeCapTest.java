package com.terraforge.rpg;

import com.terraforge.rpg.player.data.PlayerRPGData;
import com.terraforge.rpg.stats.AttributeCapRegistry;
import com.terraforge.rpg.stats.AttributeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AttributeCapTest {

    @Test
    @DisplayName("Human baseline caps match base global caps")
    void testHumanBaselineCaps() {
        PlayerRPGData human = new PlayerRPGData();
        human.setPrimaryRace("human");
        human.setLevel(50);

        assertEquals(800, AttributeCapRegistry.getEffectiveCap(human, AttributeType.DEFENSE));
        assertEquals(1000, AttributeCapRegistry.getEffectiveCap(human, AttributeType.ATTACK));
        assertEquals(500, AttributeCapRegistry.getEffectiveCap(human, AttributeType.SPEED));
    }

    @Test
    @DisplayName("Dwarf receives elevated Defense cap")
    void testDwarfDefenseCap() {
        PlayerRPGData dwarf = new PlayerRPGData();
        dwarf.setPrimaryRace("dwarf");
        dwarf.setLevel(100);

        int cap = AttributeCapRegistry.getEffectiveCap(dwarf, AttributeType.DEFENSE);
        assertEquals(1040, cap); // 800 * 1.30 = 1040
    }

    @Test
    @DisplayName("Evolution post-1000 removes all investment caps")
    void testEvolutionPost1000CapBypass() {
        PlayerRPGData human = new PlayerRPGData();
        human.setPrimaryRace("human");
        human.setLevel(1000);

        assertTrue(AttributeCapRegistry.isEvolutionUncapped(human));
        assertEquals(Integer.MAX_VALUE, AttributeCapRegistry.getEffectiveCap(human, AttributeType.ATTACK));
        assertTrue(AttributeCapRegistry.canInvest(human, AttributeType.ATTACK, 5000));
    }

    @Test
    @DisplayName("Non-evolution race at level 1000 remains capped")
    void testNonEvolutionLevel1000Capped() {
        PlayerRPGData titan = new PlayerRPGData();
        titan.setPrimaryRace("titan");
        titan.setLevel(1000);

        assertFalse(AttributeCapRegistry.isEvolutionUncapped(titan));
        assertNotEquals(Integer.MAX_VALUE, AttributeCapRegistry.getEffectiveCap(titan, AttributeType.ATTACK));
    }
}
