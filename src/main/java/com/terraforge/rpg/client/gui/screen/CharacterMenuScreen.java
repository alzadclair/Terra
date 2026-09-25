package com.terraforge.rpg.client.gui.screen;

import com.terraforge.rpg.level.LevelService;
import com.terraforge.rpg.network.payload.SpendStatPointsRequest;
import com.terraforge.rpg.player.data.PlayerRPGData;
import com.terraforge.rpg.registry.ModAttachments;
import com.terraforge.rpg.stats.AttributeCapRegistry;
import com.terraforge.rpg.stats.AttributeType;
import com.terraforge.rpg.stats.PlayerAttributesSnapshot;
import com.terraforge.rpg.stats.calculation.AttributeCalculator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Modern RPG Character Menu displaying 3D player model, level progress,
 * race info, the 7 attributes, and server-validated point allocation buttons (+1, +5, +10, MAX).
 * Rendered with solid opaque dark slate palette for 100% crisp typography without post-process blur bleed.
 */
public final class CharacterMenuScreen extends Screen {
    private static final int GUI_WIDTH = 410;
    private static final int GUI_HEIGHT = 240;

    public CharacterMenuScreen() {
        super(Component.translatable("key.terraforge_rpg.open_character_menu"));
    }

    @Override
    protected void init() {
        clearWidgets();
        var player = Minecraft.getInstance().player;
        if (player == null) return;

        PlayerRPGData data = player.getData(ModAttachments.PLAYER_RPG_DATA);
        int left = (width - GUI_WIDTH) / 2;
        int top = (height - GUI_HEIGHT) / 2;
        int rightColLeft = left + 152;

        int rowTop = top + 34;
        int rowHeight = 24;

        for (AttributeType type : AttributeType.values()) {
            final AttributeType currentType = type;
            int y = rowTop + (type.ordinal() * rowHeight);

            int available = data.getAvailableStatusPoints();
            int currentRank = getCurrentRank(data, type);
            int cap = AttributeCapRegistry.getEffectiveCap(data, type);
            boolean uncapped = AttributeCapRegistry.isEvolutionUncapped(data);
            int room = uncapped ? available : Math.max(0, cap - currentRank);

            // +1 Button
            Button btn1 = Button.builder(Component.literal("+1"), b -> spendPoints(currentType, 1))
                    .bounds(rightColLeft + 150, y - 2, 22, 16)
                    .build();
            btn1.active = available >= 1 && room >= 1;
            addRenderableWidget(btn1);

            // +5 Button
            Button btn5 = Button.builder(Component.literal("+5"), b -> spendPoints(currentType, 5))
                    .bounds(rightColLeft + 174, y - 2, 22, 16)
                    .build();
            btn5.active = available >= 5 && room >= 5;
            addRenderableWidget(btn5);

            // +10 Button
            Button btn10 = Button.builder(Component.literal("+10"), b -> spendPoints(currentType, 10))
                    .bounds(rightColLeft + 198, y - 2, 26, 16)
                    .build();
            btn10.active = available >= 10 && room >= 10;
            addRenderableWidget(btn10);

            // MAX Button
            Button btnMax = Button.builder(Component.literal("MAX"), b -> spendPoints(currentType, Math.max(1, Math.min(available, room))))
                    .bounds(rightColLeft + 226, y - 2, 26, 16)
                    .build();
            btnMax.active = available >= 1 && room >= 1;
            btnMax.setTooltip(Tooltip.create(Component.literal("Gastar o máximo possível até o cap ou pontos disponíveis.")));
            addRenderableWidget(btnMax);
        }

        // Special Accessory Slot Selector Button
        int boxBottom = top + 130;
        int infoY = boxBottom + 6;
        int lineGap = 11;
        Button btnArtefato = Button.builder(Component.literal("Artefato Especial"), b -> Minecraft.getInstance().setScreen(new SpecialAccessorySelectorScreen()))
                .bounds(left + 14, infoY + (lineGap * 4) + 12, 118, 16)
                .tooltip(Tooltip.create(Component.literal("Clique para equipar ou alterar o Artefato Especial do inventário.")))
                .build();
        addRenderableWidget(btnArtefato);

        // Close Button
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> onClose())
                .bounds(left + (GUI_WIDTH / 2) - 45, top + GUI_HEIGHT - 22, 90, 18)
                .build());
    }

    private void spendPoints(AttributeType type, int amount) {
        if (amount <= 0) return;
        PacketDistributor.sendToServer(new SpendStatPointsRequest(type.getId(), amount));
    }

    private int getCurrentRank(PlayerRPGData data, AttributeType type) {
        return switch (type) {
            case DEFENSE -> data.getDefenseRank();
            case MAGIC_DEFENSE -> data.getMagicDefenseRank();
            case ATTACK -> data.getAttackRank();
            case MAGIC_ATTACK -> data.getMagicAttackRank();
            case CRITICAL -> data.getCriticalRank();
            case CRITICAL_CHANCE -> data.getCriticalChanceRank();
            case SPEED -> data.getSpeedRank();
        };
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Dark clean cinematic overlay WITHOUT Minecraft's post-processing blur shader
        graphics.fillGradient(0, 0, width, height, 0xC0060910, 0xD8060910);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Render game background tint without blur shader
        renderBackground(graphics, mouseX, mouseY, partialTick);

        var player = Minecraft.getInstance().player;
        if (player == null) return;
        PlayerRPGData data = player.getData(ModAttachments.PLAYER_RPG_DATA);

        int left = (width - GUI_WIDTH) / 2;
        int top = (height - GUI_HEIGHT) / 2;

        // Solid, crisp, non-blurry RPG Slate background plate
        graphics.fill(left, top, left + GUI_WIDTH, top + GUI_HEIGHT, 0xFF121620);
        graphics.renderOutline(left, top, GUI_WIDTH, GUI_HEIGHT, 0xFF3B4861);
        graphics.renderOutline(left + 1, top + 1, GUI_WIDTH - 2, GUI_HEIGHT - 2, 0xFF1E2433);

        // Header Title Banner
        graphics.fill(left + 2, top + 2, left + GUI_WIDTH - 2, top + 22, 0xFF181E2C);
        graphics.fill(left + 2, top + 22, left + GUI_WIDTH - 2, top + 23, 0xFF3B4861);
        graphics.drawString(font, Component.literal("§6§lTERRAFORGE RPG §7— §fStatus do Personagem"), left + 12, top + 7, 0xFFFFFF, false);

        // Left Panel (Player Preview & Info)
        int boxLeft = left + 12;
        int boxTop = top + 28;
        int boxRight = left + 138;
        int boxBottom = top + 130;
        graphics.fill(boxLeft, boxTop, boxRight, boxBottom, 0xFF0D1017);
        graphics.renderOutline(boxLeft, boxTop, boxRight - boxLeft, boxBottom - boxTop, 0xFF2A3447);

        // 3D Player Entity Render
        InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, boxLeft, boxTop, boxRight, boxBottom, 40, 0.0625f, mouseX, mouseY, player);
        graphics.flush();

        // Left Panel Player Stats
        int infoY = boxBottom + 6;
        int lineGap = 12;

        // Level & Evolution Status
        if (data.getLevel() >= 1000 && data.hasEvolution()) {
            graphics.drawString(font, "Nível: §e1000 MAX §d(Potencial: ∞)", left + 14, infoY, 0xFFFFFF, false);
        } else {
            graphics.drawString(font, "Nível: §e" + data.getLevel(), left + 14, infoY, 0xFFFFFF, false);
        }

        // XP Bar
        double currentXp = data.getCurrentXp();
        double reqXp = LevelService.getRequiredXp(data);
        graphics.drawString(font, String.format("XP: §b%.0f / %.0f", currentXp, reqXp), left + 14, infoY + lineGap, 0xCCCCCC, false);

        // Race & Ability
        String raceName = capitalize(data.getPrimaryRace());
        if (data.isHybrid() && !data.getSecondaryRace().isEmpty()) {
            raceName = "§d[Híbrido] §f" + raceName;
        }
        graphics.drawString(font, "Raça: " + raceName, left + 14, infoY + (lineGap * 2), 0xFFFFFF, false);

        // Status Points Available
        graphics.drawString(font, "Pontos: §a§l" + data.getAvailableStatusPoints(), left + 14, infoY + (lineGap * 3), 0xFFFFFF, false);

        // Special Accessory Slot Display
        String acc = data.getEquippedSpecialAccessory().isEmpty() ? "§7Vazio" : "§6" + capitalize(data.getEquippedSpecialAccessory());
        graphics.drawString(font, "Artefato: " + acc, left + 14, infoY + (lineGap * 4), 0xFFD700, false);

        // Vertical Column Divider Line
        graphics.fill(left + 144, top + 26, left + 145, top + GUI_HEIGHT - 28, 0xFF2A3447);

        // Right Panel: 7 Attributes Display
        int rightColLeft = left + 152;
        int rowTop = top + 34;
        int rowHeight = 24;

        List<Component> hoveredTooltip = null;

        for (AttributeType type : AttributeType.values()) {
            int y = rowTop + (type.ordinal() * rowHeight);
            PlayerAttributesSnapshot snapshot = PlayerAttributesSnapshot.compute(data, type);

            // Sub-row background stripe for readability
            if (type.ordinal() % 2 == 0) {
                graphics.fill(rightColLeft - 4, y - 4, left + GUI_WIDTH - 6, y + rowHeight - 4, 0xFF161B27);
            }

            String capStr = snapshot.uncapped() ? "§d∞" : String.valueOf(snapshot.effectiveCap());
            String line = String.format("%s: §f%d §7/ %s", Component.translatable(type.getTranslationKey()).getString(), snapshot.investedRank(), capStr);
            graphics.drawString(font, line, rightColLeft, y + 2, 0xFFFFFF, false);

            // Tooltip trigger check
            if (mouseX >= rightColLeft && mouseX <= rightColLeft + 145 && mouseY >= y - 3 && mouseY <= y + 17) {
                hoveredTooltip = new ArrayList<>();
                hoveredTooltip.add(Component.translatable(type.getTranslationKey()).append(Component.literal(" - Detalhes:")));
                hoveredTooltip.add(Component.literal(String.format("§7Base: §f%.0f | Raça: §a+%.0f", snapshot.baseValue(), snapshot.raceBonus())));
                hoveredTooltip.add(Component.literal(String.format("§7Investido: §e+%d | Equipamento: §b+%.0f", snapshot.investedRank(), snapshot.gearBonus())));
                hoveredTooltip.add(Component.literal(String.format("§7Buffs: §d+%.0f §7| Final: §6§l%.0f", snapshot.buffBonus(), snapshot.finalValue())));
                hoveredTooltip.add(Component.literal("§8" + getFormulaDescription(type, snapshot.investedRank())));
            }
        }

        // Render widgets (Buttons)
        super.render(graphics, mouseX, mouseY, partialTick);

        // Render Tooltip LAST on top of everything to eliminate overlapping and z-fighting
        if (hoveredTooltip != null) {
            graphics.renderComponentTooltip(font, hoveredTooltip, mouseX, mouseY);
        }
    }

    private String getFormulaDescription(AttributeType type, int rank) {
        return switch (type) {
            case DEFENSE -> String.format("Redução Física: %.1f%%", AttributeCalculator.calculatePhysicalDamageReduction(rank) * 100);
            case MAGIC_DEFENSE -> String.format("Redução Mágica: %.1f%%", AttributeCalculator.calculateMagicDamageReduction(rank) * 100);
            case ATTACK -> String.format("Dano Físico: %.1fx", AttributeCalculator.calculatePhysicalDamageMultiplier(rank));
            case MAGIC_ATTACK -> String.format("Dano Mágico: %.1fx", AttributeCalculator.calculateMagicDamageMultiplier(rank));
            case CRITICAL -> String.format("Multiplicador Crítico: %.2fx", AttributeCalculator.calculateCriticalMultiplier(rank));
            case CRITICAL_CHANCE -> String.format("Chance Crítica: +%.1f%%", AttributeCalculator.calculateCriticalChanceBonus(rank));
            case SPEED -> String.format("Bônus de Velocidade: +%.1f%%", AttributeCalculator.calculateMovementSpeedBonus(rank) * 100);
        };
    }

    private static String capitalize(String text) {
        if (text == null || text.isEmpty()) return "";
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
