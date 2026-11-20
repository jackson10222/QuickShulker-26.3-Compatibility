/*
 * MIT License
 * Copyright (c) 2026 QuickShulker Multi contributors
 */
package net.kyrptonaught.quickshulker.mixin.minecraft;

import net.minecraft.world.inventory.CraftingContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@Mixin(targets = "net.minecraft.world.inventory.AbstractCraftingMenu")
public interface AbstractCraftingMenuAccessor {
    @Accessor("craftSlots")
    CraftingContainer quickshulker$getCraftSlots();
}
