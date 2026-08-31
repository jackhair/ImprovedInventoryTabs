package com.kqp.inventorytabs.gametest;

import com.kqp.inventorytabs.api.TabProviderRegistry;
import com.kqp.inventorytabs.init.InventoryTabsConfig;
import com.kqp.inventorytabs.mixin.accessor.HandledScreenAccessor;
import com.kqp.inventorytabs.tabs.TabManager;
import com.kqp.inventorytabs.tabs.render.TabRenderer;
import com.kqp.inventorytabs.tabs.tab.SimpleBlockTab;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.AutoConfigClient;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;

import org.lwjgl.glfw.GLFW;

/**
 * Smoke test for the 26.2 port: joins a world, places some openable blocks
 * next to the player, opens the inventory (which loads
 * AbstractContainerScreen and applies the tab mixins) and screenshots the
 * tabs being rendered. Then opens a large chest to verify the tab row is
 * clamped onto the screen for tall GUIs.
 */
public class InventoryTabsClientGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();

            singleplayer.getServer().runCommand("setblock 2 -60 -1 minecraft:chest[facing=west,type=right]");
            singleplayer.getServer().runCommand("setblock 2 -60 0 minecraft:chest[facing=west,type=left]");
            singleplayer.getServer().runCommand("setblock 0 -60 2 minecraft:crafting_table");
            singleplayer.getServer().runCommand("setblock -2 -60 0 minecraft:furnace");
            singleplayer.getConnection().waitForClientboundPackets();
            context.waitTicks(5);

            context.getInput().pressKey(options -> options.keyInventory);
            context.waitForScreen(InventoryScreen.class);
            context.waitTicks(20);
            context.takeScreenshot("inventory-tabs-open");

            context.getInput().pressKey(GLFW.GLFW_KEY_ESCAPE);
            context.waitTicks(5);

            // Open the large chest; its GUI is tall enough that the tab row
            // must be clamped onto the screen.
            context.getInput().lookAt(new BlockPos(2, -60, 0));
            context.waitTicks(2);
            context.getInput().pressMouse(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
            context.waitForScreen(ContainerScreen.class);
            context.waitTicks(20);
            context.takeScreenshot("large-chest-tabs");

            context.getInput().pressKey(GLFW.GLFW_KEY_ESCAPE);
            context.waitTicks(5);

            // An item frame on a chest changes that chest's tab icon to the
            // framed item (the vanilla way to distinguish identical chests).
            singleplayer.getServer().runCommand(
                    "summon minecraft:item_frame 1 -60 0 {Facing:4b,Item:{id:\"minecraft:diamond\",count:1}}");
            singleplayer.getConnection().waitForClientboundPackets();
            context.waitTicks(5);

            context.getInput().pressKey(options -> options.keyInventory);
            context.waitForScreen(InventoryScreen.class);
            context.waitTicks(20);
            context.takeScreenshot("item-frame-icon");

            context.getInput().pressKey(GLFW.GLFW_KEY_ESCAPE);
            context.waitTicks(5);

            // Surround the player with barrels so the tabs overflow into the
            // right column and paginate.
            int[][] barrelPositions = {
                    {3, 1}, {3, -2}, {-3, 1}, {-3, -1}, {1, 3}, {-1, 3},
                    {1, -3}, {-1, -3}, {3, 3}, {-3, 3}, {3, -3}, {-3, -3}};
            for (int[] pos : barrelPositions) {
                singleplayer.getServer().runCommand("setblock " + pos[0] + " -60 " + pos[1] + " minecraft:barrel");
            }
            singleplayer.getConnection().waitForClientboundPackets();
            context.waitTicks(5);

            context.getInput().pressKey(options -> options.keyInventory);
            context.waitForScreen(InventoryScreen.class);
            context.waitTicks(20);
            context.takeScreenshot("tab-overflow-pagination");

            // Click the next-arrow tab (last slot, bottom of the right
            // column) and verify we end up on the second page.
            int[] arrowCenter = context.computeOnClient(mc -> {
                AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) mc.gui.screen();
                HandledScreenAccessor accessor = (HandledScreenAccessor) screen;
                int x = accessor.getLeftPos() + accessor.getImageWidth() - 4 + TabRenderer.TAB_WIDTH / 2;
                int y = TabRenderer.getColumnStartY(screen)
                        + (TabRenderer.COLUMN_CAPACITY - 1) * TabRenderer.TAB_HEIGHT + TabRenderer.TAB_HEIGHT / 2;
                return new int[]{x, y};
            });
            context.getInput().setCursorPos(arrowCenter[0], arrowCenter[1]);
            context.waitTicks(2);
            context.getInput().pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
            context.waitTicks(2);

            int page = context.computeOnClient(mc -> TabManager.getInstance().currentPage);
            if (page == 0) {
                // setCursorPos may use raw window pixels rather than gui units
                double scale = context.computeOnClient(mc -> (double) mc.getWindow().getGuiScale());
                context.getInput().setCursorPos(arrowCenter[0] * scale, arrowCenter[1] * scale);
                context.waitTicks(2);
                context.getInput().pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
                context.waitTicks(2);
                page = context.computeOnClient(mc -> TabManager.getInstance().currentPage);
            }
            if (page != 1) {
                throw new AssertionError("Expected the next-arrow tab to switch to page 1, but page is " + page);
            }

            context.waitTicks(10);
            context.takeScreenshot("tab-page-two");

            context.getInput().pressKey(GLFW.GLFW_KEY_ESCAPE);
            context.waitTicks(5);

            // Excluding a block via config removes its tab. The crafting
            // table lives in the "unique" provider, which the exclude list
            // previously missed entirely.
            context.runOnClient(mc -> {
                InventoryTabsConfig config = AutoConfig.getConfigHolder(InventoryTabsConfig.class).getConfig();
                config.excludeTab = java.util.List.of("minecraft:crafting_table");
                TabProviderRegistry.init("reload");
            });
            context.waitTicks(5);

            context.getInput().pressKey(options -> options.keyInventory);
            context.waitForScreen(InventoryScreen.class);
            context.waitTicks(10);

            boolean excluded = context.computeOnClient(mc -> TabManager.getInstance().tabs.stream()
                    .noneMatch(tab -> tab instanceof SimpleBlockTab blockTab
                            && blockTab.blockId.getPath().equals("crafting_table")));
            if (!excluded) {
                throw new AssertionError("Crafting table tab is still present after excluding it via config");
            }

            context.takeScreenshot("tab-excluded");
        }

        // Back on the title screen: the Cloth Config screen (as opened via
        // Mod Menu or the NeoForge config button) renders correctly.
        context.runOnClient(mc -> {
            InventoryTabsConfig config = AutoConfig.getConfigHolder(InventoryTabsConfig.class).getConfig();
            config.excludeTab = java.util.List.of(
                    "tiered:reforging_station",
                    "#techreborn:block_entities_without_inventories",
                    "#inventorytabs:mod_compat_blacklist");
        });
        context.setScreen(() -> AutoConfigClient.getConfigScreen(InventoryTabsConfig.class, null).get());
        context.waitTicks(5);
        // Expand the "Do not show" list by clicking its underlined label
        context.getInput().setCursorPos(95, 126);
        context.waitTicks(2);
        context.getInput().pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        context.waitTicks(3);
        context.getInput().setCursorPos(190, 252);
        context.waitTicks(2);
        context.getInput().pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        context.waitTicks(5);

        // Add a new entry via the list's + button and type into its text field
        context.getInput().setCursorPos(44, 126);
        context.waitTicks(2);
        context.getInput().pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        context.getInput().setCursorPos(88, 252);
        context.waitTicks(2);
        context.getInput().pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        context.waitTicks(3);
        context.getInput().setCursorPos(150, 146);
        context.waitTicks(2);
        context.getInput().pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        context.getInput().setCursorPos(300, 292);
        context.waitTicks(2);
        context.getInput().pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        context.waitTicks(3);
        context.getInput().typeChars("minecraft:stonecutter");
        context.waitTicks(5);
        context.takeScreenshot("config-screen");
        context.setScreen(() -> null);
    }
}
