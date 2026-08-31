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

import static com.kqp.inventorytabs.init.InventoryTabs.*;

/**
 * Handles the rendering of tabs.
 */
public class TabRenderer {
    private static final Identifier[] TAB_TOP_UNSELECTED_SPRITES = {
            Identifier.withDefaultNamespace("container/creative_inventory/tab_top_unselected_1"),
            Identifier.withDefaultNamespace("container/creative_inventory/tab_top_unselected_4")};
    private static final Identifier[] TAB_TOP_SELECTED_SPRITES = {
            Identifier.withDefaultNamespace("container/creative_inventory/tab_top_selected_1"),
            Identifier.withDefaultNamespace("container/creative_inventory/tab_top_selected_4")};
    private static final Identifier[] TAB_BOTTOM_UNSELECTED_SPRITES = {
            Identifier.withDefaultNamespace("container/creative_inventory/tab_bottom_unselected_1"),
            Identifier.withDefaultNamespace("container/creative_inventory/tab_bottom_unselected_4")};
    private static final Identifier[] TAB_BOTTOM_SELECTED_SPRITES = {
            Identifier.withDefaultNamespace("container/creative_inventory/tab_bottom_selected_1"),
            Identifier.withDefaultNamespace("container/creative_inventory/tab_bottom_selected_4")};

    private static final Identifier BUTTONS_TEXTURE = InventoryTabs.id("textures/gui/buttons.png");

    public static final int TAB_WIDTH = 26;
    public static final int TAB_HEIGHT = 32;
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
                if (tabRenderInfo.tabReference != tabManager.currentTab && !tabRenderInfo.inFront) {
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
                if (tabRenderInfo.tabReference == tabManager.currentTab || tabRenderInfo.inFront) {
                    renderTab(graphics, tabRenderInfo);
                }
            }
        }

        drawButtons(graphics, mouseX, mouseY);

        drawPageText(graphics);
    }

    private void drawButtons(GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        AbstractContainerScreen<?> currentScreen = tabManager.getCurrentScreen();

        int width = ((HandledScreenAccessor) currentScreen).getImageWidth();
        int oX = ((HandledScreenAccessor) currentScreen).getLeftPos();
        int oY = ((HandledScreenAccessor) currentScreen).getTopPos();

        // Drawing back button
        int x = oX - BUTTON_WIDTH - 4;
        x += ((TabRenderingHints) currentScreen).getTopRowXOffset();
        int y = getButtonY(currentScreen);
        boolean hovered = new Rectangle(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).contains(mouseX, mouseY);
        int u = 0;
        u += tabManager.canGoBackAPage() && hovered ? BUTTON_WIDTH * 2 : 0;
        int v = tabManager.canGoBackAPage() ? 0 : 13;
        graphics.blit(RenderPipelines.GUI_TEXTURED, BUTTONS_TEXTURE, x, y, u, v, BUTTON_WIDTH, BUTTON_HEIGHT, 256, 256);

        // Drawing forward button
        x = oX + width + 4;
        x += ((TabRenderingHints) currentScreen).getTopRowXOffset();
        y = getButtonY(currentScreen);
        hovered = new Rectangle(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).contains(mouseX, mouseY);
        u = 15;
        u += tabManager.canGoForwardAPage() && hovered ? BUTTON_WIDTH * 2 : 0;
        v = tabManager.canGoForwardAPage() ? 0 : 13;
        graphics.blit(RenderPipelines.GUI_TEXTURED, BUTTONS_TEXTURE, x, y, u, v, BUTTON_WIDTH, BUTTON_HEIGHT, 256, 256);
    }

    private void drawPageText(GuiGraphicsExtractor graphics) {
        if (tabManager.getMaxPages() > 1 && pageTextRefreshTime > 0) {
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
            int y = Math.max(oY - 34, TAB_HEIGHT + 4);

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

        int maxRowLength = tabManager.getMaxRowLength();
        int numVisibleTabs;
        if(isBigInvLoaded) {
            numVisibleTabs = (maxRowLength * 2) + 5;
        } else if (isPlayerExLoaded) {
            numVisibleTabs = (maxRowLength * 2) - 3;
        } else if (isLevelzLoaded) {
            numVisibleTabs = (maxRowLength * 2) - 2;
        }else {
            numVisibleTabs = maxRowLength * 2;
        }
        int startingIndex = tabManager.currentPage * numVisibleTabs;

        TabRenderInfo[] tabRenderInfo = new TabRenderInfo[numVisibleTabs];

        int x = ((HandledScreenAccessor) currentScreen).getLeftPos();
        int y = ((HandledScreenAccessor) currentScreen).getTopPos();

        for (int i = 0; i < numVisibleTabs; i++) {
            if (startingIndex + i < tabManager.tabs.size()) {
                // Setup basic info
                Tab tab = tabManager.tabs.get(startingIndex + i);
                boolean topRow = i < maxRowLength;
                if(isPlayerExLoaded) {
                    topRow = i < maxRowLength - 3;
                } else if(isLevelzLoaded) {
                    topRow = i < maxRowLength - 2;
                }
                boolean selected = tab == tabManager.currentTab;

                // Create tab info object
                TabRenderInfo tabInfo = new TabRenderInfo();
                tabInfo.tabReference = tab;
                tabInfo.index = startingIndex + i;

                // Calc x value
                tabInfo.x = x + i * (TAB_WIDTH + 1);
                if (!topRow) {
                    tabInfo.x -= maxRowLength * (TAB_WIDTH + 1);
                }

                // Calc y value
                if (topRow) {
                    tabInfo.y = y - 28;
                } else {
                    if(isBigInvLoaded) {
                        tabInfo.y = y + ((HandledScreenAccessor) currentScreen).getImageHeight() + 32;
                    } else {
                        tabInfo.y = y + ((HandledScreenAccessor) currentScreen).getImageHeight() - 4;
                    }
                }

                // Calc texture dimensions
                tabInfo.texW = TAB_WIDTH;
                tabInfo.texH = 32;

                // Pick tab sprite: left cap for the first column, middle otherwise
                int spriteIndex = (i == 0 || i == maxRowLength) ? 0 : 1;
                if (topRow) {
                    tabInfo.sprite = selected ? TAB_TOP_SELECTED_SPRITES[spriteIndex]
                            : TAB_TOP_UNSELECTED_SPRITES[spriteIndex];
                } else {
                    tabInfo.sprite = selected ? TAB_BOTTOM_SELECTED_SPRITES[spriteIndex]
                            : TAB_BOTTOM_UNSELECTED_SPRITES[spriteIndex];
                }

                // Calc item position
                if (topRow) {
                    tabInfo.itemX = tabInfo.x + 6;
                    tabInfo.itemY = tabInfo.y + 8;
                } else {
                    tabInfo.itemX = tabInfo.x + 6;
                    tabInfo.itemY = tabInfo.y + 6;
                }

                // Apply rendering hints
                if (currentScreen instanceof TabRenderingHints) {
                    if (topRow) {
                        if(isPlayerExLoaded) {
                            tabInfo.x += ((TabRenderingHints) currentScreen).getTopRowXOffset() + 87;
                            tabInfo.itemX += ((TabRenderingHints) currentScreen).getTopRowXOffset() + 87;
                        } else if(isLevelzLoaded) {
                            tabInfo.x += ((TabRenderingHints) currentScreen).getTopRowXOffset() + 54;
                            tabInfo.itemX += ((TabRenderingHints) currentScreen).getTopRowXOffset() + 54;
                        }else {
                            tabInfo.x += ((TabRenderingHints) currentScreen).getTopRowXOffset();
                            tabInfo.itemX += ((TabRenderingHints) currentScreen).getTopRowXOffset();
                        }
                        tabInfo.y += ((TabRenderingHints) currentScreen).getTopRowYOffset();
                        tabInfo.itemY += ((TabRenderingHints) currentScreen).getTopRowYOffset();
                    } else {
                        if(isBigInvLoaded) {
                            tabInfo.x += ((TabRenderingHints) currentScreen).getBottomRowXOffset() - 145;
                            tabInfo.itemX += ((TabRenderingHints) currentScreen).getBottomRowXOffset() - 145;
                        } else if(isPlayerExLoaded) {
                            tabInfo.x += ((TabRenderingHints) currentScreen).getBottomRowXOffset() + 86;
                            tabInfo.itemX += ((TabRenderingHints) currentScreen).getBottomRowXOffset() + 86;
                        } else if(isLevelzLoaded) {
                            tabInfo.x += ((TabRenderingHints) currentScreen).getBottomRowXOffset() + 60;
                            tabInfo.itemX += ((TabRenderingHints) currentScreen).getBottomRowXOffset() + 60;
                        }else {
                            tabInfo.x += ((TabRenderingHints) currentScreen).getBottomRowXOffset();
                            tabInfo.itemX += ((TabRenderingHints) currentScreen).getBottomRowXOffset();
                        }
                        tabInfo.y += ((TabRenderingHints) currentScreen).getBottomRowYOffset();
                        tabInfo.itemY += ((TabRenderingHints) currentScreen).getBottomRowYOffset();
                    }
                }

                // Tall GUIs (e.g. large chests) can push the top row off the
                // screen; clamp it back on and draw it in front of the panel.
                if (topRow && tabInfo.y < 0) {
                    int delta = -tabInfo.y;
                    tabInfo.y += delta;
                    tabInfo.itemY += delta;
                    tabInfo.inFront = true;
                }

                tabRenderInfo[i] = tabInfo;
            }
        }

        return tabRenderInfo;
    }

    /**
     * The Y position of the paging buttons, clamped onto the screen for tall
     * GUIs the same way the top tab row is.
     */
    public static int getButtonY(AbstractContainerScreen<?> currentScreen) {
        int y = ((HandledScreenAccessor) currentScreen).getTopPos() - 16;
        y += ((TabRenderingHints) currentScreen).getTopRowYOffset();

        return Math.max(y, (TAB_HEIGHT - BUTTON_HEIGHT) / 2);
    }

    public void update() {
        pageTextRefreshTime = Math.max(pageTextRefreshTime - 1, 0);
    }

    public void resetPageTextRefreshTime() {
        pageTextRefreshTime = 60;
    }
}
