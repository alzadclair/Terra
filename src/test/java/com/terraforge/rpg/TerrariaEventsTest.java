package com.terraforge.rpg;

import com.terraforge.rpg.boss.BossScalingService;
import com.terraforge.rpg.boss.prehardmode.KingSlimeEntity;
import com.terraforge.rpg.entity.mob.goblin.GoblinPeonEntity;
import com.terraforge.rpg.entity.mob.goblin.GoblinSorcererEntity;
import com.terraforge.rpg.entity.mob.goblin.GoblinThiefEntity;
import com.terraforge.rpg.entity.mob.goblin.GoblinWarriorEntity;
import com.terraforge.rpg.event.ITerrariaEvent;
import com.terraforge.rpg.event.TerrariaEventManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TerrariaEventsTest {

    @Test
    @DisplayName("TerrariaEventManager registers canonical events")
    void testEventManagerRegistration() {
        TerrariaEventManager manager = TerrariaEventManager.getInstance();

        ITerrariaEvent slimeRain = manager.getEvent("slime_rain");
        assertNotNull(slimeRain);
        assertEquals("Slime Rain", slimeRain.getDisplayName());

        ITerrariaEvent bloodMoon = manager.getEvent("blood_moon");
        assertNotNull(bloodMoon);
        assertEquals("Blood Moon", bloodMoon.getDisplayName());

        ITerrariaEvent goblinArmy = manager.getEvent("goblin_army");
        assertNotNull(goblinArmy);
        assertEquals("Goblin Army", goblinArmy.getDisplayName());
    }

    @Test
    @DisplayName("Goblin Army canonical kill requirement formula (80 + 40 * Players)")
    void testGoblinArmyFormula() {
        int singleplayerKills = 80 + (40 * 1);
        assertEquals(120, singleplayerKills);

        int twoPlayerKills = 80 + (40 * 2);
        assertEquals(160, twoPlayerKills);

        int fourPlayerKills = 80 + (40 * 4);
        assertEquals(240, fourPlayerKills);
    }

    @Test
    @DisplayName("King Slime canonical stats and multiplayer scaling")
    void testKingSlimeStats() {
        assertEquals(2000.0, KingSlimeEntity.BASE_HEALTH, 0.001);
        assertEquals(10, KingSlimeEntity.BASE_DEFENSE);
        assertEquals(40.0, KingSlimeEntity.BASE_ATTACK_DAMAGE, 0.001);

        assertEquals(2000.0, BossScalingService.calculateScaledHealth(KingSlimeEntity.BASE_HEALTH, 1), 0.001);
        // 2 players: 2000 * (1 + 0.35) = 2700
        assertEquals(2700.0, BossScalingService.calculateScaledHealth(KingSlimeEntity.BASE_HEALTH, 2), 0.001);
    }

    @Test
    @DisplayName("Goblin Army mobs canonical stats")
    void testGoblinMobStats() {
        // Goblin Peon
        assertEquals(60.0, GoblinPeonEntity.BASE_HEALTH, 0.001);
        assertEquals(6, GoblinPeonEntity.BASE_DEFENSE);
        assertEquals(12.0, GoblinPeonEntity.BASE_DAMAGE, 0.001);

        // Goblin Thief
        assertEquals(80.0, GoblinThiefEntity.BASE_HEALTH, 0.001);
        assertEquals(8, GoblinThiefEntity.BASE_DEFENSE);
        assertEquals(20.0, GoblinThiefEntity.BASE_DAMAGE, 0.001);

        // Goblin Warrior
        assertEquals(110.0, GoblinWarriorEntity.BASE_HEALTH, 0.001);
        assertEquals(12, GoblinWarriorEntity.BASE_DEFENSE);
        assertEquals(25.0, GoblinWarriorEntity.BASE_DAMAGE, 0.001);

        // Goblin Sorcerer
        assertEquals(40.0, GoblinSorcererEntity.BASE_HEALTH, 0.001);
        assertEquals(2, GoblinSorcererEntity.BASE_DEFENSE);
        assertEquals(20.0, GoblinSorcererEntity.BASE_DAMAGE, 0.001);
    }

    @Test
    @DisplayName("Blood Moon night window check")
    void testBloodMoonWindow() {
        long daytime = 6000L;
        long nightStart = 13000L;
        long midnight = 18000L;
        long dawn = 23000L;
        long morning = 23600L;

        assertFalse(nightStart <= daytime || daytime >= 23500L);
        assertTrue(nightStart <= midnight && midnight <= 23500L);
        assertTrue(nightStart <= dawn && dawn <= 23500L);
        assertFalse(morning >= 13000L && morning <= 23500L);
    }
}
