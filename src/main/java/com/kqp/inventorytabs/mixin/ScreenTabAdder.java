package com.kqp.inventorytabs.mixin;

import com.kqp.inventorytabs.init.InventoryTabsClient;
import com.kqp.inventorytabs.interf.TabManagerContainer;
import com.kqp.inventorytabs.tabs.TabManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

/**
 * Draws the non-selected tabs into the background stratum, after the dim/blur
 * but before the container screen draws its own panel texture (subclasses call
 * super.extractBackground() first, so this fires before their own drawing).
 */
@Environment(EnvType.CLIENT)
@Mixin(Screen.class)
public class ScreenTabAdder {
    @Inject(method = "extractBackground", at = @At("TAIL"))
    private void inventorytabs$drawBackgroundTabs(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta,
            CallbackInfo callbackInfo) {
        if ((Object) this instanceof AbstractContainerScreen<?> && InventoryTabsClient.screenSupported((Screen) (Object) this)) {
            Minecraft client = Minecraft.getInstance();
            TabManager tabManager = ((TabManagerContainer) client).getTabManager();

            if (tabManager.getCurrentScreen() == (Object) this) {
                tabManager.tabRenderer.renderBackground(graphics);
            }
        }
    }
}
