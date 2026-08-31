package com.kqp.inventorytabs.init;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;

public class InventoryTabsClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyMappingHelper.registerKeyMapping(InventoryTabsClient.NEXT_TAB_KEY_BIND);

        ClientTickEvents.START_LEVEL_TICK.register(level -> InventoryTabsClient.levelTick());
    }
}
