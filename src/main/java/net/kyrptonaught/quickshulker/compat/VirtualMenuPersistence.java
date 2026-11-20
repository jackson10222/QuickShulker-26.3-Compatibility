/*
 * MIT License
 *
 * Copyright (c) 2019 kyrptonaught
 * Copyright (c) 2024 Haocen2004
 * Copyright (c) 2025 MoRanpcy
 * Copyright (c) 2025 EnderPhantomWing
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */


/*
 * MIT License
 * Copyright (c) 2026 QuickShulker Multi contributors
 */
package net.kyrptonaught.quickshulker.compat;

import net.kyrptonaught.quickshulker.mixin.minecraft.AbstractCraftingMenuAccessor;
import net.kyrptonaught.quickshulker.mixin.minecraft.AnvilMenuAccessor;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

//#if MC>=260300
//$$ import net.minecraft.util.Prediction;
//#endif

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Persists the input slots of optional virtual workbench/anvil menus on the
 * block item itself. The result slot is deliberately not persisted: each menu
 * recalculates it from the saved inputs when it is opened.
 */
public final class VirtualMenuPersistence {
    private static final Map<AbstractContainerMenu, Runnable> CLOSE_SAVERS = new WeakHashMap<>();

    private VirtualMenuPersistence() {
    }

    /** Called by the AbstractContainerMenu mixin immediately before a menu is removed. */
    public static void saveAndForget(AbstractContainerMenu menu) {
        Runnable saver = CLOSE_SAVERS.remove(menu);
        if (saver != null) {
            saver.run();
        }
    }

    public static void attachCrafting(Player player, AbstractContainerMenu menu, ItemStack hostStack) {
        if (!(menu instanceof CraftingMenu)) {
            throw new IllegalArgumentException("Expected a crafting menu, got " + menu.getClass().getName());
        }
        Container input = ((AbstractCraftingMenuAccessor) menu).quickshulker$getCraftSlots();
        attach(player, menu, input, hostStack, 9);
    }

    public static void attachAnvil(Player player, AbstractContainerMenu menu, ItemStack hostStack) {
        if (!(menu instanceof AnvilMenu)) {
            throw new IllegalArgumentException("Expected an anvil menu, got " + menu.getClass().getName());
        }
        Container input = ((AnvilMenuAccessor) menu).quickshulker$getInputSlots();
        attach(player, menu, input, hostStack, 2);
    }

    private static void attach(Player player, AbstractContainerMenu menu, Container input, ItemStack hostStack, int size) {
        NonNullList<ItemStack> saved = NonNullList.withSize(size, ItemStack.EMPTY);
        hostStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(saved);

        // Restore before adding the listener, so initialization doesn't write
        // partially restored data back onto the host item.
        for (int slot = 0; slot < size && slot < input.getContainerSize(); slot++) {
            input.setItem(slot, saved.get(slot).copy());
        }
        input.setChanged();

        Runnable saveAction = () -> save(player, input, hostStack, size);
        CLOSE_SAVERS.put(menu, saveAction);

        menu.addSlotListener(new ContainerListener() {
            @Override
            public void slotChanged(AbstractContainerMenu containerMenu, int slot, ItemStack stack) {
                // CraftingMenu places its result slot before the 3x3 input
                // grid, while ItemCombinerMenu puts its result slot after the
                // inputs. Identify the actual backing container instead of
                // assuming a shared slot-index layout.
                if (slot >= 0 && slot < containerMenu.slots.size()) {
                    Slot changedSlot = containerMenu.slots.get(slot);
                    if (changedSlot.container == input
                            && changedSlot.getContainerSlot() >= 0
                            && changedSlot.getContainerSlot() < size) {
                        saveAction.run();
                    }
                }
            }

            @Override
            public void dataChanged(AbstractContainerMenu containerMenu, int dataSlotIndex, int value) {
                // No persistent menu properties are used by these inputs.
            }
        });

        // Save once after restoration as well, normalizing older/empty data.
        saveAction.run();
    }

    private static void save(Player player, Container input, ItemStack hostStack, int size) {
        NonNullList<ItemStack> contents = NonNullList.withSize(size, ItemStack.EMPTY);
        for (int slot = 0; slot < size && slot < input.getContainerSize(); slot++) {
            contents.set(slot, input.getItem(slot).copy());
        }
        hostStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(contents));

        // Mark the inventory dirty so the changed item component is included
        // in player saves and the active menu can synchronize it next tick.
        player.getInventory().setChanged();
    }

    /**
     * A container-bearing block item must be unique while its menu is open;
     * otherwise a stack of identical blocks would duplicate its stored inputs.
     */
    public static void isolateHostStack(Player player, ItemStack hostStack) {
        if (hostStack.getCount() <= 1) {
            return;
        }

        ItemStack remainder = hostStack.split(hostStack.getCount() - 1);

        // The remainder represents separate, unused blocks and must not inherit
        // the virtual workbench/anvil inventory of the opened block item.
        remainder.remove(DataComponents.CONTAINER);

        //#if MC>=260300
        //$$ player.getInventory().placeItemBackInInventory(remainder, Prediction.SERVER_ONLY);
        //#else
        //$$ player.getInventory().placeItemBackInInventory(remainder, false);
        //#endif
    }
}
