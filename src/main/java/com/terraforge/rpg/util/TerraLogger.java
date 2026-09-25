package com.terraforge.rpg.util;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

/**
 * Centralized structured logger for TerraForge RPG.
 */
public final class TerraLogger {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean debugMode = false;

    private TerraLogger() {}

    public static void setDebugMode(boolean enabled) {
        debugMode = enabled;
    }

    public static boolean isDebugMode() {
        return debugMode;
    }

    public static void info(String category, String message, Object... args) {
        LOGGER.info("[TerraForge - {}] " + message, category, args);
    }

    public static void warn(String category, String message, Object... args) {
        LOGGER.warn("[TerraForge - {}] " + message, category, args);
    }

    public static void error(String category, String message, Object... args) {
        LOGGER.error("[TerraForge - {}] " + message, category, args);
    }

    public static void debug(String category, String message, Object... args) {
        if (debugMode) {
            LOGGER.info("[TerraForge - {}][DEBUG] " + message, category, args);
        } else {
            LOGGER.debug("[TerraForge - {}] " + message, category, args);
        }
    }
}
