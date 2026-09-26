package com.terraforge.rpg.client.gui.screen;

import com.terraforge.rpg.accessory.special.SpecialAccessoryItem;
import com.terraforge.rpg.accessory.special.SpecialAccessoryService;
import com.terraforge.rpg.network.payload.EquipSpecialAccessoryRequest;
import com.terraforge.rpg.network.payload.UnequipSpecialAccessoryRequest;
import com.terraforge.rpg.player.data.PlayerRPGData;
import com.terraforge.rpg.registry.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Modal screen listing valid Special Accessories in the player's inventory,
 * allowing safe equipment and unequipping.
 */
public final class SpecialAccessorySelectorScreen extends Screen {
    private static final int GUI_WIDTH = 280;
    private static final int GUI_HEIGHT = 200;

    public SpecialAccessorySelectorScreen() {
        super(Component.literal("Seletor de Artefato Especial"));
    }

    @Override
    protected void init() {
        clearWidgets();
        var player = Minecraft.getInstance().player;
        if (player == null) return;

        PlayerRPGData data = player.getData(ModAttachments.PLAYER_RPG_DATA);
        int left = (width - GUI_WIDTH) / 2;
        int top = (height - GUI_HEIGHT) / 2;

        // Unequip button if accessory is already equipped
        if (!data.getEquippedSpecialAccessory().isEmpty()) {
            addRenderableWidget(Button.builder(Component.literal("§cDesequipar Atual"), b -> {
                PacketDistributor.sendToServer(new UnequipSpecialAccessoryRequest());
                Minecraft.getInstance().setScreen(new CharacterMenuScreen());
            }).bounds(left + 20, top + 26, 120, 18).build());
        }

        // Find available accessories in inventory
        List<ItemStack> available = new ArrayList<>();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (SpecialAccessoryService.isValidAccessory(key.getPath())) {
                available.add(stack);
            }
        }

        int itemTop = top + 52;
        int rowHeight = 22;

        for (int i = 0; i < available.size() && itemTop + (i * rowHeight) < top + GUI_HEIGHT - 30; i++) {
            ItemStack stack = available.get(i);
            String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
            int y = itemTop + (i * rowHeight);

            addRenderableWidget(Button.builder(Component.literal("Equipar"), b -> {
                PacketDistributor.sendToServer(new EquipSpecialAccessoryRequest(itemId));
                Minecraft.getInstance().setScreen(new CharacterMenuScreen());
            }).bounds(left + GUI_WIDTH - 70, y - 2, 54, 18).build());
        }

        // Back button
        addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, b -> Minecraft.getInstance().setScreen(new CharacterMenuScreen()))
                .bounds(left + (GUI_WIDTH / 2) - 40, top + GUI_HEIGHT - 22, 80, 18)
                .build());
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fillGradient(0, 0, width, height, 0xC0060910, 0xD8060910);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);

        var player = Minecraft.getInstance().player;
        if (player == null) return;
        PlayerRPGData data = player.getData(ModAttachments.PLAYER_RPG_DATA);

        int left = (width - GUI_WIDTH) / 2;
        int top = (height - GUI_HEIGHT) / 2;

        graphics.fillGradient(left, top, left + GUI_WIDTH, top + GUI_HEIGHT, 0xEE12151D, 0xEE1A1E29);
        graphics.renderOutline(left, top, GUI_WIDTH, GUI_HEIGHT, 0xFF4A5568);

        graphics.drawString(font, "§6§lARTEFATOS ESPECIAIS §8| §f§lSlot Único", left + 12, top + 10, 0xFFFFFF, true);

        List<ItemStack> available = new ArrayList<>();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (SpecialAccessoryService.isValidAccessory(key.getPath())) {
                available.add(stack);
            }
        }

        if (available.isEmpty() && data.getEquippedSpecialAccessory().isEmpty()) {
            graphics.drawString(font, "§7Nenhum Artefato Especial no inventário.", left + 20, top + 70, 0xAAAAAA, true);
        }

        int itemTop = top + 52;
        int rowHeight = 22;

        for (int i = 0; i < available.size() && itemTop + (i * rowHeight) < top + GUI_HEIGHT - 30; i++) {
            ItemStack stack = available.get(i);
            int y = itemTop + (i * rowHeight);

            graphics.renderItem(stack, left + 16, y - 2);
            graphics.drawString(font, stack.getHoverName(), left + 38, y + 2, 0xFFFFFF, true);

            // Hover tooltip
            if (mouseX >= left + 16 && mouseX <= left + GUI_WIDTH - 75 && mouseY >= y - 2 && mouseY <= y + 16) {
                graphics.renderTooltip(font, stack, mouseX, mouseY);
            }
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
