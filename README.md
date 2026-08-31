# Improved Inventory Tabs
Adds tabs to access nearby blocks without leaving your inventory. Completely client-side.

<img width="640" height="412" alt="improved-inventory-tabs-small" src="https://github.com/user-attachments/assets/38d18d71-9a87-4333-8ed3-a6fbc98ca8b9" />

Tabs appear in vertical columns along the sides of any container screen, one for each openable block or entity near you — chests, furnaces, crafting tables, barrels, lecterns, shulker boxes, chest minecarts and more. Click a tab (or press **Tab** to cycle, **Shift+Tab** to cycle backwards) to jump straight to that block's screen. When there are more tabs than fit, the last slot becomes a next-page arrow tab.

This is a continuation of Inventory Tabs for **Minecraft 26.2**, rewritten for the modern game and available for **both Fabric and NeoForge** from a shared codebase.

## Installation
Grab the jar for your loader from the [releases page](https://github.com/jackhair/InventoryTabs/releases) and drop it in your `mods` folder.

| | Fabric | NeoForge |
|---|---|---|
| Minecraft | 26.2 | 26.2 |
| Loader | Fabric Loader 0.19.3+ | NeoForge 26.2.0.75+ |
| Required mods | [Fabric API](https://modrinth.com/mod/fabric-api), [Cloth Config](https://modrinth.com/mod/cloth-config) | [Cloth Config](https://modrinth.com/mod/cloth-config) |

Configuration (sight checks, per-block include/exclude lists) is available through Cloth Config — via [Mod Menu](https://modrinth.com/mod/modmenu) on Fabric, or the built-in mod list config button on NeoForge.

## Developers
### Project layout
The mod uses a MultiLoader-style layout: all mod logic lives in `common/` (compiled against vanilla via NeoForm — Minecraft is unobfuscated as of 26.1, so every loader shares Mojang names), with thin `fabric/` and `neoforge/` subprojects providing entry points and event glue.

```
./gradlew build                       # builds both loader jars into */build/libs
./gradlew :fabric:runClient           # dev client (Fabric)
./gradlew :neoforge:runClient         # dev client (NeoForge)
./gradlew :fabric:runClientGameTest   # automated in-game screenshot tests
```

### Adding custom tabs
Everything below lives in `common` and works identically on both loaders.

#### Simple block tabs
A "simple block tab" is opened by interacting with a block. If your block falls under this category, pass it (or its `Identifier`) to `TabProviderRegistry#registerSimpleBlock`. Most blocks with screens are picked up automatically, and players can force-include or exclude blocks via the config.

#### Chest tabs
Chest tabs belong to chests that can double up along the horizontal axis and be blocked by a block above. To register yours, pass the block to `TabProviderRegistry#registerChest`. Chests that do not match vanilla chest behavior should use a simple block tab instead.

#### Custom tabs
For full control, extend the `Tab` class — it defines the tab's icon, hover text, what happens on click (`open`), and when it disappears (`shouldBeRemoved`) — then register a `TabProvider` with `TabProviderRegistry#register`. Providers are asked every tick while a screen is open to populate the available tab list; `BlockTabProvider` is a ready-made base for block-backed tabs that handles range and line-of-sight checks for you (see `EnderChestTabProvider` and `ShulkerBoxTabProvider` for examples).

Tabs are rendered and clicked entirely by the mod's own mixins into `AbstractContainerScreen`, so custom screens generally don't need to do anything — any screen extending it gets tabs automatically.

## Credits
Fourth-generation continuation: the original mod is by cakewhip ([kqpel](https://github.com/kqpel)), continued by LiamMCW, then [Andrew6rant](https://github.com/Andrew6rant/InventoryTabs) through 1.19, and now here for 26.2+. Full history in the [contributor graph](https://github.com/jackhair/InventoryTabs/graphs/contributors). Licensed MIT.
