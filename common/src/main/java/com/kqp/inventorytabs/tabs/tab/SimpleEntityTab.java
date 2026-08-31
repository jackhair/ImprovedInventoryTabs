package com.kqp.inventorytabs.tabs.tab;

import com.kqp.inventorytabs.tabs.render.TabRenderInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import java.util.Objects;

public class SimpleEntityTab extends Tab {
    public final Vec3 entityPos;
    public final Identifier entityId;
    public final Entity entity;

    public SimpleEntityTab(Entity entity) {
        super(new ItemStack(Items.BARRIER));
        this.entity = entity;
        this.entityPos = entity.position();
        this.entityId = EntityType.getKey(entity.getType());
    }

    @Override
    public void open() {
        LocalPlayer player = Minecraft.getInstance().player;
        Minecraft.getInstance().gameMode.interact(player, entity, new EntityHitResult(entity), player.getUsedItemHand());
    }

    @Override
    public boolean shouldBeRemoved() {
        if (entity.isRemoved()) {
            return true;
        }
        return entityPos.distanceTo(Minecraft.getInstance().player.position()) > 5;
    }

    @Override
    public Component getHoverText() {
        return entity.getName();
    }

    @Override
    public void renderTabIcon(GuiGraphicsExtractor graphics, TabRenderInfo tabRenderInfo, AbstractContainerScreen<?> currentScreen) {
        ItemStack itemStack = getItemStack();
        graphics.item(itemStack, tabRenderInfo.itemX, tabRenderInfo.itemY);
        graphics.itemDecorations(currentScreen.getFont(), itemStack, tabRenderInfo.itemX, tabRenderInfo.itemY);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SimpleEntityTab tab = (SimpleEntityTab) o;
        return Objects.equals(entityId, tab.entityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityId);
    }

    public ItemStack getItemStack() {
        return entity.getPickResult() != null ? entity.getPickResult() : new ItemStack(Items.BARRIER);
    }
}
