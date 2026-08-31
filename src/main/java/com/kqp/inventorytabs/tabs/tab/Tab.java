package com.kqp.inventorytabs.tabs.tab;

import com.kqp.inventorytabs.tabs.render.TabRenderInfo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Base interface for tabs.
 */
@Environment(EnvType.CLIENT)
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
     * Renders the tab's icon
     *
     * @param graphics      GuiGraphicsExtractor
     * @param tabRenderInfo TabRenderInfo
     * @param currentScreen AbstractContainerScreen
     */
    @Environment(EnvType.CLIENT)
    public void renderTabIcon(GuiGraphicsExtractor graphics, TabRenderInfo tabRenderInfo, AbstractContainerScreen<?> currentScreen) {
        graphics.item(renderItemStack, tabRenderInfo.itemX, tabRenderInfo.itemY);
        graphics.itemDecorations(currentScreen.getFont(), renderItemStack, tabRenderInfo.itemX, tabRenderInfo.itemY);
    }
}
