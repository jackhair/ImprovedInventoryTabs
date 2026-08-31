package com.kqp.inventorytabs.init;

import com.kqp.inventorytabs.interf.TabManagerContainer;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;

/**
 * Loader-independent client state. The key mapping is created here and
 * registered by each loader's entry point; the loaders also call
 * {@link #levelTick()} from their client tick events.
 */
public class InventoryTabsClient {
    public static final KeyMapping NEXT_TAB_KEY_BIND = new KeyMapping(
            "inventorytabs.key.next_tab", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_TAB, "key.categories.inventory");

    public static boolean serverDoSightCheckFlag = true;

    // Handle state of tab manager
    public static void levelTick() {
        Minecraft client = Minecraft.getInstance();

        if (client.screen != null) {
            TabManagerContainer tabManagerContainer = (TabManagerContainer) client;

            tabManagerContainer.getTabManager().update();
        }
    }

    public static boolean screenSupported(Screen screen) {
        return (screen instanceof AbstractContainerScreen<?>) && !(screen instanceof CreativeModeInventoryScreen);
    }
}
