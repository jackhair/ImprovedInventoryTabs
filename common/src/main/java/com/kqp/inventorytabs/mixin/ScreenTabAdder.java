package com.kqp.inventorytabs.mixin;

import com.kqp.inventorytabs.init.InventoryTabsClient;
import com.kqp.inventorytabs.interf.TabManagerContainer;
import com.kqp.inventorytabs.tabs.TabManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

/**
 * Draws the non-selected tabs behind the container. On 1.20.1 every
 * container screen calls Screen.renderBackground itself right before
 * drawing its panel texture (some inside renderBg), so a TAIL hook here
 * lands after the dim and before the panel on all of them.
 */
@Mixin(Screen.class)
public class ScreenTabAdder {
    @Inject(method = "renderBackground(Lnet/minecraft/client/gui/GuiGraphics;)V", at = @At("TAIL"))
    private void inventorytabs$drawBackgroundTabs(GuiGraphics graphics, CallbackInfo callbackInfo) {
        if ((Object) this instanceof AbstractContainerScreen<?> && InventoryTabsClient.screenSupported((Screen) (Object) this)) {
            Minecraft client = Minecraft.getInstance();
            TabManager tabManager = ((TabManagerContainer) client).getTabManager();

            if (tabManager.getCurrentScreen() == (Object) this) {
                double mouseX = client.mouseHandler.xpos() * client.getWindow().getGuiScaledWidth()
                        / client.getWindow().getScreenWidth();
                double mouseY = client.mouseHandler.ypos() * client.getWindow().getGuiScaledHeight()
                        / client.getWindow().getScreenHeight();
                tabManager.tabRenderer.renderBackground(graphics, mouseX, mouseY);
            }
        }
    }
}
