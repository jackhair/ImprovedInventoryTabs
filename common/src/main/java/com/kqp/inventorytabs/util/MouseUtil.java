package com.kqp.inventorytabs.util;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;

/**
 * Utility class for manipulating the client's mouse position.
 */
public class MouseUtil {
    private static double mouseX = -1D, mouseY = -1D;

    public static void push() {
        mouseX = getMouseX();
        mouseY = getMouseY();
    }

    public static void tryPop() {
        if (mouseX != -1D && mouseY != -1D) {
            InputConstants.grabOrReleaseMouse(Minecraft.getInstance().getWindow(), GLFW.GLFW_CURSOR_NORMAL, mouseX,
                    mouseY);

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
