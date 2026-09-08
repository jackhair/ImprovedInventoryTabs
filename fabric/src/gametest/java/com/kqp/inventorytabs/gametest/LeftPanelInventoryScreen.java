package com.kqp.inventorytabs.gametest;

import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.player.Player;
import top.theillusivec4.curios.api.client.ICuriosScreen;

/**
 * Stand-in for Curios' screen, which paints a slot panel left of the
 * container itself rather than adding widgets, so the tabs can't dodge it
 * and must use only the right column.
 */
public class LeftPanelInventoryScreen extends InventoryScreen implements ICuriosScreen {
    public LeftPanelInventoryScreen(Player player) {
        super(player);
    }
}
