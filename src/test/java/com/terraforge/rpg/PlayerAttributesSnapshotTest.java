package com.terraforge.rpg;

import com.terraforge.rpg.player.data.PlayerRPGData;
import com.terraforge.rpg.stats.AttributeType;
import com.terraforge.rpg.stats.PlayerAttributesSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerAttributesSnapshotTest {

    @Test
    @DisplayName("Snapshot correctly computes Base + Race + Invested = Final breakdown")
    void testSnapshotComputation() {
        PlayerRPGData data = new PlayerRPGData();
        data.setPrimaryRace("dwarf");
        data.setLevel(50);
        data.setDefenseRank(50);

        PlayerAttributesSnapshot snapshot = PlayerAttributesSnapshot.compute(data, AttributeType.DEFENSE);

        assertEquals(10.0, snapshot.baseValue());
        assertEquals(18.0, snapshot.raceBonus()); // Dwarf base defense is 18
        assertEquals(50, snapshot.investedRank());
        assertEquals(78.0, snapshot.finalValue(), 0.001); // 10 + 18 + 50 = 78
        assertEquals(1040, snapshot.effectiveCap()); // Dwarf cap multiplier 1.30 * 800 = 1040
        assertFalse(snapshot.uncapped());
    }

    @Test
    @DisplayName("Snapshot detects uncapped status for Evolution post-1000")
    void testSnapshotEvolutionUncapped() {
        PlayerRPGData human = new PlayerRPGData();
        human.setPrimaryRace("human");
        human.setLevel(1000);
        human.setAttackRank(1500);

        PlayerAttributesSnapshot snapshot = PlayerAttributesSnapshot.compute(human, AttributeType.ATTACK);

        assertTrue(snapshot.uncapped());
        assertEquals(Integer.MAX_VALUE, snapshot.effectiveCap());
    }
}
