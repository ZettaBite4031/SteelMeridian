package dev.zettatech.steelmeridian.client.gui;

import dev.zettatech.steelmeridian.content.machine.PrototypeMachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class PrototypeMachineScreen extends AbstractContainerScreen<PrototypeMachineMenu> {
    public PrototypeMachineScreen(PrototypeMachineMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        imageWidth = 176;
        imageHeight = 166;

        titleLabelX = 8;
        titleLabelY = 6;

        inventoryLabelX = 8;
        inventoryLabelY = 172;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFC6C6C6);
        graphics.fill(leftPos + 6, topPos + 18, leftPos + imageWidth - 6, topPos + 68, 0xFF8B8B8B);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);

        String recipe = menu.selectedRecipe() == null ? "none" : menu.selectedRecipe().id().toString();

        graphics.drawString(font, Component.literal("State: " + menu.status()),
            8, 24, 0xFFFFFFFF, false);
        graphics.drawString(font, Component.literal("Recipe: " + recipe),
            8, 36, 0xFFFFFFFF, false);
        graphics.drawString(font, Component.literal("Progress: " + menu.remainingTicks() + "/" + menu.totalTicks() + " ticks"),
            8, 48, 0xFFFFFFFF, false);
        graphics.drawString(font, Component.literal("Input: " + menu.inputCount() + "  Output: " + menu.outputCount()),
            8, 60, 0xFFFFFFFF, false);
    }
}

