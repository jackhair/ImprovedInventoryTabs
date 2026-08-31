package com.kqp.inventorytabs.init;

import com.kqp.inventorytabs.interf.TabManagerContainer;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;

public class InventoryTabsClient implements ClientModInitializer {
    public static final KeyMapping NEXT_TAB_KEY_BIND = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "inventorytabs.key.next_tab", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_TAB, KeyMapping.Category.INVENTORY));

    public static boolean serverDoSightCheckFlag = true;

    @Override
    public void onInitializeClient() {
        // Handle state of tab manager
        ClientTickEvents.START_LEVEL_TICK.register(level -> {
            Minecraft client = Minecraft.getInstance();

            if (client.gui.screen() != null) {
                TabManagerContainer tabManagerContainer = (TabManagerContainer) client;

                tabManagerContainer.getTabManager().update();
            }
        });
    }

    public static boolean screenSupported(Screen screen) {
        return (screen instanceof AbstractContainerScreen<?>) && !(screen instanceof CreativeModeInventoryScreen);
    }
}
