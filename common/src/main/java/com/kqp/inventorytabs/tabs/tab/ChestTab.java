package com.kqp.inventorytabs.tabs.tab;

import com.kqp.inventorytabs.tabs.render.TabRenderInfo;
import com.kqp.inventorytabs.util.ChestUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.List;

import static com.kqp.inventorytabs.util.ChestUtil.getOtherChestBlockPos;

/**
 * Tab for chests
 */
public class ChestTab extends SimpleBlockTab {
    ItemStack itemStack;
    public ChestTab(ResourceLocation blockId, BlockPos blockPos) {
        super(blockId, blockPos);
        this.itemStack = new ItemStack(BuiltInRegistries.BLOCK.get(blockId));
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
        if (itemStack.hasCustomHoverName()) {
            return itemStack.getHoverName();
        }
        return super.getHoverText();
    }

    @Override
    public void renderTabIcon(GuiGraphics graphics, TabRenderInfo tabRenderInfo, AbstractContainerScreen<?> currentScreen) {
        ItemStack itemStack = getItemFrame();
        graphics.renderItem(itemStack, tabRenderInfo.itemX, tabRenderInfo.itemY);
        graphics.renderItemDecorations(Minecraft.getInstance().font, itemStack, tabRenderInfo.itemX, tabRenderInfo.itemY);
    }

    public ItemStack getItemFrame() {
        Level world = Minecraft.getInstance().player.level();
        itemStack = new ItemStack(world.getBlockState(blockPos).getBlock());
        BlockPos otherPos = ChestUtil.isDouble(world, blockPos) ? getOtherChestBlockPos(world, blockPos) : blockPos;
        // An item frame hangs in the block in front of the face it's mounted
        // on, so a box one block bigger than the chest reaches every frame
        // that could belong to it. Attachment is then checked exactly (the
        // frame's supporting block must be one of this chest's blocks) so
        // frames on neighbouring chests are never picked up.
        AABB searchBox = new AABB(blockPos).minmax(new AABB(otherPos)).inflate(1.0);
        List<ItemFrame> frames = world.getEntitiesOfClass(ItemFrame.class, searchBox,
                frame -> isMountedOn(frame, blockPos) || isMountedOn(frame, otherPos));
        // Choose deterministically so the icon can't change between screens,
        // and ignore empty frames rather than blanking the tab.
        frames.stream()
                .filter(frame -> !frame.getItem().isEmpty())
                .min(Comparator.comparingLong((ItemFrame frame) -> frame.getPos().asLong())
                        .thenComparingInt(frame -> frame.getDirection().ordinal()))
                .ifPresent(frame -> itemStack = frame.getItem());
        return itemStack;
    }

    private static boolean isMountedOn(ItemFrame frame, BlockPos pos) {
        return frame.getPos().relative(frame.getDirection().getOpposite()).equals(pos);
    }
}
