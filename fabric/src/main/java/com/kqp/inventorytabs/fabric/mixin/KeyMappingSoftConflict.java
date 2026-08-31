package com.kqp.inventorytabs.fabric.mixin;

import java.util.Map;
import java.util.Objects;

import com.kqp.inventorytabs.init.InventoryTabsClient;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * The 'Tab' keybinding conflicts with the multiplayer player list keybind.
 * Vanilla (before 26.x) resolves a key to a single binding, so when our
 * binding wins the slot but no supported screen is open, redirect the press
 * to whichever other binding shares the key. This makes the conflict soft
 * instead of hard.
 */
@Mixin(KeyMapping.class)
public abstract class KeyMappingSoftConflict {
    @Shadow @Final private static Map<String, KeyMapping> ALL;
    @Shadow @Final private static Map<InputConstants.Key, KeyMapping> MAP;

    @Shadow private InputConstants.Key key;
    @Shadow private int clickCount;

    @Shadow public abstract void setDown(boolean down);

    @Inject(method = "click", at = @At("HEAD"), cancellable = true)
    private static void inventorytabs$click(InputConstants.Key key, CallbackInfo ci) {
        KeyMapping alternative = inventorytabs$findAlternative(key);
        if (alternative != null) {
            ((KeyMappingSoftConflict) (Object) alternative).clickCount++;
            ci.cancel();
        }
    }

    @Inject(method = "set", at = @At("HEAD"), cancellable = true)
    private static void inventorytabs$set(InputConstants.Key key, boolean held, CallbackInfo ci) {
        KeyMapping alternative = inventorytabs$findAlternative(key);
        if (alternative != null) {
            alternative.setDown(held);
            ci.cancel();
        }
    }

    @Unique
    private static KeyMapping inventorytabs$findAlternative(InputConstants.Key key) {
        if (MAP.get(key) != InventoryTabsClient.NEXT_TAB_KEY_BIND) {
            return null;
        }

        Screen screen = Minecraft.getInstance().screen;
        if (InventoryTabsClient.screenSupported(screen)) {
            return null;
        }

        for (KeyMapping value : ALL.values()) {
            if (value != InventoryTabsClient.NEXT_TAB_KEY_BIND
                    && Objects.equals(((KeyMappingSoftConflict) (Object) value).key, key)) {
                return value;
            }
        }

        return null;
    }
}
