package com.terraforge.rpg;

import com.terraforge.rpg.player.data.PlayerRPGData;
import com.terraforge.rpg.stats.AttributeType;
import com.terraforge.rpg.stats.StatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatServiceTest {

    @Test
    @DisplayName("StatService accurately returns base rank values")
    void testRankRetrieval() {
        PlayerRPGData data = new PlayerRPGData();
        data.setDefenseRank(100);
        data.setAttackRank(250);
        data.setSpeedRank(45);

        assertEquals(100, StatService.getRank(data, "defense"));
        assertEquals(250, StatService.getRank(data, "attack"));
        assertEquals(45, StatService.getRank(data, "speed"));
        assertEquals(0, StatService.getRank(data, "unknown_stat"));
    }

    @Test
    @DisplayName("StatService returns correct effective caps")
    void testCaps() {
        PlayerRPGData data = new PlayerRPGData();
        assertEquals(AttributeType.DEFENSE.getBaseGlobalCap(), StatService.getEffectiveCap(data, "defense"));
        assertEquals(AttributeType.ATTACK.getBaseGlobalCap(), StatService.getEffectiveCap(data, "attack"));
        assertEquals(AttributeType.SPEED.getBaseGlobalCap(), StatService.getEffectiveCap(data, "speed"));
    }
}
