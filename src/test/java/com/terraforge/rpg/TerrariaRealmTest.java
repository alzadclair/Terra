package com.terraforge.rpg;

import com.terraforge.rpg.world.layer.TerrariaLayers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TerrariaRealmTest {

    @Test
    @DisplayName("Terraria Realm 5 continuous layers by Y elevation")
    void testLayerDetectionByElevation() {
        // Space: Y >= 260
        assertEquals(TerrariaLayers.SPACE, TerrariaLayers.getLayer(300));
        assertEquals(TerrariaLayers.SPACE, TerrariaLayers.getLayer(260));

        // Surface: 120 <= Y < 260
        assertEquals(TerrariaLayers.SURFACE, TerrariaLayers.getLayer(259));
        assertEquals(TerrariaLayers.SURFACE, TerrariaLayers.getLayer(150));
        assertEquals(TerrariaLayers.SURFACE, TerrariaLayers.getLayer(120));

        // Underground: 40 <= Y < 120
        assertEquals(TerrariaLayers.UNDERGROUND, TerrariaLayers.getLayer(119));
        assertEquals(TerrariaLayers.UNDERGROUND, TerrariaLayers.getLayer(70));
        assertEquals(TerrariaLayers.UNDERGROUND, TerrariaLayers.getLayer(40));

        // Cavern: -30 <= Y < 40
        assertEquals(TerrariaLayers.CAVERN, TerrariaLayers.getLayer(39));
        assertEquals(TerrariaLayers.CAVERN, TerrariaLayers.getLayer(0));
        assertEquals(TerrariaLayers.CAVERN, TerrariaLayers.getLayer(-30));

        // Underworld: Y < -30
        assertEquals(TerrariaLayers.UNDERWORLD, TerrariaLayers.getLayer(-31));
        assertEquals(TerrariaLayers.UNDERWORLD, TerrariaLayers.getLayer(-64));
    }

    @Test
    @DisplayName("Space layer exhibits canonical low gravity")
    void testSpaceGravity() {
        assertEquals(0.015, TerrariaLayers.SPACE.getGravity(), 0.0001);
        assertEquals(0.08, TerrariaLayers.SURFACE.getGravity(), 0.0001);
        assertEquals(0.08, TerrariaLayers.UNDERGROUND.getGravity(), 0.0001);
        assertEquals(0.08, TerrariaLayers.CAVERN.getGravity(), 0.0001);
        assertEquals(0.08, TerrariaLayers.UNDERWORLD.getGravity(), 0.0001);
    }
}
