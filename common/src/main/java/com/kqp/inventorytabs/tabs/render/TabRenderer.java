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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Handles the rendering of tabs. Tabs are laid out as vertical columns along
 * the sides of the container: the left column fills first, then overflows
 * into a column on the right side. When there are more tabs than slots, the
 * last slot becomes a page-forward arrow tab and (on later pages) the first
 * slot becomes a page-back arrow tab.
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

    private static final Identifier[] TAB_ABOVE_UNSELECTED_SPRITES = {
            Identifier.withDefaultNamespace("advancements/tab_above_left"),
            Identifier.withDefaultNamespace("advancements/tab_above_middle"),
            Identifier.withDefaultNamespace("advancements/tab_above_right")};
    private static final Identifier[] TAB_ABOVE_SELECTED_SPRITES = {
            Identifier.withDefaultNamespace("advancements/tab_above_left_selected"),
            Identifier.withDefaultNamespace("advancements/tab_above_middle_selected"),
            Identifier.withDefaultNamespace("advancements/tab_above_right_selected")};
    private static final Identifier[] TAB_BELOW_UNSELECTED_SPRITES = {
            Identifier.withDefaultNamespace("advancements/tab_below_left"),
            Identifier.withDefaultNamespace("advancements/tab_below_middle"),
            Identifier.withDefaultNamespace("advancements/tab_below_right")};
    private static final Identifier[] TAB_BELOW_SELECTED_SPRITES = {
            Identifier.withDefaultNamespace("advancements/tab_below_left_selected"),
            Identifier.withDefaultNamespace("advancements/tab_below_middle_selected"),
            Identifier.withDefaultNamespace("advancements/tab_below_right_selected")};

    private static final Identifier BUTTONS_TEXTURE = InventoryTabs.id("textures/gui/buttons.png");

    // Vertical layout: side tabs
    public static final int TAB_WIDTH = 32;
    public static final int TAB_HEIGHT = 28;
    // Horizontal layout: top/bottom tabs, spaced like the advancement screen
    public static final int ROW_TAB_WIDTH = 28;
    public static final int ROW_TAB_HEIGHT = 32;
    public static final int ROW_TAB_SPACING = 4;
    public static final int ARROW_WIDTH = 15;
    public static final int ARROW_HEIGHT = 13;
    /**
     * Tabs per column. Fixed (rather than derived from the GUI's height) so
     * tabs stay in the same place and keep the same distribution no matter
     * which container screen is open.
     */
    public static final int COLUMN_CAPACITY = 5;

    public final TabManager tabManager;

    private TabRenderInfo[] tabRenderInfos;

    private long pageTextRefreshTime;

    public TabRenderer(TabManager tabManager) {
        this.tabManager = tabManager;
    }

    public void renderBackground(GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        tabRenderInfos = getTabRenderInfos();

        for (int i = 0; i < tabRenderInfos.length; i++) {
            TabRenderInfo tabRenderInfo = tabRenderInfos[i];

            if (tabRenderInfo != null) {
                if (tabRenderInfo.tabReference != tabManager.currentTab || tabRenderInfo.pageArrow != 0) {
                    renderTab(graphics, tabRenderInfo, mouseX, mouseY);
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
                if (tabRenderInfo.pageArrow == 0 && tabRenderInfo.tabReference == tabManager.currentTab) {
                    renderTab(graphics, tabRenderInfo, mouseX, mouseY);
                }
            }
        }

        drawPageText(graphics);
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

            int leftPos = ((HandledScreenAccessor) currentScreen).getLeftPos();
            String text = (tabManager.currentPage + 1) + "/" + (tabManager.getMaxPages() + 1);
            int textWidth = textRenderer.width(text);
            int x;
            int y;
            if (InventoryTabs.getConfig().tabLayout == TabLayout.HORIZONTAL) {
                // Centered above the top row
                int guiWidth = ((HandledScreenAccessor) currentScreen).getImageWidth();
                x = leftPos + (guiWidth - textWidth) / 2;
                y = Math.max(((HandledScreenAccessor) currentScreen).getTopPos() - ROW_TAB_HEIGHT + 4 - 12, 2);
            } else {
                // Centered over the left tab column, kept clear of the GUI corner
                int columnCenterX = leftPos - TAB_WIDTH / 2 + 4;
                x = Math.min(columnCenterX - textWidth / 2, leftPos - textWidth - 2);
                y = Math.max(getColumnStartY(currentScreen) - 12, 2);
            }

            graphics.text(textRenderer, text, x, y, color);
        }
    }

    private void renderTab(GuiGraphicsExtractor graphics, TabRenderInfo tabRenderInfo, double mouseX, double mouseY) {
        AbstractContainerScreen<?> currentScreen = tabManager.getCurrentScreen();

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, tabRenderInfo.sprite, tabRenderInfo.x, tabRenderInfo.y,
                tabRenderInfo.texW, tabRenderInfo.texH);

        if (tabRenderInfo.pageArrow != 0) {
            boolean hovered = new Rectangle(tabRenderInfo.x, tabRenderInfo.y, tabRenderInfo.texW, tabRenderInfo.texH)
                    .contains(mouseX, mouseY);
            int u = tabRenderInfo.pageArrow > 0 ? ARROW_WIDTH : 0;
            u += hovered ? ARROW_WIDTH * 2 : 0;
            graphics.blit(RenderPipelines.GUI_TEXTURED, BUTTONS_TEXTURE, tabRenderInfo.itemX,
                    tabRenderInfo.itemY + 2, u, 0, ARROW_WIDTH, ARROW_HEIGHT, 256, 256);
        } else {
            tabRenderInfo.tabReference.renderTabIcon(graphics, tabRenderInfo, currentScreen);
        }
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
                    Component text = tabRenderInfo.pageArrow != 0
                            ? Component.translatable(tabRenderInfo.pageArrow > 0 ? "inventorytabs.tab.next_page"
                                    : "inventorytabs.tab.previous_page")
                            : tabRenderInfo.tabReference.getHoverText();
                    graphics.setTooltipForNextFrame(text, (int) mouseX, (int) mouseY);
                }
            }
        }
    }

    public TabRenderInfo[] getTabRenderInfos() {
        AbstractContainerScreen<?> currentScreen = tabManager.getCurrentScreen();

        int maxColumnLength = tabManager.getMaxColumnLength();
        int numSlots = tabManager.getNumSlots();
        int page = Math.min(tabManager.currentPage, tabManager.getMaxPages());

        int startingIndex = tabManager.firstTabIndexOfPage(page);

        boolean paginated = tabManager.isPaginated();
        boolean hasBackArrow = paginated && page > 0;
        // A next arrow is needed unless the remaining tabs all fit in this
        // page's tab slots (every slot except a back arrow's).
        boolean hasNextArrow = paginated
                && startingIndex + (numSlots - (hasBackArrow ? 1 : 0)) < tabManager.tabs.size();

        TabRenderInfo[] tabRenderInfo = new TabRenderInfo[numSlots];

        int x = ((HandledScreenAccessor) currentScreen).getLeftPos();
        int y = getColumnStartY(currentScreen);
        int guiWidth = ((HandledScreenAccessor) currentScreen).getImageWidth();
        int guiHeight = ((HandledScreenAccessor) currentScreen).getImageHeight();
        int topPos = ((HandledScreenAccessor) currentScreen).getTopPos();
        boolean horizontal = InventoryTabs.getConfig().tabLayout == TabLayout.HORIZONTAL;
        // Horizontal rows are centered on the container
        int rowWidth = maxColumnLength * ROW_TAB_WIDTH + (maxColumnLength - 1) * ROW_TAB_SPACING;
        int rowStartX = x + (guiWidth - rowWidth) / 2;

        int tabOffset = hasBackArrow ? 1 : 0;

        for (int i = 0; i < numSlots; i++) {
            boolean backArrowSlot = hasBackArrow && i == 0;
            boolean nextArrowSlot = hasNextArrow && i == numSlots - 1;
            int tabIndex = startingIndex + i - tabOffset;

            if (!backArrowSlot && !nextArrowSlot && tabIndex >= tabManager.tabs.size()) {
                continue;
            }

            boolean leftColumn = i < maxColumnLength;
            int columnIndex = leftColumn ? i : i - maxColumnLength;

            TabRenderInfo tabInfo = new TabRenderInfo();
            tabInfo.index = tabIndex;

            boolean selected = false;
            if (backArrowSlot) {
                tabInfo.pageArrow = -1;
            } else if (nextArrowSlot) {
                tabInfo.pageArrow = 1;
            } else {
                tabInfo.tabReference = tabManager.tabs.get(tabIndex);
                selected = tabInfo.tabReference == tabManager.currentTab;
            }

            // First and last tabs of a line get the capped sprites
            int spriteIndex = columnIndex == 0 ? 0 : (columnIndex == maxColumnLength - 1 ? 2 : 1);

            if (horizontal) {
                // Rows tuck 4px underneath the container's top and bottom edges
                tabInfo.x = rowStartX + columnIndex * (ROW_TAB_WIDTH + ROW_TAB_SPACING);
                tabInfo.y = leftColumn ? topPos - ROW_TAB_HEIGHT + 4 : topPos + guiHeight - 4;

                tabInfo.texW = ROW_TAB_WIDTH;
                tabInfo.texH = ROW_TAB_HEIGHT;

                if (leftColumn) {
                    tabInfo.sprite = selected ? TAB_ABOVE_SELECTED_SPRITES[spriteIndex]
                            : TAB_ABOVE_UNSELECTED_SPRITES[spriteIndex];
                } else {
                    tabInfo.sprite = selected ? TAB_BELOW_SELECTED_SPRITES[spriteIndex]
                            : TAB_BELOW_UNSELECTED_SPRITES[spriteIndex];
                }

                // Icon positions match the vanilla advancement tabs
                tabInfo.itemX = tabInfo.x + 6;
                tabInfo.itemY = tabInfo.y + (leftColumn ? 9 : 6);
            } else {
                // Columns tuck 4px underneath the container's side edges
                tabInfo.x = leftColumn ? x - TAB_WIDTH + 4 : x + guiWidth - 4;
                tabInfo.y = y + columnIndex * TAB_HEIGHT;

                tabInfo.texW = TAB_WIDTH;
                tabInfo.texH = TAB_HEIGHT;

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
            }

            tabRenderInfo[i] = tabInfo;
        }

        return tabRenderInfo;
    }

    /**
     * The tab columns are vertically centered on the screen rather than
     * anchored to the GUI, so they don't jump around when switching between
     * screens of different heights.
     */
    public static int getColumnStartY(AbstractContainerScreen<?> currentScreen) {
        return currentScreen.height / 2 - (COLUMN_CAPACITY * TAB_HEIGHT) / 2;
    }

    public void update() {
        pageTextRefreshTime = Math.max(pageTextRefreshTime - 1, 0);
    }

    public void resetPageTextRefreshTime() {
        pageTextRefreshTime = 60;
    }
}
