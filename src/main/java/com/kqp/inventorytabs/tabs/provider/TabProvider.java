package com.kqp.inventorytabs.tabs.provider;

import java.util.List;

import com.kqp.inventorytabs.tabs.tab.Tab;

import net.minecraft.client.player.LocalPlayer;

/**
 * Base interface for exposing tabs to the player.
 */
public interface TabProvider {
    void addAvailableTabs(LocalPlayer player, List<Tab> tabs);
}
