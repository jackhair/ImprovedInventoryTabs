package com.kqp.inventorytabs;

import com.kqp.inventorytabs.api.TabProviderRegistry;
import com.kqp.inventorytabs.init.InventoryTabs;
import com.kqp.inventorytabs.init.InventoryTabsClient;
import com.kqp.inventorytabs.init.InventoryTabsConfig;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkConstants;

@Mod(InventoryTabs.ID)
public class InventoryTabsForge {
    public InventoryTabsForge() {
        // Client-only mod: let servers without it accept our clients.
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));

        if (FMLEnvironment.dist != Dist.CLIENT) {
            return;
        }

        InventoryTabs.init();

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::registerKeyMappings);
        MinecraftForge.EVENT_BUS.addListener(this::clientTick);
        MinecraftForge.EVENT_BUS.addListener(this::loggingIn);
        MinecraftForge.EVENT_BUS.addListener(this::tagsUpdated);

        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (minecraft, parent) -> AutoConfig.getConfigScreen(InventoryTabsConfig.class, parent).get()));
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        // Only active while a screen is open, so Tab doesn't conflict with
        // the player-list key. (Fabric needs a mixin for this; Forge has
        // conflict contexts built in.)
        InventoryTabsClient.NEXT_TAB_KEY_BIND.setKeyConflictContext(KeyConflictContext.GUI);
        event.register(InventoryTabsClient.NEXT_TAB_KEY_BIND);
    }

    private void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START && Minecraft.getInstance().level != null) {
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
