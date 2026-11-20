/*
 * MIT License
 * Copyright (c) 2026 QuickShulker Multi contributors
 */
package net.kyrptonaught.quickshulker.mixin.minecraft;

import net.kyrptonaught.quickshulker.compat.VirtualMenuPersistence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
    @Inject(method = "removed", at = @At("HEAD"))
    private void quickshulker$saveVirtualBlockInventory(Player player, CallbackInfo ci) {
        VirtualMenuPersistence.saveAndForget((AbstractContainerMenu) (Object) this);
    }
}
