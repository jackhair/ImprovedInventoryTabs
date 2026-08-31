package com.kqp.inventorytabs.init;

import com.kqp.inventorytabs.api.TabProviderRegistry;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;

/**
 * Loader-independent mod state and initialization. The per-loader entry
 * points call {@link #init()} and wire up their own event hooks.
 */
public class InventoryTabs {
    public static final String ID = "inventorytabs";
    static ConfigHolder<InventoryTabsConfig> inventoryTabsConfig;

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(ID, path);
    }

    public static void init() {
        inventoryTabsConfig = AutoConfig.register(InventoryTabsConfig.class, GsonConfigSerializer::new);
        inventoryTabsConfig.registerSaveListener((configHolder, config) -> {
            TabProviderRegistry.init("save");
            return InteractionResult.SUCCESS;
        });
    }

    public static InventoryTabsConfig getConfig() {
        return AutoConfig.getConfigHolder(InventoryTabsConfig.class).getConfig();
    }
}
