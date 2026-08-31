package com.kqp.inventorytabs.tabs.tab;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class InventoryTab extends Tab {
    public final Item itemId;
    public InventoryTab(Item itemId) {
        super(new ItemStack(itemId));
        this.itemId = itemId;
    }

    @Override
    public void open() {
        LocalPlayer player = Minecraft.getInstance().player;
        Level world = Minecraft.getInstance().level;
        Item item = new ItemStack(itemId).getItem();
        item.use(world, player, player.getUsedItemHand());
    }

    @Override
    public boolean shouldBeRemoved() {
        LocalPlayer player = Minecraft.getInstance().player;
        return (player == null || !player.getInventory().contains(new ItemStack(itemId)));
    }

    @Override
    public Component getHoverText() {
        return new ItemStack(itemId).getHoverName();
    }

    @Override
    public String getStableSortKey() {
        return BuiltInRegistries.ITEM.getKey(itemId).toString();
    }
}
