package com.kqp.inventorytabs.tabs.provider;

import com.kqp.inventorytabs.tabs.tab.InventoryTab;
import com.kqp.inventorytabs.tabs.tab.Tab;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class InventoryTabProvider implements TabProvider {
    private static final Set<Identifier> inventoryItems = new HashSet<>();

    @Override
    public void addAvailableTabs(LocalPlayer player, List<Tab> tabs) {
        Set<Item> itemSet = inventoryItems.stream().map(BuiltInRegistries.ITEM::getValue).collect(Collectors.toSet());
        for (Item item : itemSet) {
            if (player.getInventory().contains(new ItemStack(item))) {
                Tab tab = new InventoryTab(item);
                if (tabs.stream().filter(c -> c instanceof InventoryTab).noneMatch(c -> ((InventoryTab) c).itemId == item)) {
                    tabs.add(tab);
                }
            }
        }
    }

    public void addItem(Identifier blockId) {
        inventoryItems.add(blockId);
    }

    public Set<Identifier> getItemIds() {
        return inventoryItems;
    }

    public static Set<Item> getItems() {
        return inventoryItems.stream().map(BuiltInRegistries.ITEM::getValue).collect(Collectors.toSet());
    }

}
