package com.terraforge.rpg.item.rarity;

import net.minecraft.network.chat.TextColor;

import java.util.Arrays;

/**
 * Terraria 1.4.5.8 canonical item rarities.
 */
public enum TerrariaRarity {
    GRAY(-1, "rarity.terraforge_rpg.gray", 0x828282),
    WHITE(0, "rarity.terraforge_rpg.white", 0xFFFFFF),
    BLUE(1, "rarity.terraforge_rpg.blue", 0x9696FF),
    GREEN(2, "rarity.terraforge_rpg.green", 0x96FF96),
    ORANGE(3, "rarity.terraforge_rpg.orange", 0xFFC896),
    LIGHT_RED(4, "rarity.terraforge_rpg.light_red", 0xFF9696),
    PINK(5, "rarity.terraforge_rpg.pink", 0xFF96FF),
    LIGHT_PURPLE(6, "rarity.terraforge_rpg.light_purple", 0xD2A0FF),
    LIME(7, "rarity.terraforge_rpg.lime", 0x96FF0A),
    YELLOW(8, "rarity.terraforge_rpg.yellow", 0xFFFF0A),
    CYAN(9, "rarity.terraforge_rpg.cyan", 0x05C8FF),
    RED(10, "rarity.terraforge_rpg.red", 0xFF2864),
    PURPLE(11, "rarity.terraforge_rpg.purple", 0xB428FF),
    RAINBOW(12, "rarity.terraforge_rpg.rainbow", 0xFFD700),
    AMBER(-11, "rarity.terraforge_rpg.amber", 0xFF6A00);

    private final int level;
    private final String translationKey;
    private final int colorRgb;
    private final TextColor textColor;

    TerrariaRarity(int level, String translationKey, int colorRgb) {
        this.level = level;
        this.translationKey = translationKey;
        this.colorRgb = colorRgb;
        this.textColor = TextColor.fromRgb(colorRgb);
    }

    public int getLevel() {
        return level;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public int getColorRgb() {
        return colorRgb;
    }

    public TextColor getTextColor() {
        return textColor;
    }

    public static TerrariaRarity fromLevel(int level) {
        return Arrays.stream(values())
                .filter(r -> r.level == level)
                .findFirst()
                .orElse(WHITE);
    }
}
