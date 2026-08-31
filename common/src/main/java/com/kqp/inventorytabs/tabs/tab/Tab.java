package com.kqp.inventorytabs.tabs.tab;

import com.kqp.inventorytabs.tabs.render.TabRenderInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Base interface for tabs.
 */
public abstract class Tab {
    private final ItemStack renderItemStack;

    protected Tab(ItemStack renderItemStack) {
        this.renderItemStack = renderItemStack;
    }

    /**
     * Fires whenever the tab is clicked.
     */
    public abstract void open();

    /**
     * Returns true if the tab should stop being displayed. Should be synced up with
     * the provider that provides this tab.
     *
     * @return
     */
    public abstract boolean shouldBeRemoved();

    /**
     * Returns the text that's displayed when hovering over the tab.
     *
     * @return
     */
    public abstract Component getHoverText();

    /**
     * Called when the screen associated with the tab is closed.
     */
    public void onClose() {
    }

    /**
     * Returns the tab's priority when being displayed. The player's inventory is at
     * 100.
     *
     * @return
     */
    public int getPriority() {
        return 0;
    }

    /**
     * Deterministic tie-breaker used after priority and hover text when
     * sorting, so equally-named tabs keep a stable order.
     */
    public String getStableSortKey() {
        return "";
    }

    /**
     * Renders the tab's icon
     *
     * @param graphics      GuiGraphics
     * @param tabRenderInfo TabRenderInfo
     * @param currentScreen AbstractContainerScreen
     */
    public void renderTabIcon(GuiGraphics graphics, TabRenderInfo tabRenderInfo, AbstractContainerScreen<?> currentScreen) {
        graphics.renderItem(renderItemStack, tabRenderInfo.itemX, tabRenderInfo.itemY);
        graphics.renderItemDecorations(Minecraft.getInstance().font, renderItemStack, tabRenderInfo.itemX, tabRenderInfo.itemY);
    }
}
