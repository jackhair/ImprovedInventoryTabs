package com.kqp.inventorytabs.init;

import com.kqp.inventorytabs.api.TabProviderRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class InventoryTabsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        InventoryTabs.init();

        ClientLoginConnectionEvents.INIT.register((handler, client) -> TabProviderRegistry.init("load"));
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> TabProviderRegistry.init("reload"));
    }
}
