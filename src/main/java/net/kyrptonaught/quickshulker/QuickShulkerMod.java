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

package net.kyrptonaught.quickshulker;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.kyrptonaught.kyrptconfig.config.ConfigManager;
import net.kyrptonaught.quickshulker.api.util.ItemStackInventory;
import net.kyrptonaught.quickshulker.api.util.QuickOpenableRegistry;
import net.kyrptonaught.quickshulker.api.util.QuickShulkerData;
import net.kyrptonaught.quickshulker.api.util.Util;
import net.kyrptonaught.quickshulker.compat.ModIds;
import net.kyrptonaught.quickshulker.compat.ModUtils;
import net.kyrptonaught.quickshulker.compat.ReflectiveMenuFactory;
import net.kyrptonaught.quickshulker.compat.VirtualMenuPersistence;
import net.kyrptonaught.quickshulker.compat.reinfshulker.ReinfshulkerOpenableRegistry;
import net.kyrptonaught.quickshulker.config.ConfigOptions;
import net.kyrptonaught.quickshulker.event.EventListeners;
import net.kyrptonaught.quickshulker.api.MenuFactory;
import net.kyrptonaught.quickshulker.api.RegisterQuickShulker;
import net.kyrptonaught.quickshulker.network.EnderChestS2CSyncPacket;
import net.kyrptonaught.quickshulker.network.OpenInventoryPacket;
import net.kyrptonaught.quickshulker.network.OpenShulkerPacket;
import net.kyrptonaught.quickshulker.network.QuickBundlePacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.level.block.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
//#if MC >= 1.21.2
import net.minecraft.world.InteractionResult;
//#else
//$$ import net.minecraft.world.InteractionResultHolder;
//#endif
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuickShulkerMod implements ModInitializer, RegisterQuickShulker {

    public static final String MOD_ID = "quickshulker";
    public static ConfigManager.SingleConfigManager config = new ConfigManager.SingleConfigManager(MOD_ID, new ConfigOptions());
    public static double lastMouseX, lastMouseY;
    public static Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ConfigOptions getConfig() {
        return (ConfigOptions) config.getConfig();
    }

    @Override
    public void onInitialize() {
        config.load();
        OpenShulkerPacket.registerReceivePacket();
        QuickBundlePacket.registerReceivePacket();
        EventListeners.registerEventListeners();
        LOGGER.info("QuickShulker Multi is loaded.");

        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (!world.isClientSide()) {
                if (QuickShulkerMod.getConfig().rightClickToOpen) {
                    if (Util.isOpenableItem(stack) && Util.canOpenInHand(stack)) {
                        if (hand == InteractionHand.MAIN_HAND)
                            //#if MC >= 1.21.5
                            Util.openItem(player, 0, player.getInventory().getSelectedSlot());
                            //#else
                            //$$ Util.openItem(player, 0, player.getInventory().selected);
                            //#endif
                        else Util.openItem(player, 0, Inventory.SLOT_OFFHAND);

                        //#if MC >= 1.21.2
                        return InteractionResult.SUCCESS_SERVER;
                        //#else
                        //$$ return InteractionResultHolder.success(stack);
                        //#endif
                    }
                }
            }
            //#if MC >= 1.21.2
            return InteractionResult.PASS;
            //#else
            //$$ return InteractionResultHolder.pass(stack);
            //#endif
        });

        PayloadTypeRegistry.playS2C().register(OpenInventoryPacket.OPEN_INV_ID, OpenInventoryPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(EnderChestS2CSyncPacket.S2CEChestContentPacket.S2C_ECHEST_CONTENT_PACKET_ID, EnderChestS2CSyncPacket.S2CEChestContentPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(EnderChestS2CSyncPacket.S2CEChestSlotPacket.S2C_ECHEST_SLOT_PACKET_ID, EnderChestS2CSyncPacket.S2CEChestSlotPacket.CODEC);

        FabricLoader.getInstance().getEntrypoints(MOD_ID, RegisterQuickShulker.class).forEach(RegisterQuickShulker::registerProviders);
    }

    private static ContainerLevelAccess createAccess(Player player) {
        //#if MC >= 1.21.6
        return ContainerLevelAccess.create(player.level(), player.blockPosition());
        //#else
        //$$ return ContainerLevelAccess.create(player.getCommandSenderWorld(), player.blockPosition());
        //#endif
    }

    private void registerMenuBlock(boolean enabled, Class<? extends Block> blockClass, Component title, MenuFactory factory) {
        registerMenuBlock(enabled, blockClass, title, factory, null);
    }

    @FunctionalInterface
    private interface OptionalMenuInitializer {
        void initialize(Player player, AbstractContainerMenu menu, ItemStack hostStack);
    }

    private void registerMenuBlock(boolean enabled, Class<? extends Block> blockClass, Component title,
                                   MenuFactory factory, OptionalMenuInitializer menuInitializer) {
        if (!enabled) return;
        new QuickOpenableRegistry.Builder()
                .setItem(blockClass)
                .ignoreSingleStackCheck(true)
                .setOpenAction((player, stack) -> {
                    if (menuInitializer != null) {
                        VirtualMenuPersistence.isolateHostStack(player, stack);
                    }
                    player.openMenu(new SimpleMenuProvider((i, playerInventory, playerEntity) -> {
                        AbstractContainerMenu menu = factory.create(i, playerInventory, createAccess(playerEntity));
                        if (menuInitializer != null) {
                            menuInitializer.initialize(playerEntity, menu, stack);
                        }
                        return menu;
                    }, title));
                })
                .register();
    }

    /**
     * Registers an optional mod's replacement block while opening the vanilla menu
     * from QuickShulker. The optional mod remains responsible for normal world use
     * of its placed block.
     */
    private void registerOptionalVanillaMenuBlock(String blockClassName, Component title,
                                                  MenuFactory menuFactory,
                                                  OptionalMenuInitializer menuInitializer) {
        try {
            Class<?> blockClass = Class.forName(blockClassName, false, QuickShulkerMod.class.getClassLoader());
            if (!Block.class.isAssignableFrom(blockClass)) {
                throw new IllegalStateException(blockClassName + " is not a Block");
            }

            @SuppressWarnings("unchecked")
            Class<? extends Block> typedBlockClass = (Class<? extends Block>) blockClass;
            registerMenuBlock(true, typedBlockClass, title, menuFactory, menuInitializer);
            LOGGER.info("Registered QuickShulker compatibility using the vanilla menu for {}", blockClassName);
        } catch (ClassNotFoundException exception) {
            LOGGER.warn("Could not register QuickShulker compatibility: block class {} was not found", blockClassName, exception);
        } catch (LinkageError error) {
            LOGGER.warn("Could not link optional compatibility for {}", blockClassName, error);
        }
    }

    private void registerOptionalMenuBlock(String blockClassName, Component title, String menuClassName,
                                           MenuFactory fallbackFactory,
                                           OptionalMenuInitializer menuInitializer) {
        try {
            Class<?> blockClass = Class.forName(blockClassName, false, QuickShulkerMod.class.getClassLoader());
            if (!Block.class.isAssignableFrom(blockClass)) {
                throw new IllegalStateException(blockClassName + " is not a Block");
            }

            @SuppressWarnings("unchecked")
            Class<? extends Block> typedBlockClass = (Class<? extends Block>) blockClass;
            MenuFactory menuFactory;
            try {
                menuFactory = new ReflectiveMenuFactory(menuClassName);
                LOGGER.info("Registered QuickShulker compatibility using {} for {}", menuClassName, blockClassName);
            } catch (ReflectiveOperationException | LinkageError exception) {
                // Keep the replacement block quick-openable even if a future
                // release changes the optional mod's menu implementation.
                LOGGER.warn("Optional menu {} is unavailable; using the vanilla menu for {}", menuClassName, blockClassName, exception);
                menuFactory = fallbackFactory;
            }
            registerMenuBlock(true, typedBlockClass, title, menuFactory, menuInitializer);
        } catch (ClassNotFoundException exception) {
            LOGGER.warn("Could not register QuickShulker compatibility: block class {} was not found", blockClassName, exception);
        } catch (LinkageError error) {
            // An optional mod may be present but incompatible with this game
            // version. Do not let its missing classes prevent QuickShulker loading.
            LOGGER.warn("Could not link optional compatibility for {}", blockClassName, error);
        }
    }

    @Override
    public void registerProviders() {
        if (getConfig().quickShulkerBox)
            new QuickOpenableRegistry.Builder()
                    .setItem(ShulkerBoxBlock.class)
                    .supportsBundleing(true)
                    .getSound((stack, isOpenSound) -> isOpenSound ? SoundEvents.SHULKER_BOX_OPEN : SoundEvents.SHULKER_BOX_CLOSE)
                    .setOpenAction(((player, stack) ->
                            player.openMenu(new SimpleMenuProvider((i, playerInventory, playerEntity) ->
                                    new ShulkerBoxMenu(i, player.getInventory(), new ItemStackInventory(stack, 27)),
                                    stack.getComponents().has(DataComponents.CUSTOM_NAME) ? stack.getHoverName() : Component.translatable("container.shulkerBox")))))
                    .register();

        if (getConfig().quickEChest)
            new QuickOpenableRegistry.Builder(new QuickShulkerData.QuickEnderData())
                    .setItem(EnderChestBlock.class)
                    .supportsBundleing(true)
                    .getSound((stack, isOpenSound) -> isOpenSound ? SoundEvents.ENDER_CHEST_OPEN : SoundEvents.ENDER_CHEST_CLOSE)
                    .ignoreSingleStackCheck(true)
                    .setOpenAction(((player, stack) ->
                            player.openMenu(new SimpleMenuProvider((i, playerInventory, playerEntity) ->
                                    ChestMenu.threeRows(i, playerInventory, player.getEnderChestInventory()), Component.translatable("container.enderchest")))))
                    .register();

        registerMenuBlock(getConfig().quickCraftingTables,  CraftingTableBlock.class,
                Component.translatable("container.crafting"),           CraftingMenu::new);
        registerMenuBlock(getConfig().quickStonecutter,     StonecutterBlock.class,
                Component.translatable("container.stonecutter"),        StonecutterMenu::new);
        registerMenuBlock(getConfig().quickAnvil,           AnvilBlock.class,
                Component.translatable("container.repair"),             AnvilMenu::new);
        registerMenuBlock(getConfig().quickGrindstone,      GrindstoneBlock.class,
                Component.translatable("container.grindstone_title"),   GrindstoneMenu::new);
        registerMenuBlock(getConfig().quickSmithingTable,   SmithingTableBlock.class,
                Component.translatable("container.upgrade"),            SmithingMenu::new);
        registerMenuBlock(getConfig().quickLoom,            LoomBlock.class,
                Component.translatable("container.loom"),               LoomMenu::new);

        // Visual Workbench replaces vanilla crafting tables with its own block.
        // QuickShulker opens a vanilla CraftingMenu for the virtual interface.
        // Do not attach VirtualMenuPersistence or store the crafting grid on the
        // block item: the grid must behave like a normal temporary crafting menu,
        // otherwise its contents can be duplicated across stacked block items.
        // Right-clicking a placed Visual Workbench in the world remains unchanged.
        if (getConfig().quickCraftingTables && ModUtils.isModLoad(ModIds.visualWorkbench)) {
            registerOptionalVanillaMenuBlock(
                    "fuzs.visualworkbench.common.world.level.block.CraftingTableWithInventoryBlock",
                    Component.translatable("container.crafting"),
                    CraftingMenu::new,
                    null);
        }

        // Easy Anvils replaces vanilla anvils with a subclass of AnvilBlock.
        // For QuickShulker's virtual interface, always use the vanilla AnvilMenu
        // and do not persist input slots on the anvil item. This prevents an
        // opened anvil's contents from being copied to every item in a stack.
        // Normal right-click interaction with a placed Easy Anvils block is
        // unchanged and remains handled by Easy Anvils itself.
        if (getConfig().quickAnvil && ModUtils.isModLoad(ModIds.easyAnvils)) {
            registerOptionalVanillaMenuBlock(
                    "fuzs.easyanvils.common.world.level.block.AnvilWithInventoryBlock",
                    Component.translatable("container.repair"),
                    AnvilMenu::new,
                    null);
        }

        //#if MC < 26.1
        if (ModUtils.isModLoad(ModIds.reinfshulker) && QuickShulkerMod.getConfig().quickShulkerBox) {
            ReinfshulkerOpenableRegistry.registerProviders();
        }
        //#endif
    }

}
