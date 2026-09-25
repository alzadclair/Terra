package com.terraforge.rpg.client.hud;

import com.terraforge.rpg.armor.ArmorSetBonus;
import com.terraforge.rpg.armor.ArmorSetService;
import com.terraforge.rpg.config.TerraClientConfig;
import com.terraforge.rpg.level.LevelService;
import com.terraforge.rpg.player.data.PlayerRPGData;
import com.terraforge.rpg.registry.ModAttachments;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Modern Terraria-inspired Top-Left HUD.
 * Renders Life, Mana, Level progress, Defense, and Active Set Bonus in a unified,
 * high-contrast, polished RPG status panel.
 */
public final class TerraHudOverlay {

    public static void register(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.AIR_LEVEL,
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("terraforge_rpg", "rpg_hud"),
                TerraHudOverlay::render);
    }

    private static void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (player == null || minecraft.options.hideGui || player.isSpectator() || !player.isAlive() || minecraft.screen != null) {
            return;
        }

        String hudMode = "TERRAFORGE";
        try {
            if (TerraClientConfig.HUD_MODE != null && TerraClientConfig.HUD_MODE.get() != null) {
                hudMode = TerraClientConfig.HUD_MODE.get().toUpperCase();
            }
        } catch (Exception ignored) {}

        if ("VANILLA".equalsIgnoreCase(hudMode)) {
            return;
        }

        PlayerRPGData data = player.getData(ModAttachments.PLAYER_RPG_DATA);
        var font = minecraft.font;

        // Position: Top-Left Corner
        final int hudX = 8;
        final int hudY = 8;
        final int barWidth = 138;
        final int panelWidth = 154;
        final int panelHeight = 72;

        // 1. Sleek Ornamental Plate Background
        graphics.fill(hudX, hudY, hudX + panelWidth, hudY + panelHeight, 0xDC0B0F19);
        graphics.renderOutline(hudX, hudY, panelWidth, panelHeight, 0xFF2A364F);
        graphics.renderOutline(hudX + 1, hudY + 1, panelWidth - 2, panelHeight - 2, 0xFF151C2B);

        // Corner accents
        graphics.fill(hudX, hudY, hudX + 3, hudY + 3, 0xFF6366F1);
        graphics.fill(hudX + panelWidth - 3, hudY, hudX + panelWidth, hudY + 3, 0xFF6366F1);
        graphics.fill(hudX, hudY + panelHeight - 3, hudX + 3, hudY + panelHeight, 0xFF6366F1);
        graphics.fill(hudX + panelWidth - 3, hudY + panelHeight - 3, hudX + panelWidth, hudY + panelHeight, 0xFF6366F1);

        // 2. Row 1: Level Badge & XP Mini-Bar (hudY + 5)
        int row1Y = hudY + 5;
        String levelBadge = data.getLevel() >= 1000 && data.hasEvolution()
                ? "§e★ Lv. 1000 §d(∞)"
                : "§e★ Lv. " + data.getLevel();
        graphics.drawString(font, levelBadge, hudX + 6, row1Y, 0xFFFFFF, true);

        // XP Bar (Right beside level badge)
        int xpBarX = hudX + 70;
        int xpBarY = row1Y + 2;
        int xpBarW = hudX + panelWidth - 6 - xpBarX;
        int xpBarH = 5;

        double curXp = data.getCurrentXp();
        double reqXp = LevelService.getRequiredXp(data);
        double xpRatio = reqXp > 0 ? Math.clamp(curXp / reqXp, 0.0, 1.0) : 0.0;
        int filledXp = (int) Math.round(xpBarW * xpRatio);

        graphics.fill(xpBarX - 1, xpBarY - 1, xpBarX + xpBarW + 1, xpBarY + xpBarH + 1, 0xFF141A14);
        graphics.fill(xpBarX, xpBarY, xpBarX + xpBarW, xpBarY + xpBarH, 0xFF1F2B1A);
        if (filledXp > 0) {
            graphics.fillGradient(xpBarX, xpBarY, xpBarX + filledXp, xpBarY + xpBarH, 0xFF84CC16, 0xFF4D7C0F);
        }
        graphics.renderOutline(xpBarX - 1, xpBarY - 1, xpBarW + 2, xpBarH + 2, 0xFF65A30D);

        // 3. Row 2: Terraria Heart Life Bar (hudY + 18)
        int barX = hudX + 8;
        int lifeY = hudY + 18;
        int barH = 11;

        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        double lifeRatio = maxHealth > 0 ? Math.clamp((double) health / maxHealth, 0.0, 1.0) : 0.0;
        int filledLife = (int) Math.round(barWidth * lifeRatio);

        // Dark track
        graphics.fill(barX - 1, lifeY - 1, barX + barWidth + 1, lifeY + barH + 1, 0xFF2A0C13);
        graphics.fill(barX, lifeY, barX + barWidth, lifeY + barH, 0xFF1A060B);

        // Crimson gradient fill with specular highlight
        if (filledLife > 0) {
            graphics.fillGradient(barX, lifeY, barX + filledLife, lifeY + barH, 0xFFE11D48, 0xFF9F1239);
            // Specular shine line at top of health bar
            graphics.fill(barX, lifeY, barX + filledLife, lifeY + 1, 0xFFFB7185);
            // Bottom shadow line
            graphics.fill(barX, lifeY + barH - 1, barX + filledLife, lifeY + barH, 0xFF881337);
        }

        // 10-Segment Terraria heart tick marks
        for (int i = 1; i < 10; i++) {
            int tickX = barX + (barWidth * i / 10);
            graphics.fill(tickX, lifeY, tickX + 1, lifeY + barH, 0x44000000);
        }

        // Outline
        graphics.renderOutline(barX - 1, lifeY - 1, barWidth + 2, barH + 2, 0xFFE11D48);

        // Crisp centered Life text
        String lifeStr = String.format("❤ %.0f / %.0f", health, maxHealth);
        int lifeStrW = font.width(lifeStr);
        graphics.drawString(font, lifeStr, barX + (barWidth / 2) - (lifeStrW / 2), lifeY + 2, 0xFFFFFF, true);

        // 4. Row 3: Terraria Crystal Star Mana Bar (hudY + 34)
        int manaY = hudY + 34;

        double curMana = data.getCurrentMana();
        double maxMana = data.getMaxMana();
        double manaRatio = maxMana > 0 ? Math.clamp(curMana / maxMana, 0.0, 1.0) : 0.0;
        int filledMana = (int) Math.round(barWidth * manaRatio);

        // Dark track
        graphics.fill(barX - 1, manaY - 1, barX + barWidth + 1, manaY + barH + 1, 0xFF0B1930);
        graphics.fill(barX, manaY, barX + barWidth, manaY + barH, 0xFF070F1E);

        // Celestial Blue gradient fill with specular highlight
        if (filledMana > 0) {
            graphics.fillGradient(barX, manaY, barX + filledMana, manaY + barH, 0xFF38BDF8, 0xFF1D4ED8);
            // Specular shine line at top of mana bar
            graphics.fill(barX, manaY, barX + filledMana, manaY + 1, 0xFF93C5FD);
            // Bottom shadow line
            graphics.fill(barX, manaY + barH - 1, barX + filledMana, manaY + barH, 0xFF1E3A8A);
        }

        // 5-Segment star tick marks
        for (int i = 1; i < 5; i++) {
            int tickX = barX + (barWidth * i / 5);
            graphics.fill(tickX, manaY, tickX + 1, manaY + barH, 0x44000000);
        }

        // Outline
        graphics.renderOutline(barX - 1, manaY - 1, barWidth + 2, barH + 2, 0xFF38BDF8);

        // Crisp centered Mana text
        String manaStr = String.format("★ %.0f / %.0f", curMana, maxMana);
        int manaStrW = font.width(manaStr);
        graphics.drawString(font, manaStr, barX + (barWidth / 2) - (manaStrW / 2), manaY + 2, 0xFFFFFF, true);

        // 5. Row 4: Defense & Armor Set Bonus (hudY + 50)
        int row4Y = hudY + 50;
        int totalDefense = ArmorSetService.getTotalArmorDefense(player);
        String defBadge = "§7🛡 §f" + totalDefense + " Def";
        graphics.drawString(font, defBadge, barX, row4Y, 0xFFFFFF, true);

        ArmorSetBonus setBonus = ArmorSetService.getActiveSetBonus(player);
        if (setBonus != ArmorSetBonus.NONE) {
            String bonusBadge = "§6[" + setBonus.getDisplayName() + "]";
            int badgeW = font.width(bonusBadge);
            graphics.drawString(font, bonusBadge, barX + barWidth - badgeW, row4Y, 0xFFFFFF, true);
        } else {
            String noBonus = "§8[No Set Bonus]";
            int badgeW = font.width(noBonus);
            graphics.drawString(font, noBonus, barX + barWidth - badgeW, row4Y, 0xFFFFFF, true);
        }
    }

    private TerraHudOverlay() {}
}
