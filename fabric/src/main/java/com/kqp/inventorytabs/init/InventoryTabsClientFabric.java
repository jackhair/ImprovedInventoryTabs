package com.kqp.inventorytabs.init;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

public class InventoryTabsClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(InventoryTabsClient.NEXT_TAB_KEY_BIND);

        ClientTickEvents.START_WORLD_TICK.register(level -> InventoryTabsClient.levelTick());
    }
}
