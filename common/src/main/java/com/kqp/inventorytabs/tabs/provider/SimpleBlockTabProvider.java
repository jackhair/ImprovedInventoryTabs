package com.kqp.inventorytabs.tabs.provider;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.kqp.inventorytabs.tabs.tab.SimpleBlockTab;
import com.kqp.inventorytabs.tabs.tab.Tab;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Provides tabs for simple blocks.
 */
public class SimpleBlockTabProvider extends BlockTabProvider {
    private final Set<ResourceLocation> blockIds = new HashSet<>();

    public SimpleBlockTabProvider() {
    }

    public void addBlock(Block block) {
        blockIds.add(BuiltInRegistries.BLOCK.getKey(block));
    }

    public void addBlock(ResourceLocation identifier) {
        blockIds.add(identifier);
    }

    public void removeBlock(Block block) {
        blockIds.remove(BuiltInRegistries.BLOCK.getKey(block));
    }

    public void removeBlock(ResourceLocation identifier) {
        blockIds.remove(identifier);
    }

    public Set<ResourceLocation> getBlockIds() {
        return this.blockIds;
    }

    public Set<Block> getBlocks() {
        return this.blockIds.stream().map(id -> BuiltInRegistries.BLOCK.get(id)).collect(Collectors.toSet());
    }

    @Override
    public boolean matches(Level world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);

        return blockIds.contains(BuiltInRegistries.BLOCK.getKey(blockState.getBlock()));
    }

    @Override
    public Tab createTab(Level world, BlockPos pos) {
        return new SimpleBlockTab(BuiltInRegistries.BLOCK.getKey(world.getBlockState(pos).getBlock()), pos);
    }
}
