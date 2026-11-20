/*
 * MIT License
 * Copyright (c) 2026 QuickShulker Multi contributors
 */
package net.kyrptonaught.quickshulker.mixin.minecraft;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.ItemCombinerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemCombinerMenu.class)
public interface AnvilMenuAccessor {
    @Accessor("inputSlots")
    Container quickshulker$getInputSlots();
}
