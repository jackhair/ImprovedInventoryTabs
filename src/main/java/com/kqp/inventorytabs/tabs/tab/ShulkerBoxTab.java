package com.kqp.inventorytabs.tabs.tab;

import com.kqp.inventorytabs.mixin.ShulkerBoxBlockInvoker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Tab for shulker boxes.
 */
public class ShulkerBoxTab extends SimpleBlockTab {
    public ShulkerBoxTab(Identifier blockId, BlockPos blockPos) {
        super(blockId, blockPos);
    }

    @Override
    public boolean shouldBeRemoved() {
        LocalPlayer player = Minecraft.getInstance().player;

        BlockEntity blockEntity = player.level().getBlockEntity(blockPos);

        if (blockEntity instanceof ShulkerBoxBlockEntity) {
            BlockState blockState = player.level().getBlockState(blockPos);

            return !ShulkerBoxBlockInvoker.invokeCanOpen(blockState, player.level(), blockPos,
                    (ShulkerBoxBlockEntity) blockEntity);
        }

        return super.shouldBeRemoved();
    }
}
