package com.terraforge.rpg;

import com.terraforge.rpg.player.data.PlayerRPGData;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerRPGDataTest {

    @Test
    @DisplayName("Player starts at level 1 with 5 status points and Human race by default")
    void testInitialState() {
        PlayerRPGData data = new PlayerRPGData();
        assertEquals(1, data.getLevel());
        assertEquals(5, data.getAvailableStatusPoints());
        assertEquals(5, data.getTotalStatusPointsAcquired());
        assertEquals(0, data.getStatusPointsSpent());
        assertEquals("human", data.getPrimaryRace());
        assertFalse(data.isHybrid());
        assertTrue(data.hasEvolution());
    }

    @Test
    @DisplayName("Human and Human-hybrid races correctly detect Evolution")
    void testEvolutionDetection() {
        PlayerRPGData human = new PlayerRPGData();
        human.setPrimaryRace("human");
        assertTrue(human.hasEvolution());

        PlayerRPGData goblin = new PlayerRPGData();
        goblin.setPrimaryRace("goblin");
        assertFalse(goblin.hasEvolution());

        PlayerRPGData hybridHuman = new PlayerRPGData();
        hybridHuman.setPrimaryRace("demon");
        hybridHuman.setSecondaryRace("human");
        hybridHuman.setHybrid(true);
        assertTrue(hybridHuman.hasEvolution());
    }

    @Test
    @DisplayName("Clamping prevents negative levels or status points")
    void testClampingProtection() {
        PlayerRPGData data = new PlayerRPGData();
        data.setLevel(-10);
        assertEquals(1, data.getLevel());

        data.setAvailableStatusPoints(-50);
        assertEquals(0, data.getAvailableStatusPoints());

        data.setDefenseRank(-5);
        assertEquals(0, data.getDefenseRank());

        data.setCurrentXp(-100.0);
        assertEquals(0.0, data.getCurrentXp());
    }

    @Test
    @DisplayName("NBT serialization and deserialization preserves integrity")
    void testNbtPersistence() {
        PlayerRPGData original = new PlayerRPGData();
        original.setLevel(25);
        original.setAvailableStatusPoints(120);
        original.setPrimaryRace("demon");
        original.setSecondaryRace("goblin");
        original.setHybrid(true);
        original.setDefenseRank(50);
        original.setAttackRank(150);
        original.setEquippedSpecialAccessory("phoenix_wings");
        original.setFlightAuthorized(true);

        CompoundTag tag = original.serializeNBT(null);

        PlayerRPGData restored = new PlayerRPGData();
        restored.deserializeNBT(null, tag);

        assertEquals(25, restored.getLevel());
        assertEquals(120, restored.getAvailableStatusPoints());
        assertEquals("demon", restored.getPrimaryRace());
        assertEquals("goblin", restored.getSecondaryRace());
        assertTrue(restored.isHybrid());
        assertEquals(50, restored.getDefenseRank());
        assertEquals(150, restored.getAttackRank());
        assertEquals("phoenix_wings", restored.getEquippedSpecialAccessory());
        assertTrue(restored.isFlightAuthorized());
    }
}
