package com.kqp.inventorytabs.interf;

import com.kqp.inventorytabs.tabs.TabManager;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Interface for holding the tab manager. Gets injected into
 * {@link net.minecraft.client.Minecraft}.
 */
@Environment(EnvType.CLIENT)
public interface TabManagerContainer {
    TabManager getTabManager();
}
