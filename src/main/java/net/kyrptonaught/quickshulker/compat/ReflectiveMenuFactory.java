/*
 * MIT License
 *
 * Copyright (c) 2026 QuickShulker Multi contributors
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

package net.kyrptonaught.quickshulker.compat;

import net.kyrptonaught.quickshulker.QuickShulkerMod;
import net.kyrptonaught.quickshulker.api.MenuFactory;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Creates optional-mod menu implementations without linking QuickShulker against
 * either mod at compile time. Both supported menus expose a public
 * (int, Inventory) constructor intended for menus without a backing block entity.
 */
public final class ReflectiveMenuFactory implements MenuFactory {
    private final String menuClassName;
    private final Constructor<?> constructor;

    public ReflectiveMenuFactory(String menuClassName) throws ReflectiveOperationException {
        this.menuClassName = menuClassName;
        Class<?> menuClass = Class.forName(menuClassName, true, QuickShulkerMod.class.getClassLoader());
        this.constructor = menuClass.getConstructor(int.class, Inventory.class);
    }

    @Override
    public AbstractContainerMenu create(int syncId, Inventory playerInventory, ContainerLevelAccess access) {
        try {
            Object menu = this.constructor.newInstance(syncId, playerInventory);
            if (menu instanceof AbstractContainerMenu containerMenu) {
                return containerMenu;
            }
            throw new IllegalStateException(this.menuClassName + " is not an AbstractContainerMenu");
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            throw new IllegalStateException("Failed to create optional menu " + this.menuClassName, cause);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not instantiate optional menu " + this.menuClassName, exception);
        }
    }
}
