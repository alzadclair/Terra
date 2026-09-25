package com.terraforge.rpg.event;

import com.terraforge.rpg.event.invasion.BloodMoonEvent;
import com.terraforge.rpg.event.invasion.GoblinArmyEvent;
import com.terraforge.rpg.event.invasion.SlimeRainEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Server-authoritative manager controlling all active Terraria events and invasions.
 */
public final class TerrariaEventManager {

    private static final TerrariaEventManager INSTANCE = new TerrariaEventManager();

    private final Map<String, ITerrariaEvent> registeredEvents = new HashMap<>();

    private TerrariaEventManager() {
        registerEvent(new SlimeRainEvent());
        registerEvent(new BloodMoonEvent());
        registerEvent(new GoblinArmyEvent());
    }

    public static TerrariaEventManager getInstance() {
        return INSTANCE;
    }

    public void registerEvent(ITerrariaEvent event) {
        registeredEvents.put(event.getEventId().toLowerCase(), event);
    }

    public ITerrariaEvent getEvent(String eventId) {
        if (eventId == null) return null;
        return registeredEvents.get(eventId.toLowerCase());
    }

    public Collection<ITerrariaEvent> getAllEvents() {
        return Collections.unmodifiableCollection(registeredEvents.values());
    }

    public boolean isEventActive(String eventId) {
        ITerrariaEvent event = getEvent(eventId);
        return event != null && event.isActive();
    }

    public boolean startEvent(String eventId, ServerLevel level) {
        ITerrariaEvent event = getEvent(eventId);
        if (event != null && !event.isActive()) {
            event.start(level);
            return true;
        }
        return false;
    }

    public boolean stopEvent(String eventId, ServerLevel level) {
        ITerrariaEvent event = getEvent(eventId);
        if (event != null && event.isActive()) {
            event.stop(level);
            return true;
        }
        return false;
    }

    public void tick(ServerLevel level) {
        for (ITerrariaEvent event : registeredEvents.values()) {
            if (event.isActive()) {
                event.tick(level);
            }
        }

        // Automatic event triggers based on world time
        checkNaturalEventTriggers(level);
    }

    private void checkNaturalEventTriggers(ServerLevel level) {
        long dayTime = level.getDayTime() % 24000L;

        // Sunset (13000 ticks): Check Blood Moon
        if (dayTime == 13000L && !isEventActive("blood_moon")) {
            // Check if any player has >= 120 HP
            boolean eligible = level.players().stream().anyMatch(p -> p.getMaxHealth() >= 24.0f); // 24 = 12 hearts / 120 Terraria HP
            if (eligible && level.random.nextInt(9) == 0) { // 1 in 9 canonical chance
                startEvent("blood_moon", level);
            }
        }
    }

    public void onEntityKilled(LivingEntity entity, Player killer) {
        for (ITerrariaEvent event : registeredEvents.values()) {
            if (event.isActive()) {
                event.onEntityKilled(entity, killer);
            }
        }
    }
}
