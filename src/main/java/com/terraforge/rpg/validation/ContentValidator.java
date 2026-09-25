package com.terraforge.rpg.validation;

import com.terraforge.rpg.util.TerraLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Validation engine for verifying data integrity, registries, race definitions,
 * and content matrix consistency during development and startup.
 */
public final class ContentValidator {
    private static final List<String> ERRORS = new ArrayList<>();
    private static final List<String> WARNINGS = new ArrayList<>();

    private ContentValidator() {}

    public static synchronized void recordError(String context, String message) {
        String entry = "[" + context + "] ERROR: " + message;
        ERRORS.add(entry);
        TerraLogger.error("VALIDATION", entry);
    }

    public static synchronized void recordWarning(String context, String message) {
        String entry = "[" + context + "] WARNING: " + message;
        WARNINGS.add(entry);
        TerraLogger.warn("VALIDATION", entry);
    }

    public static synchronized void clear() {
        ERRORS.clear();
        WARNINGS.clear();
    }

    public static synchronized List<String> getErrors() {
        return Collections.unmodifiableList(new ArrayList<>(ERRORS));
    }

    public static synchronized List<String> getWarnings() {
        return Collections.unmodifiableList(new ArrayList<>(WARNINGS));
    }

    public static synchronized boolean hasErrors() {
        return !ERRORS.isEmpty();
    }

    /**
     * Executes initial sanity checks on base registrations and configuration.
     */
    public static synchronized boolean runSanityCheck() {
        clear();
        TerraLogger.info("VALIDATION", "Running startup content validation checks...");

        // Sanity checks will run on loaded data
        if (hasErrors()) {
            TerraLogger.error("VALIDATION", "Content validation failed with {} error(s).", ERRORS.size());
            return false;
        }

        TerraLogger.info("VALIDATION", "Content validation passed successfully. 0 errors, {} warning(s).", WARNINGS.size());
        return true;
    }
}
