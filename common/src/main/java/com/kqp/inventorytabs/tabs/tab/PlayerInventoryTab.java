package com.kqp.inventorytabs.tabs.tab;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;

/**
 * Tab for the player's inventory.
 */
public class PlayerInventoryTab extends Tab {
    public PlayerInventoryTab() {
        super(getRenderItemStack());
    }

    @Override
    public void open() {
        Minecraft client = Minecraft.getInstance();
        client.setScreen(new InventoryScreen(client.player));
    }

    @Override
    public boolean shouldBeRemoved() {
        return false;
    }

    @Override
    public Component getHoverText() {
        return Component.literal("Inventory");
    }

    @Override
    public String toString() {
        return "PLAYER INVENTORY TAB";
    }

    @Override
    public int getPriority() {
        return 100;
    }

    private static ItemStack getRenderItemStack() {
        ItemStack itemStack = new ItemStack(Items.PLAYER_HEAD);
        itemStack.set(DataComponents.PROFILE,
                new ResolvableProfile(Minecraft.getInstance().player.getGameProfile()));

        return itemStack;
    }
}
