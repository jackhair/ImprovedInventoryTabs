package com.kqp.inventorytabs.util;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;

/**
 * Remembers the cursor position while a tab switches screens and puts it back
 * afterwards, so clicking through tabs doesn't jump the mouse around.
 */
public class MouseUtil {
    private static double mouseX = -1D, mouseY = -1D;

    public static void push() {
        mouseX = getMouseX();
        mouseY = getMouseY();
    }

    public static void tryPop() {
        if (mouseX != -1D && mouseY != -1D) {
            InputConstants.releaseMouse(Minecraft.getInstance().getWindow(), mouseX, mouseY);

            mouseX = -1D;
            mouseY = -1D;
        }
    }

    public static double getMouseX() {
        return Minecraft.getInstance().mouseHandler.xpos();
    }

    public static double getMouseY() {
        return Minecraft.getInstance().mouseHandler.ypos();
    }
}
