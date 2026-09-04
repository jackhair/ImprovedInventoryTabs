package com.kqp.inventorytabs.tabs.provider;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.kqp.inventorytabs.init.InventoryTabs;
import com.kqp.inventorytabs.tabs.tab.SimpleBlockTab;
import com.kqp.inventorytabs.tabs.tab.Tab;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Provides tabs for simple blocks.
 */
public class SimpleBlockTabProvider extends BlockTabProvider {
    private final Set<Identifier> blockIds = new HashSet<>();
    /**
     * Blocks that were asked for by name (the config's include list or
     * another mod's API call). These skip the inventory/menu check.
     */
    private final Set<Identifier> forcedBlockIds = new HashSet<>();

    public SimpleBlockTabProvider() {
    }

    public void addBlock(Block block) {
        blockIds.add(BuiltInRegistries.BLOCK.getKey(block));
    }

    public void addBlock(Identifier identifier) {
        blockIds.add(identifier);
    }

    /**
     * Registers a block that always gets a tab, whether or not it looks like
     * it has an inventory or menu.
     */
    public void forceBlock(Identifier identifier) {
        blockIds.add(identifier);
        forcedBlockIds.add(identifier);
    }

    public void removeBlock(Block block) {
        removeBlock(BuiltInRegistries.BLOCK.getKey(block));
    }

    public void removeBlock(Identifier identifier) {
        blockIds.remove(identifier);
        forcedBlockIds.remove(identifier);
    }

    public Set<Identifier> getBlockIds() {
        return this.blockIds;
    }

    public Set<Block> getBlocks() {
        return this.blockIds.stream().map(BuiltInRegistries.BLOCK::getValue).collect(Collectors.toSet());
    }

    @Override
    public boolean matches(Level world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(blockState.getBlock());

        if (!blockIds.contains(blockId)) {
            return false;
        }
        if (forcedBlockIds.contains(blockId) || !InventoryTabs.getConfig().onlyBlocksWithMenus) {
            return true;
        }
        return hasInventoryOrMenu(world, pos, blockState);
    }

    /**
     * Every block with a block entity is registered up front, but plenty of
     * them (cables, belts, cogwheels, sculk catalysts...) only use it for
     * ticking or rendering and open nothing when clicked. A tab is only
     * worth showing when the block actually holds an inventory or opens a
     * menu, which is checked against the real block in the world.
     */
    public static boolean hasInventoryOrMenu(Level world, BlockPos pos, BlockState blockState) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof Container || blockEntity instanceof MenuProvider) {
            return true;
        }
        try {
            return blockState.getMenuProvider(world, pos) != null;
        } catch (RuntimeException e) {
            // A mod's menu provider may assume it's running on the server.
            // If it blows up on the client it almost certainly has a menu.
            return true;
        }
    }

    @Override
    public Tab createTab(Level world, BlockPos pos) {
        return new SimpleBlockTab(BuiltInRegistries.BLOCK.getKey(world.getBlockState(pos).getBlock()), pos);
    }
}
