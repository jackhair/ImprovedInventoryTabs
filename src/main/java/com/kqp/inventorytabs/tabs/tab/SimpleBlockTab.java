package com.kqp.inventorytabs.tabs.tab;

import java.util.Objects;

import com.kqp.inventorytabs.init.InventoryTabs;
import com.kqp.inventorytabs.tabs.provider.BlockTabProvider;
import com.kqp.inventorytabs.util.BlockUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Generic tab for blocks.
 */
public class SimpleBlockTab extends Tab {
    public final Identifier blockId;
    public final BlockPos blockPos;

    public SimpleBlockTab(Identifier blockId, BlockPos blockPos) {
        super(new ItemStack(Minecraft.getInstance().player.level().getBlockState(blockPos).getBlock()));
        this.blockId = blockId;
        this.blockPos = blockPos;
    }

    @Override
    public void open() {
        Minecraft client = Minecraft.getInstance();
        BlockHitResult hitResult;

        if (InventoryTabs.getConfig().doSightChecksFlag) {
            hitResult = BlockUtil.getLineOfSight(blockPos, client.player, 5D);
        } else {
            hitResult = new BlockHitResult(Vec3.atCenterOf(blockPos), Direction.EAST, blockPos, false);
        }

        if (hitResult != null) {
            if (InventoryTabs.getConfig().rotatePlayer) {
                Minecraft.getInstance().player.lookAt(EntityAnchorArgument.Anchor.EYES,
                        Vec3.atCenterOf(blockPos));
            }

            Minecraft.getInstance().gameMode.useItemOn(client.player,
                    InteractionHand.MAIN_HAND, hitResult);
        }
    }

    @Override
    public boolean shouldBeRemoved() {
        LocalPlayer player = Minecraft.getInstance().player;

        if (!BuiltInRegistries.BLOCK.getKey(player.level().getBlockState(blockPos).getBlock()).equals(blockId)) {
            return true;
        }

        if (InventoryTabs.getConfig().doSightChecksFlag) {
            if (BlockUtil.getLineOfSight(blockPos, player, 5D) == null) {
                return true;
            } else {
                return !BlockUtil.inRange(blockPos, player, 5D);
            }
        }
        Vec3 playerHead = player.getEyePosition();

        return Vec3.atCenterOf(blockPos).subtract(playerHead).lengthSqr() > BlockTabProvider.SEARCH_DISTANCE
                * BlockTabProvider.SEARCH_DISTANCE;

    }

    @Override
    public Component getHoverText() {
        Level world = Minecraft.getInstance().player.level();

        BlockEntity blockEntity = world.getBlockEntity(blockPos);

        if (blockEntity instanceof Nameable nameable && nameable.hasCustomName()) {
            return nameable.getCustomName();
        }

        return world.getBlockState(blockPos).getBlock().getName();

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SimpleBlockTab tab = (SimpleBlockTab) o;
        return Objects.equals(blockPos, tab.blockPos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockPos);
    }
}
