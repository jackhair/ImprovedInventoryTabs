package com.kqp.inventorytabs.tabs.render;

import java.awt.Rectangle;

import com.kqp.inventorytabs.init.InventoryTabs;
import com.kqp.inventorytabs.mixin.accessor.HandledScreenAccessor;
import com.kqp.inventorytabs.tabs.TabManager;
import com.kqp.inventorytabs.tabs.tab.Tab;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/**
 * Handles the rendering of tabs. Tabs are laid out as vertical columns along
 * the sides of the container: the left column fills first, then overflows
 * into a column on the right side.
 */
public class TabRenderer {
    private static final Identifier[] TAB_LEFT_UNSELECTED_SPRITES = {
            Identifier.withDefaultNamespace("advancements/tab_left_top"),
            Identifier.withDefaultNamespace("advancements/tab_left_middle"),
            Identifier.withDefaultNamespace("advancements/tab_left_bottom")};
    private static final Identifier[] TAB_LEFT_SELECTED_SPRITES = {
            Identifier.withDefaultNamespace("advancements/tab_left_top_selected"),
            Identifier.withDefaultNamespace("advancements/tab_left_middle_selected"),
            Identifier.withDefaultNamespace("advancements/tab_left_bottom_selected")};
    private static final Identifier[] TAB_RIGHT_UNSELECTED_SPRITES = {
            Identifier.withDefaultNamespace("advancements/tab_right_top"),
            Identifier.withDefaultNamespace("advancements/tab_right_middle"),
            Identifier.withDefaultNamespace("advancements/tab_right_bottom")};
    private static final Identifier[] TAB_RIGHT_SELECTED_SPRITES = {
            Identifier.withDefaultNamespace("advancements/tab_right_top_selected"),
            Identifier.withDefaultNamespace("advancements/tab_right_middle_selected"),
            Identifier.withDefaultNamespace("advancements/tab_right_bottom_selected")};

    private static final Identifier BUTTONS_TEXTURE = InventoryTabs.id("textures/gui/buttons.png");

    public static final int TAB_WIDTH = 32;
    public static final int TAB_HEIGHT = 28;
    public static final int BUTTON_WIDTH = 15;
    public static final int BUTTON_HEIGHT = 13;

    public final TabManager tabManager;

    private TabRenderInfo[] tabRenderInfos;

    private long pageTextRefreshTime;

    public TabRenderer(TabManager tabManager) {
        this.tabManager = tabManager;
    }

    public void renderBackground(GuiGraphicsExtractor graphics) {
        tabRenderInfos = getTabRenderInfos();

        for (int i = 0; i < tabRenderInfos.length; i++) {
            TabRenderInfo tabRenderInfo = tabRenderInfos[i];

            if (tabRenderInfo != null) {
                if (tabRenderInfo.tabReference != tabManager.currentTab) {
                    renderTab(graphics, tabRenderInfo);
                }
            }
        }
    }

    public void renderForeground(GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        if (tabRenderInfos == null) {
            tabRenderInfos = getTabRenderInfos();
        }

        for (int i = 0; i < tabRenderInfos.length; i++) {
            TabRenderInfo tabRenderInfo = tabRenderInfos[i];

            if (tabRenderInfo != null) {
                if (tabRenderInfo.tabReference == tabManager.currentTab) {
                    renderTab(graphics, tabRenderInfo);
                }
            }
        }

        drawButtons(graphics, mouseX, mouseY);

        drawPageText(graphics);
    }

    private void drawButtons(GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        AbstractContainerScreen<?> currentScreen = tabManager.getCurrentScreen();

        // Drawing back button
        int x = getButtonX(currentScreen);
        int y = getBackButtonY(currentScreen);
        boolean hovered = new Rectangle(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).contains(mouseX, mouseY);
        int u = 0;
        u += tabManager.canGoBackAPage() && hovered ? BUTTON_WIDTH * 2 : 0;
        int v = tabManager.canGoBackAPage() ? 0 : 13;
        graphics.blit(RenderPipelines.GUI_TEXTURED, BUTTONS_TEXTURE, x, y, u, v, BUTTON_WIDTH, BUTTON_HEIGHT, 256, 256);

        // Drawing forward button
        y = getForwardButtonY(currentScreen);
        hovered = new Rectangle(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).contains(mouseX, mouseY);
        u = 15;
        u += tabManager.canGoForwardAPage() && hovered ? BUTTON_WIDTH * 2 : 0;
        v = tabManager.canGoForwardAPage() ? 0 : 13;
        graphics.blit(RenderPipelines.GUI_TEXTURED, BUTTONS_TEXTURE, x, y, u, v, BUTTON_WIDTH, BUTTON_HEIGHT, 256, 256);
    }

    private void drawPageText(GuiGraphicsExtractor graphics) {
        if (tabManager.getMaxPages() > 0 && pageTextRefreshTime > 0) {
            int color = 0xFFFFFFFF;

            if (pageTextRefreshTime <= 20) {
                float transparency = pageTextRefreshTime / 20F;

                color &= 0x00FFFFFF;
                color = ((int) (0xFF * transparency) << 24) | color;
            }

            AbstractContainerScreen<?> currentScreen = tabManager.getCurrentScreen();
            Font textRenderer = Minecraft.getInstance().font;

            int oX = currentScreen.width;
            int oY = ((HandledScreenAccessor) currentScreen).getTopPos();

            String text = (tabManager.currentPage + 1) + " / " + (tabManager.getMaxPages() + 1);
            int x = (oX - textRenderer.width(text)) / 2;
            int y = Math.max(oY - 12, 2);

            graphics.text(textRenderer, text, x, y, color);
        }
    }

    private void renderTab(GuiGraphicsExtractor graphics, TabRenderInfo tabRenderInfo) {
        AbstractContainerScreen<?> currentScreen = tabManager.getCurrentScreen();

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, tabRenderInfo.sprite, tabRenderInfo.x, tabRenderInfo.y,
                tabRenderInfo.texW, tabRenderInfo.texH);

        tabRenderInfo.tabReference.renderTabIcon(graphics, tabRenderInfo, currentScreen);
    }

    public void renderHoverTooltips(GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        if (tabRenderInfos == null) {
            return;
        }

        for (int i = 0; i < tabRenderInfos.length; i++) {
            TabRenderInfo tabRenderInfo = tabRenderInfos[i];

            if (tabRenderInfo != null) {
                Rectangle itemRec = new Rectangle(tabRenderInfo.itemX, tabRenderInfo.itemY, 16, 16);

                if (itemRec.contains(mouseX, mouseY)) {
                    graphics.setTooltipForNextFrame(tabRenderInfo.tabReference.getHoverText(),
                            (int) mouseX, (int) mouseY);
                }
            }
        }
    }

    public TabRenderInfo[] getTabRenderInfos() {
        AbstractContainerScreen<?> currentScreen = tabManager.getCurrentScreen();

        int maxColumnLength = tabManager.getMaxColumnLength();
        int numVisibleTabs = maxColumnLength * 2;
        int startingIndex = tabManager.currentPage * numVisibleTabs;

        TabRenderInfo[] tabRenderInfo = new TabRenderInfo[numVisibleTabs];

        int x = ((HandledScreenAccessor) currentScreen).getLeftPos();
        int y = ((HandledScreenAccessor) currentScreen).getTopPos();
        int guiWidth = ((HandledScreenAccessor) currentScreen).getImageWidth();

        for (int i = 0; i < numVisibleTabs; i++) {
            if (startingIndex + i < tabManager.tabs.size()) {
                // Setup basic info
                Tab tab = tabManager.tabs.get(startingIndex + i);
                boolean leftColumn = i < maxColumnLength;
                int columnIndex = leftColumn ? i : i - maxColumnLength;
                boolean selected = tab == tabManager.currentTab;

                // Create tab info object
                TabRenderInfo tabInfo = new TabRenderInfo();
                tabInfo.tabReference = tab;
                tabInfo.index = startingIndex + i;

                // Tabs tuck 4px underneath the container's side edges
                tabInfo.x = leftColumn ? x - TAB_WIDTH + 4 : x + guiWidth - 4;
                tabInfo.y = y + columnIndex * TAB_HEIGHT;

                tabInfo.texW = TAB_WIDTH;
                tabInfo.texH = TAB_HEIGHT;

                // First and last tabs of a column get the capped sprites
                int spriteIndex = columnIndex == 0 ? 0 : (columnIndex == maxColumnLength - 1 ? 2 : 1);
                if (leftColumn) {
                    tabInfo.sprite = selected ? TAB_LEFT_SELECTED_SPRITES[spriteIndex]
                            : TAB_LEFT_UNSELECTED_SPRITES[spriteIndex];
                } else {
                    tabInfo.sprite = selected ? TAB_RIGHT_SELECTED_SPRITES[spriteIndex]
                            : TAB_RIGHT_UNSELECTED_SPRITES[spriteIndex];
                }

                // Icon positions match the vanilla advancement tabs
                tabInfo.itemX = tabInfo.x + (leftColumn ? 10 : 6);
                tabInfo.itemY = tabInfo.y + 5;

                tabRenderInfo[i] = tabInfo;
            }
        }

        return tabRenderInfo;
    }

    /**
     * The paging buttons sit to the left of the left tab column.
     */
    public static int getButtonX(AbstractContainerScreen<?> currentScreen) {
        return ((HandledScreenAccessor) currentScreen).getLeftPos() - TAB_WIDTH + 4 - BUTTON_WIDTH - 3;
    }

    public static int getBackButtonY(AbstractContainerScreen<?> currentScreen) {
        return ((HandledScreenAccessor) currentScreen).getTopPos() + 1;
    }

    public static int getForwardButtonY(AbstractContainerScreen<?> currentScreen) {
        return getBackButtonY(currentScreen) + BUTTON_HEIGHT + 2;
    }

    public void update() {
        pageTextRefreshTime = Math.max(pageTextRefreshTime - 1, 0);
    }

    public void resetPageTextRefreshTime() {
        pageTextRefreshTime = 60;
    }
}
