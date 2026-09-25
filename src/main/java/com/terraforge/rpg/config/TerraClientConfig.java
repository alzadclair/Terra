package com.terraforge.rpg.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Client visual and interface configuration for TerraForge RPG.
 */
public final class TerraClientConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ENABLE_SCREEN_SHAKE;
    public static final ModConfigSpec.DoubleValue SCREEN_SHAKE_INTENSITY;
    public static final ModConfigSpec.ConfigValue<String> HUD_MODE;
    public static final ModConfigSpec.BooleanValue SHOW_COOLDOWN_SECONDS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Visual Effects and Accessibility").push("visuals");
        ENABLE_SCREEN_SHAKE = builder.comment("Enable camera shake effects on heavy impacts and boss attacks.")
                .define("enableScreenShake", true);
        SCREEN_SHAKE_INTENSITY = builder.comment("Screen shake intensity multiplier.")
                .defineInRange("screenShakeIntensity", 1.0, 0.0, 3.0);
        builder.pop();

        builder.comment("User Interface").push("ui");
        HUD_MODE = builder.comment("HUD rendering mode: TERRAFORGE, VANILLA, or HYBRID.")
                .define("hudMode", "TERRAFORGE");
        SHOW_COOLDOWN_SECONDS = builder.comment("Display numerical seconds countdown over ability icons.")
                .define("showCooldownSeconds", true);
        builder.pop();

        SPEC = builder.build();
    }

    private TerraClientConfig() {}
}
