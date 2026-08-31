package com.kqp.inventorytabs.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

/**
 * Smoke test for the 26.2 port: joins a world, places some openable blocks
 * next to the player, opens the inventory (which loads
 * AbstractContainerScreen and applies the tab mixins) and screenshots the
 * tabs being rendered.
 */
public class InventoryTabsClientGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();

            singleplayer.getServer().runCommand("setblock 2 -60 0 minecraft:chest");
            singleplayer.getServer().runCommand("setblock 0 -60 2 minecraft:crafting_table");
            singleplayer.getServer().runCommand("setblock -2 -60 0 minecraft:furnace");
            singleplayer.getConnection().waitForClientboundPackets();
            context.waitTicks(5);

            context.getInput().pressKey(options -> options.keyInventory);
            context.waitForScreen(InventoryScreen.class);
            context.waitTicks(20);
            context.takeScreenshot("inventory-tabs-open");
        }
    }
}
