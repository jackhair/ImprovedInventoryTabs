package com.kqp.inventorytabs.tabs;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.kqp.inventorytabs.api.TabProviderRegistry;
import com.kqp.inventorytabs.init.InventoryTabsClient;
import com.kqp.inventorytabs.interf.TabManagerContainer;
import com.kqp.inventorytabs.mixin.accessor.HandledScreenAccessor;
import com.kqp.inventorytabs.tabs.render.TabRenderInfo;
import com.kqp.inventorytabs.tabs.render.TabRenderer;
import com.kqp.inventorytabs.tabs.tab.Tab;
import com.kqp.inventorytabs.util.MouseUtil;

import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;


/**
 * Manages everything related to tabs.
 */
public class TabManager {
    public final List<Tab> tabs;
    public Tab currentTab;

    private AbstractContainerScreen<?> currentScreen;
    public int currentPage = 0;
    public boolean tabOpenedRecently;
    public int prevCursorStackSlot = -1;

    public final TabRenderer tabRenderer;

    public TabManager() {
        this.tabs = new ArrayList<>();
        this.tabRenderer = new TabRenderer(this);
    }

    public void update() {
        refreshAvailableTabs();

        tabRenderer.update();
    }

    public void setCurrentTab(Tab tab) {
        this.currentTab = tab;
    }
    public void removeTabs() {
        for (int i = 0; i < tabs.size(); i++) {
            tabs.remove(i);
            i--;
        }
    }

    private void refreshAvailableTabs() {
        // Remove old ones
        for (int i = 0; i < tabs.size(); i++) {
            if (tabs.get(i).shouldBeRemoved()) {
                tabs.remove(i);
                i--;
            }
        }

        LocalPlayer player = Minecraft.getInstance().player;

        if (player != null && player.isAlive()) {
            // Add new tabs
            TabProviderRegistry.getTabProviders().forEach(tabProvider -> {
                tabProvider.addAvailableTabs(player, tabs);
            });
        }

        if (currentTab != null) {
            for (int i = 0; i < tabs.size(); i++) {
                Tab tab = tabs.get(i);
                if (currentTab != tab && currentTab.equals(tab)) {
                    // We've come across a tab we already have open
                    tabs.set(i, currentTab);
                    break;
                }
            }
        }

        // Sort
        tabs.sort(
                Comparator.comparing(Tab::getPriority).reversed().thenComparing(tab -> tab.getHoverText().getString()));
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int guiWidth = ((HandledScreenAccessor) currentScreen).getImageWidth();
            int guiHeight = ((HandledScreenAccessor) currentScreen).getImageHeight();
            int x = ((HandledScreenAccessor) currentScreen).getLeftPos();
            int y = ((HandledScreenAccessor) currentScreen).getTopPos();

            if (mouseX > x && mouseX < x + guiWidth && mouseY > y && mouseY < y + guiHeight) {
                return false;
            }

            int buttonX = TabRenderer.getButtonX(currentScreen);

            // Check back button
            if (new Rectangle(buttonX, TabRenderer.getBackButtonY(currentScreen), TabRenderer.BUTTON_WIDTH,
                    TabRenderer.BUTTON_HEIGHT).contains(mouseX, mouseY)) {
                if (canGoBackAPage()) {
                    setCurrentPage(currentPage - 1);
                    playClick();

                    return true;
                }
            }

            // Check forward button
            if (new Rectangle(buttonX, TabRenderer.getForwardButtonY(currentScreen), TabRenderer.BUTTON_WIDTH, TabRenderer.BUTTON_HEIGHT)
                    .contains(mouseX, mouseY)) {
                if (canGoForwardAPage()) {
                    setCurrentPage(currentPage + 1);
                    playClick();

                    return true;
                }
            }

            TabRenderInfo[] tabRenderInfos = tabRenderer.getTabRenderInfos();

            for (int i = 0; i < tabRenderInfos.length; i++) {
                TabRenderInfo tabRenderInfo = tabRenderInfos[i];

                if (tabRenderInfo != null) {
                    if (tabRenderInfo.tabReference != currentTab) {
                        Rectangle rect = new Rectangle(tabRenderInfo.x, tabRenderInfo.y, tabRenderInfo.texW,
                                tabRenderInfo.texH);

                        if (rect.contains(mouseX, mouseY)) {
                            onTabClick(tabRenderInfo.tabReference);

                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public boolean keyPressed(KeyEvent event) {
        if (InventoryTabsClient.NEXT_TAB_KEY_BIND.matches(event)) {
            int currentTabIndex = tabs.indexOf(currentTab);
            if (event.hasShiftDown()) {
                if (currentTabIndex > 0) {
                    onTabClick(tabs.get(currentTabIndex - 1));
                } else {
                    onTabClick(tabs.get(tabs.size() - 1));
                }
                return true;
            } else {
                if (currentTabIndex < tabs.size() - 1) {
                    onTabClick(tabs.get(currentTabIndex + 1));
                } else {
                    onTabClick(tabs.get(0));
                }

                return true;
            }
        }

        return false;
    }

    public void onScreenOpen(AbstractContainerScreen<?> screen) {
        refreshAvailableTabs();

        setCurrentScreen(screen);
        MouseUtil.tryPop();
    }

    public void restoreCursorStack(MultiPlayerGameMode manager, LocalPlayer player, AbstractContainerMenu currentHandler) {
        // Try restore the cursor stack if it exists and wasn't dropped.
        if (manager!= null && this.prevCursorStackSlot != -1) {
            currentHandler.findSlot(player.getInventory(), this.prevCursorStackSlot).ifPresent((screenSlot) ->{
                manager.handleContainerInput(
                        currentHandler.containerId,
                        screenSlot,
                        0, // Mouse Left Click
                        ContainerInput.PICKUP,
                        player
                );
            });
            this.prevCursorStackSlot = -1;
        }
    }

    public void onTabClick(Tab tab) {
        // Push current mouse position
        // This is to persist mouse position across screens
        MouseUtil.push();

        // Set tab open flag
        tabOpenedRecently = true;

        Minecraft client = Minecraft.getInstance();
        AbstractContainerMenu handler = client.player.containerMenu;
        this.prevCursorStackSlot = -1;

        if (handler != null) {

            // Preserve the cursor stack
            ItemStack prevCursorStack = client.player.containerMenu.getCarried();
            if (prevCursorStack != null && !prevCursorStack.isEmpty()) {
                this.prevCursorStackSlot = client.player.getInventory().getFreeSlot();

                if (this.prevCursorStackSlot != -1 && client.gameMode != null) {
                    // Put the cursor stack there
                    handler.findSlot(client.player.getInventory(), this.prevCursorStackSlot).ifPresent((screenSlot) -> {
                        client.gameMode.handleContainerInput(
                                handler.containerId,
                                screenSlot,
                                0, // Mouse Left Click
                                ContainerInput.PICKUP,
                                client.player
                        );
                    });
                }
            }

            // Close any handled screens
            // This fixes the inventory desync issue
            client.getConnection().send(new ServerboundContainerClosePacket(handler.containerId));
        }

        // Open new tab
        onOpenTab(tab);
        tab.open();
    }

    public void onOpenTab(Tab tab) {
        if (currentTab != null && currentTab != tab) {
            currentTab.onClose();
        }

        setCurrentTab(tab);
        setCurrentPage(pageOf(tab));
    }

    public int pageOf(Tab tab) {
        int index = tabs.indexOf(tab);

        return index / (getMaxColumnLength() * 2);
    }

    public int getMaxColumnLength() {
        int guiHeight = ((HandledScreenAccessor) currentScreen).getImageHeight();

        return Math.max(guiHeight / TabRenderer.TAB_HEIGHT, 1);
    }

    public void setCurrentScreen(AbstractContainerScreen<?> screen) {
        this.currentScreen = screen;
    }

    public AbstractContainerScreen<?> getCurrentScreen() {
        return currentScreen;
    }

    public void setCurrentPage(int page) {
        if (page > 0 && tabs.size() <= getMaxColumnLength() * 2) {
            System.err.println("Not enough tabs to paginate, ignoring");

            return;
        }

        if (this.currentPage != page) {
            tabRenderer.resetPageTextRefreshTime();
        }

        this.currentPage = page;
    }

    public boolean screenOpenedViaTab() {
        if (tabOpenedRecently) {
            tabOpenedRecently = false;

            return true;
        }

        return false;
    }

    public int getMaxPages() {
        return (tabs.size() - 1) / (getMaxColumnLength() * 2);
    }

    public boolean canGoBackAPage() {
        return currentPage != 0;
    }

    public boolean canGoForwardAPage() {
        return currentPage < getMaxPages();
    }

    public static TabManager getInstance() {
        return ((TabManagerContainer) Minecraft.getInstance()).getTabManager();
    }

    public static void playClick() {
        Minecraft.getInstance().getSoundManager()
                .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}
