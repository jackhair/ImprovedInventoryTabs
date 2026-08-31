package com.kqp.inventorytabs.tabs.tab;

import com.kqp.inventorytabs.tabs.render.TabRenderInfo;
import com.kqp.inventorytabs.util.ChestUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.kqp.inventorytabs.util.ChestUtil.getOtherChestBlockPos;

/**
 * Tab for chests
 */
public class ChestTab extends SimpleBlockTab {
    ItemStack itemStack;
    public ChestTab(Identifier blockId, BlockPos blockPos) {
        super(blockId, blockPos);
        this.itemStack = new ItemStack(BuiltInRegistries.BLOCK.getValue(blockId));
    }

    @Override
    public boolean shouldBeRemoved() {
        LocalPlayer player = Minecraft.getInstance().player;

        if (ChestBlock.isChestBlockedAt(player.level(), blockPos)) {
            return true;
        }

        return super.shouldBeRemoved();
    }

    @Override
    public Component getHoverText() {
        if (itemStack.has(DataComponents.CUSTOM_NAME)) {
            return itemStack.getHoverName();
        }
        return super.getHoverText();
    }

    @Override
    public void renderTabIcon(GuiGraphicsExtractor graphics, TabRenderInfo tabRenderInfo, AbstractContainerScreen<?> currentScreen) {
        ItemStack itemStack = getItemFrame();
        graphics.item(itemStack, tabRenderInfo.itemX, tabRenderInfo.itemY);
        graphics.itemDecorations(currentScreen.getFont(), itemStack, tabRenderInfo.itemX, tabRenderInfo.itemY);
    }

    public ItemStack getItemFrame() {
        Level world = Minecraft.getInstance().player.level();
        itemStack = new ItemStack(world.getBlockState(blockPos).getBlock());
        BlockPos doubleChestPos = ChestUtil.isDouble(world, blockPos) ? getOtherChestBlockPos(world, blockPos) : blockPos;
        AABB box = AABB.encapsulatingFullBlocks(blockPos, doubleChestPos);
        double x = box.minX;    double y = box.minY;    double z = box.minZ;
        double x1 = box.maxX;   double y1 = box.maxY;   double z1 = box.maxZ;
        List<ItemFrame> list1 = world.getEntitiesOfClass(ItemFrame.class, new AABB(x-0.8, y, z, x1+1.8, y1+0.8, z1+0.8));
        List<ItemFrame> list2 = world.getEntitiesOfClass(ItemFrame.class, new AABB(x, y, z-0.8, x1+0.8, y1+0.8, z1+1.8));
        List<ItemFrame> list3 = world.getEntitiesOfClass(ItemFrame.class, new AABB(x, y-0.8, z, x1+0.8, y1+1.8, z1+0.8));
        List<ItemFrame> list = new ArrayList<>();
        Stream.of(list1, list2, list3).forEach(list::addAll);
        if (!list.isEmpty()) {
            itemStack = list.get(0).getItem();
        }
        return itemStack;
    }
}
