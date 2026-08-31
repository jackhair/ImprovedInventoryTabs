package com.kqp.inventorytabs.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

@Environment(EnvType.CLIENT)
@Mixin(AbstractContainerScreen.class)
public interface HandledScreenAccessor {
    @Accessor
    int getImageWidth();

    @Accessor
    int getImageHeight();

    @Accessor
    int getLeftPos();

    @Accessor
    int getTopPos();
}
