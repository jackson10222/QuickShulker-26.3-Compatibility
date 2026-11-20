# QuickShulker Multi 26.3 compatibility implementation

Scope: **QuickShulker Multi only**. This is an unofficial compatibility adaptation for Minecraft 26.3. Easy Anvils and Visual Workbench are optional dependencies; their source code is not modified, bundled, or redistributed.

## Easy Anvils

- Detects the replacement block class `fuzs.easyanvils.common.world.level.block.AnvilWithInventoryBlock` when Easy Anvils is loaded.
- When that block is quick-opened through QuickShulker, opens the vanilla `AnvilMenu`.
- Does not persist the virtual anvil's input slots on the anvil item. This avoids sharing/copying an opened virtual anvil's contents across a stack of anvil items.
- Normal interaction with a placed Easy Anvils block remains handled by Easy Anvils.

## Visual Workbench

- Detects the replacement block class `fuzs.visualworkbench.common.world.level.block.CraftingTableWithInventoryBlock` when Visual Workbench is loaded.
- When that block is quick-opened through QuickShulker, opens the vanilla `CraftingMenu`.
- Does not persist the virtual crafting grid on the block item; the virtual grid behaves as a temporary vanilla crafting menu.
- Normal interaction with a placed Visual Workbench block remains handled by Visual Workbench.

## Optional-mod isolation

The replacement block classes are looked up by class name at runtime. QuickShulker does not add compile-time dependencies on Easy Anvils or Visual Workbench. If either mod is absent, its compatibility registration is skipped. The compatibility registration only affects QuickShulker's quick-open action; it does not replace either mod's normal world interaction.

## Build naming

For Minecraft 26.3 only, the project version is set to `1.0.0+mc26.3`, producing `quickshulker-1.0.0+mc26.3.jar`. Other Minecraft versions retain the upstream version naming logic.

## Validation status

The source configuration and documentation have been updated. A full Gradle build and in-game test have not been run as part of this archive edit; build success and runtime behavior should be verified in the project's Windows development environment.
