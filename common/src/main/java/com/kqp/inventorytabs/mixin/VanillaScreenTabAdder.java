package com.kqp.inventorytabs.mixin;

import java.util.HashSet;
import java.util.Set;

import com.kqp.inventorytabs.init.InventoryTabsClient;
import com.kqp.inventorytabs.interf.TabManagerContainer;
import com.kqp.inventorytabs.tabs.TabManager;
import com.kqp.inventorytabs.tabs.tab.SimpleBlockTab;
import com.kqp.inventorytabs.tabs.tab.Tab;
import com.kqp.inventorytabs.util.ChestUtil;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(AbstractContainerScreen.class)
public abstract class VanillaScreenTabAdder extends Screen {
    protected VanillaScreenTabAdder(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void initRestoreStack(CallbackInfo callbackInfo) {
        Minecraft client = Minecraft.getInstance();
        TabManager tabManager = ((TabManagerContainer) client).getTabManager();
        if (tabManager.screenOpenedViaTab()) {
            tabManager.restoreCursorStack(client.gameMode, client.player, ((AbstractContainerScreen<?>) (Object) this).getMenu());
            tabManager.tabOpenedRecently = true; // Preserve value for later
        }
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void initTabRenderer(CallbackInfo callbackInfo) {
        if (InventoryTabsClient.screenSupported(this)) {
            Minecraft client = Minecraft.getInstance();
            TabManager tabManager = ((TabManagerContainer) client).getTabManager();

            tabManager.onScreenOpen((AbstractContainerScreen<?>) (Object) this);

            Tab tabOpened = null;

            if ((Object) this instanceof InventoryScreen) {
                tabOpened = tabManager.tabs.get(0);
            } else if (!tabManager.screenOpenedViaTab()) { // Consumes flag
                // If the screen was NOT opened via tab,
                // check what block player is looking at for context

                if (client.hitResult instanceof BlockHitResult) {
                    BlockHitResult blockHitResult = (BlockHitResult) client.hitResult;
                    BlockPos blockPos = blockHitResult.getBlockPos();

                    Set<BlockPos> matchingBlockPositions = new HashSet<>();
                    matchingBlockPositions.add(blockPos);

                    // For double chests
                    Level world = client.player.level();
                    if (world.getBlockState(blockPos).getBlock() instanceof ChestBlock) {
                        if (ChestUtil.isDouble(world, blockPos)) {
                            matchingBlockPositions.add(ChestUtil.getOtherChestBlockPos(world, blockPos));
                        }
                    }

                    for (int i = 0; i < tabManager.tabs.size(); i++) {
                        Tab tab = tabManager.tabs.get(i);

                        if (tab instanceof SimpleBlockTab) {
                            if (matchingBlockPositions.contains(((SimpleBlockTab) tab).blockPos)) {
                                tabOpened = tab;
                                break;
                            }
                        }
                    }
                }
            }

            if (tabOpened != null) {
                tabManager.onOpenTab(tabOpened);
            }
        }
    }

    // The non-selected tabs draw behind the container: ACS.renderBackground
    // dims the screen, then draws the panel via renderBg — tabs go between.
    @Inject(method = "renderBackground", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderBg(Lnet/minecraft/client/gui/GuiGraphics;FII)V"))
    private void drawBackgroundTabs(GuiGraphics graphics, int mouseX, int mouseY, float delta,
            CallbackInfo callbackInfo) {
        if (InventoryTabsClient.screenSupported(this)) {
            TabManager tabManager = ((TabManagerContainer) Minecraft.getInstance()).getTabManager();

            if (tabManager.getCurrentScreen() == (Object) this) {
                tabManager.tabRenderer.renderBackground(graphics, mouseX, mouseY);
            }
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    protected void drawForegroundTabs(GuiGraphics graphics, int mouseX, int mouseY, float delta,
            CallbackInfo callbackInfo) {
        if (InventoryTabsClient.screenSupported(this)) {
            Minecraft client = Minecraft.getInstance();
            TabManager tabManager = ((TabManagerContainer) client).getTabManager();

            if (tabManager.getCurrentScreen() == (Object) this) {
                tabManager.tabRenderer.renderForeground(graphics, mouseX, mouseY);
                tabManager.tabRenderer.renderHoverTooltips(graphics, mouseX, mouseY);
            }
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (InventoryTabsClient.screenSupported(this)) {
            TabManager tabManager = ((TabManagerContainer) Minecraft.getInstance()).getTabManager();

            if (tabManager.mouseClicked(mouseX, mouseY, button)) {
                callbackInfo.setReturnValue(true);
            }
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (InventoryTabsClient.screenSupported(this)) {
            TabManager tabManager = ((TabManagerContainer) Minecraft.getInstance()).getTabManager();

            if (tabManager.keyPressed(keyCode, scanCode, modifiers)) {
                callbackInfo.setReturnValue(true);
            }
        }
    }

}
