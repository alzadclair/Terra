package com.terraforge.rpg;

import com.terraforge.rpg.entity.npc.GuideEntity;
import com.terraforge.rpg.entity.npc.TerraBaseTownNPC;
import com.terraforge.rpg.entity.npc.housing.HousingService;
import com.terraforge.rpg.entity.npc.housing.HousingValidationResult;
import com.terraforge.rpg.entity.npc.housing.HousingValidator;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TownHousingTest {

    @Test
    @DisplayName("HousingValidator constants match Terraria 1.4.5.8 specifications")
    void testHousingValidatorConstants() {
        assertEquals(30, HousingValidator.MIN_VOLUME);
        assertEquals(750, HousingValidator.MAX_VOLUME);
        assertEquals(15, HousingValidator.MAX_RADIUS);

        HousingValidationResult nullResult = HousingValidator.validate(null, null);
        assertFalse(nullResult.isValid());
        assertEquals(0, nullResult.volume());
    }

    @Test
    @DisplayName("HousingService assigns, queries, and vacates houses correctly")
    void testHousingServiceAssignments() {
        HousingService service = HousingService.getInstance();
        BlockPos house1 = new BlockPos(100, 64, 100);

        assertFalse(service.hasAssignedHouse("guide"));
        assertFalse(service.isHouseOccupied(house1));

        // Assign Guide
        service.assignHouse("guide", house1);
        assertTrue(service.hasAssignedHouse("guide"));
        assertEquals(house1, service.getHouse("guide"));
        assertTrue(service.isHouseOccupied(house1));

        // Vacate Guide
        service.vacateHouse("guide");
        assertFalse(service.hasAssignedHouse("guide"));
        assertNull(service.getHouse("guide"));
        assertFalse(service.isHouseOccupied(house1));
    }

    @Test
    @DisplayName("Town NPCs canonical health and defense values")
    void testTownNpcStats() {
        assertEquals(250.0, TerraBaseTownNPC.BASE_HEALTH, 0.001);
        assertEquals(15, TerraBaseTownNPC.BASE_DEFENSE);

        // Pre-Hardmode defense = 15, Hardmode defense = 15 + 12 = 27
        int preHardmodeDef = TerraBaseTownNPC.BASE_DEFENSE;
        int hardmodeDef = TerraBaseTownNPC.BASE_DEFENSE + 12;

        assertEquals(15, preHardmodeDef);
        assertEquals(27, hardmodeDef);
    }
}
