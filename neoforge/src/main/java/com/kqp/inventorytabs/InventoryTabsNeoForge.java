package com.kqp.inventorytabs;

import com.kqp.inventorytabs.api.TabProviderRegistry;
import com.kqp.inventorytabs.init.InventoryTabs;
import com.kqp.inventorytabs.init.InventoryTabsClient;
import com.kqp.inventorytabs.init.InventoryTabsConfig;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

@Mod(value = InventoryTabs.ID, dist = Dist.CLIENT)
public class InventoryTabsNeoForge {
    public InventoryTabsNeoForge(ModContainer container, IEventBus modBus) {
        InventoryTabs.init();

        modBus.addListener(this::registerKeyMappings);
        NeoForge.EVENT_BUS.addListener(this::clientTick);
        NeoForge.EVENT_BUS.addListener(this::loggingIn);
        NeoForge.EVENT_BUS.addListener(this::tagsUpdated);

        container.registerExtensionPoint(IConfigScreenFactory.class,
                (modContainer, parent) -> AutoConfig.getConfigScreen(InventoryTabsConfig.class, parent).get());
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        // Only active while a screen is open, so Tab doesn't conflict with
        // the player-list key. (Fabric needs a mixin for this; NeoForge has
        // conflict contexts built in.)
        InventoryTabsClient.NEXT_TAB_KEY_BIND.setKeyConflictContext(KeyConflictContext.GUI);
        event.register(InventoryTabsClient.NEXT_TAB_KEY_BIND);
    }

    private void clientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().level != null) {
            InventoryTabsClient.levelTick();
        }
    }

    private void loggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        TabProviderRegistry.init("load");
    }

    private void tagsUpdated(TagsUpdatedEvent event) {
        TabProviderRegistry.init("reload");
    }
}
