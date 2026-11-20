# GTNH-fork Galacticraft — non-GTNH regression audit & restoration plan

Audited against:
- **Upstream HEAD at audit time:** `5c02df91a` (`GTNewHorizons/Galacticraft` master), tag
  **`3.4.32-GTNH`**. The fork is now rebased onto **`61f55046d`** = tag **`3.5.1-GTNH`**; the deltas
  that affect this document are noted inline (§3.9 in particular) and in §8.
- **Baseline (last upstream commit before the GTNH fork):** `79be38a2b` (2017-12-01, "Update
  GCPlayerHandler.java") — the direct parent of the first GTNH commit `52ea597d1`
- **347 commits** exist between the fork point and HEAD

Everything below was verified by a **whitespace/format-insensitive recipe-call diff** (the GTNH tree
was reformatted wholesale with spotless, so plain `git diff` is pure noise) plus a per-commit
`git log -S` trace for each removal. Nothing here is inferred from changelogs.

---

## 0. TL;DR

Galacticraft's situation is **better and worse** than Railcraft's:

**Better:** most of the recipe set *is* correctly gated. `RecipeManagerGC`, `RecipeManagerMars`,
`RecipeManagerAsteroids`, `GCItems`, `AsteroidsItems` and `MarsModule` all wrap the vanilla GC
recipes / ore-dict entries in `if (!Loader.isModLoaded("dreamcraft"))`. In a pack with **no
`dreamcraft`**, that content comes back.

**Worse:** seven whole recipe families were **deleted outright** — present in *neither* branch — and
they are exactly the progression spine of the mod:

| # | Removed content | Replacement lives in |
|---|---|---|
| 1 | **NASA Workbench** crafting recipe | GTNH coremod (per #89) |
| 2 | **Oxygen tanks**: Medium + Heavy (and Light's material reworked) | *nothing known* — `3cfded1b6` simply dropped them |
| 3 | **Tier-1 rocket** NASA-bench recipes (basic + 7 cargo variants) | GTNH coremod (per #89) |
| 4 | **Tier-2 rocket** + **Cargo Rocket** NASA-bench recipes | GTNH coremod (per #89) |
| 4b | **Tier-3 rocket** bench recipes (basic + 7 cargo variants) | GTNH coremod (per #89) |
| 5 | **Moon Buggy** bench recipes (basic + 7 cargo variants) | GTNH coremod (per #89) — *and the bench GUI was redesigned* |
| 6 | **Astro Miner** NASA-bench recipe | GTNH coremod (per #89) — *and the bench GUI was redesigned* |

Plus NEI pages that are registered but whose display maps are **never populated** (§3.9), so every
rocket/bench page renders as an empty box, and five **mod-integration features** dropped
(`CraftGuide`, MFR Slimelings, Mekanism energy/gas, BuildCraft MJ, version checker).

**And the gate is keyed to the wrong mod.** Every recipe gate tests `dreamcraft` (the GTNH
*pack* mod), while "GregTech is installed" is a different id — and GC uses *three* different ones
(`dreamcraft`, `gregtech`, `gregtech_nh`). That is the structural mistake worth fixing in the fork:

> A pack with **GregTech installed but no `dreamcraft`** runs the GTNH code path for every gate
> *except* the recipe gates, and the deleted content above is gone in **both** paths.

Result for a vanilla-ish pack: you can build the NASA Workbench only via `/give`, the Tier-1 rocket
cannot be assembled at all, so the Moon is unreachable. Moon Buggy, Cargo Rocket and Astro Miner are
uncraftable. Only the Light oxygen tank survives.

---

## 1. Method

Because of the reformat, the audit used a **recipe-call extractor** rather than a diff:

1. For every `src/main/java/**/*.java` present in both revisions, extract every recipe/registry
   call invocation (`RecipeUtil.addRecipe`, `addRocketBenchRecipe`, `addBuggyBenchRecipe`,
   `addRocketBenchT2Recipe`, `adCargoRocketRecipe`, `addAstroMinerRecipe`, `CompressorRecipes.*`,
   `CircuitFabricatorRecipes.*`, `FurnaceRecipes.*`, …) with balanced-paren capture, strip comments,
   collapse whitespace.
2. Set-diff baseline vs HEAD. That yields **exactly 24 distinct** removed recipe calls (plus a
   number of `DataWatcher.addObject` false positives, which are excluded). Because this is a *set*,
   it collapses identical duplicate calls — so the per-family **call counts** in §3 were recounted
   separately with `grep -c`. Verified totals, **44 calls**: NASA Workbench 1, oxygen tanks 3,
   T1 rocket **8**, Moon Buggy **8**, T2 rocket **8**, Cargo Rocket **7**, T3 rocket **8**,
   Astro Miner 1.

   All six `GalacticraftRegistry.add*Recipe` entry points (`addT1RocketRecipe`, `addT2RocketRecipe`,
   `addT3RocketRecipe`, `addCargoRocketRecipe`, `addMoonBuggyRecipe`, `addAstroMinerRecipe`) are
   covered by the extractor. An earlier pass missed `addT3RocketRecipe`, which is why the Tier-3
   rocket is listed below &mdash; the recipe lived in `AsteroidsModule`, not in
   `RecipeManagerAsteroids`.
3. Confirm each hit with `git log -S<distinctive token> -- <file>` to get the commit and PR.
4. Separately diff the NEI `IConfigureNEI` classes (handler registrations are not recipe calls).
5. Separately diff the `Container*`/`Slot*` bench classes, comparing slot counts.

The result is the complete list in §3. Nothing outside that list was removed.

---

## 2. The gating model GTNH actually used (and why it misfires)

GTNH introduced three distinct "is this GTNH?" tests, and they are **not** the same mod:

| Test | Constant / literal | Used for |
|---|---|---|
| `dreamcraft` | `Constants.MOD_ID_NEW_HORIZONS_CORE_MOD` (`Constants.java:14`) | **every recipe gate** |
| `gregtech` | `Constants.MOD_ID_GREGTECH` (`Constants.java:13`) | `GameRegistry.findItem("gregtech", "gt.metaitem.01")` slot whitelists |
| `gregtech_nh` | literal in `CompatibilityManager.isGT5uNHLoaded()` (`CompatibilityManager.java:137`) | GT ore-block reflection, Astro Miner GT-ore guard |

`dependencies.gradle` also hard-depends on `GT5-Unofficial` (`implementation`), and `@Mod`'s
`dependencies` string lists **neither** `gregtech` nor `dreamcraft` — so GC is *intended* to run
without GT, and does, but on a hollowed-out recipe set.

Note the asymmetry that this creates:

- `dreamcraft` present → vanilla GC recipes suppressed, coremod supplies GT-flavoured ones. Fine.
- `dreamcraft` absent, `gregtech` present → vanilla GC recipes **are** registered (good), but the
  coremod-only content in §3 is still missing, and the GT-shaped bench slot whitelists in §4 are
  dead slots.
- Both absent → same; NASA Workbench / rockets / buggy / tanks simply do not exist.

**Conclusion:** the recipe gates are not *wrong*, they're just **incomplete** — they wrap the
survivors and leave the deleted content unwrapped entirely. They were also bolted on *late*: the
recipe families in §3 were already commented out in 2020 and hard-deleted in 2024, and the
`!dreamcraft` gate did not exist until 2025 (see the appendix timeline).

---

## 3. Ungated removals — the complete list

### 3.1 CRITICAL — NASA Workbench has no crafting recipe

- **Commits:** `459cfc415` *"removed recipes handled by GalaxySpace"* (2020-08-23) commented it out
  behind `//Handled by Galaxy Space`; `697f953cd` (#89, 2024-07-15) and `8405381d2` (#98,
  2025-01-04) deleted the commented block.
- **File:** `core/recipe/RecipeManagerGC.java` (`addUniversalRecipes`)
- **Effect:** `GCBlocks.nasaWorkbench` is registered, obtainable only in creative/`/give`, and is the
  **only** block that can assemble a rocket. No workbench ⇒ no Tier-1 rocket ⇒ **the Moon is
  unreachable** in any non-GTNH pack.
- **Upstream recipe (verified from `79be38a2b`):**

  ```
  XZX        X = compressedSteel      Z = crafting_table
  UWU        U = lever                W = waferAdvanced
  YVY        Y = compressedSteel      V = redstone_torch
  ```

### 3.2 CRITICAL — Oxygen tank tiers removed

- **Commit:** `3cfded1b6` *"fix(Galaticraft)oxy Tank recipes / change or remove oxygen tank
  recipes"* (2020-07-31, DreamMasterXXL). Not gated, not commented with a rationale.
- **File:** `core/recipe/RecipeManagerGC.java`
- **Removed (complete, from baseline):**

  | Tank | Shape | Canister | Metal | Wool |
  |---|---|---|---|---|
  | Light | `"Z","X","Y"` | tin (meta 0) | `compressedCopper` | lime (meta 5) |
  | Medium | `"ZZ","XX","YY"` | tin (meta 0) | `compressedTin` | orange (meta 1) |
  | Heavy | `"ZZZ","XXX","YYY"` | tin (meta 0) | `compressedSteel` | red (meta 14) |

- **Added instead (unconditional, 2 variants):** Light only,
  `"YZY","YXY","YYY"` with `Y = compressedAluminum`, `Z = oxygenPipe`, canister meta 0 and meta 1.
- **Effect:** a clean 3-tier ladder became a single tier. Medium and Heavy are **uncraftable**, so
  the player is stuck with the light tank's small capacity — a hard gate on longer trips.
- **Note:** `oxTankSuperHeavy` / `oxTankUltraHeavy` are GTNH-only additions; there is no upstream
  recipe to restore for them.

### 3.3 CRITICAL — Tier-1 rocket bench recipes deleted

- **Commits:** `df346caa0` *"change Moon Buggy, Cargo Rocket and Astro Miner recipes"*
  (2020-08-31) replaced them with GT-ingredient variants; `459cfc415` (2020-08-23)
  **commented the vanilla Tier-1 rocket and Moon Buggy recipe blocks out in one `/* */`**;
  `697f953cd` *"Move gregified recipes from GC to coremod (#89)"* (2024-07-15)
  **deleted them entirely**, leaving a bare `// Handled by GalaxySpace` comment.
- **File:** `core/recipe/RecipeManagerGC.java`
- **Removed:** all **8** `RecipeUtil.addRocketBenchRecipe` calls — `rocketTier1` metas 0–3
  (basic + the 7 chest/cargo permutations).
- **Upstream layout (verified):** slot 1 nose cone; 2–9 `heavyPlatingTier1`; 10,11 fins;
  12 `rocketEngine` meta 0; 13,14 fins; 15–17 cargo chests (meta 0 = none → `rocketTier1:0`,
  1 chest → `:1`, 2 → `:2`, 3 → `:3`).
- **Effect:** Tier-1 rocket is unassemblable. Even with the NASA Workbench recipe restored (§3.1),
  the rocket bench is empty.

### 3.4 CRITICAL — Tier-2 rocket + Cargo Rocket bench recipes deleted

- **Commits:** `459cfc415` (commented out), `697f953cd` (#89, deleted)
- **File:** `planets/mars/recipe/RecipeManagerMars.java`
- **Removed:** 8 × `MarsUtil.addRocketBenchT2Recipe` (`MarsItems.spaceship` metas 0–3) and
  7 × `MarsUtil.adCargoRocketRecipe` (metas 11, 12, 13).
- **Upstream layout (T2):** slot 1 nose cone; 2–11 `MarsItems.marsItemBasic` meta 3 (reinforced
  plate T2); 12 booster; 13,14 fins; 15 `rocketEngine` meta 0; 16 booster; 17,18 fins;
  19–21 cargo chests.
- **Effect:** Mars is unreachable even if Tier-1 is restored.

### 3.5 CRITICAL — Moon Buggy bench recipes deleted *and the bench redesigned*

- **Commits:** `df346caa0` (gregified), `459cfc415` (commented), `697f953cd` (#89, deleted)
- **Files:** `core/recipe/RecipeManagerGC.java`, `core/inventory/ContainerBuggyBench.java`,
  `core/inventory/SlotBuggyBench.java`, `core/inventory/InventoryBuggyBench.java`,
  `core/client/gui/container/GuiSchematicBuggy.java`
- **Removed:** 8 × `RecipeUtil.addBuggyBenchRecipe` (`GCItems.buggy` metas 0–3).
- **Structural change (this is the hard part):** the bench was rewritten, and so was its backing
  inventory (`InventoryBuggyBench`: `new ItemStack[32]` → `new ItemStack[36]`):

  | | Baseline | HEAD |
  |---|---|---|
  | Craft matrix | `ItemStack[32]` | `ItemStack[36]` |
  | Container slots | 1 output + **19** craft slots (index 1–19) | 1 output + **35** craft slots (index 1–35) |
  | Body | 4×3 loop (`partBuggy` / `heavyPlatingTier1`) | explicit per-slot coords (44/62/80/98 × 19…109) |
  | Middle | 2×2 wheel loop at ±72 px | 8 "rod" slots (GT stainless **sticks**) |
  | Add-ons | 3 slots (meta 0–3 = cargo tiers) | **1** slot (index 35) |

- **Upstream layout (verified):** slot 1 = `basicItem` meta 19 (frequency module);
  2 = `partBuggy` meta 1 (seat); 3–6 = `partBuggy` meta 0 (wheels); 7–16 = `heavyPlatingTier1`;
  17–19 = `partBuggy` meta 2 (storage) → cargo metas 0–3.
- **Effect:** the buggy is uncraftable **and** the GUI no longer has the slots a vanilla recipe would
  need. Restoring recipes alone is not enough — the container/gui/slot triple must be restored too.

### 3.6 CRITICAL — Tier-3 rocket bench recipes deleted

- **Commits:** `459cfc415` (commented out), `697f953cd` (#89, deleted)
- **File:** `planets/asteroids/AsteroidsModule.java` — 73 lines removed from the planet
  registration method, immediately above the Astro Miner block
- **Removed:** all **8** `GalacticraftRegistry.addT3RocketRecipe` calls
  (`AsteroidsItems.tier3Rocket` metas 0&ndash;3).
- **Upstream layout (verified):** slot 1 `heavyNoseCone`; 2&ndash;11 `AsteroidsItems.basicItem`
  meta 0 (heavy plating T3); 12 booster; 13,14 T3 fins (meta 2); 15 T3 engine (meta 1);
  16 booster; 17,18 T3 fins; 19&ndash;21 cargo chests.
- **Effect:** the Asteroids are only reachable with a Tier-3 rocket, so this gates an entire
  dimension. Unlike the Moon Buggy and Astro Miner, **no GUI change is needed** here: the
  `ContainerSchematicTier3Rocket` layout is still the vanilla one and
  `SlotSchematicTier3Rocket` still whitelists exactly these parts &mdash; only
  `RocketT3RecipeHandler` registration and the NEI map were lost on top.

### 3.7 CRITICAL — Astro Miner recipe deleted *and the bench redesigned*

- **Commits:** `459cfc415` (commented out), `697f953cd` (#89, deleted)
- **Files:** `planets/asteroids/AsteroidsModule.java` (the 73-line
  `GalacticraftRegistry.addAstroMinerRecipe(...)` block), plus
  `planets/asteroids/inventory/ContainerSchematicAstroMiner.java`,
  `SlotSchematicAstroMiner.java`, `InventorySchematicAstroMiner.java`,
  `client/gui/GuiSchematicAstroMiner.java`
- **Removed:** the single `addAstroMinerRecipe(new NasaWorkbenchRecipe(new ItemStack(AsteroidsItems.astroMiner, 1, 0), input))`.
- **Structural change:** the backing inventory went from `new ItemStack[15]` to
  `new ItemStack[30]` and the container from 7 to 16 `addSlotToContainer` call sites. Slot
  *whitelists* now hard-require
  `GameRegistry.findItem("gregtech", "gt.metaitem.01")` meta `32603` for slots 28–29 **with no
  `dreamcraft` guard at all** (unlike `SlotBuggyBench`).
- **Effect:** Astro Miner uncraftable, and the schematic GUI has 9 slots that accept nothing.

### 3.8 Cargo Rocket bench — layout moved, recipe must follow the *current* layout

- **Commits:** `df346caa0` (layout), `d18e22fa5` (chest recipes reintegrated), `697f953cd` (recipes
  deleted)
- **Files:** `planets/mars/inventory/ContainerSchematicCargoRocket.java`, `SlotSchematicCargoRocket`,
  `InventorySchematicCargoRocket` (17 → 22), `GuiSchematicCargoRocket`
- **What changed:** the **2017** baseline container used slots 1–16 (nose cone 1, body 3–8, fins
  9/10/12/13, engine 11, add-ons 14–16). GTNH moved it to the modern layout: slot 1 advanced wafer,
  2/3–5/6 GalaxySpace gear, 7 nose cone, 8–15 reinforced plates, 16 engine, 17–20 fins, 21 chest.
  That modern layout is **still in HEAD** and `SlotSchematicCargoRocket.isItemValid` still whitelists
  exactly those parts.
- **Consequence:** do **not** restore the 2017 recipe against this container — it will not match.
  Restore the recipe as it existed at `697f953cd^`, which is written for the current layout:
  `1` = advanced wafer, `7` = nose cone, `8–15` = **eight** reinforced plates T2, `16` = engine
  (meta 0), `17–20` = fins, `21` = chest; chest meta 3 → `spaceship:11`, 0 → `:12`, 1 → `:13`.
- **Effect:** unlike the buggy and astro miner benches, **the cargo rocket bench GUI needs no
  dual-layout work** — only the three recipes have to come back.

### 3.9 NEI: pages registered but empty (maps never populated)

GTNH removed NEI integration for content it believed the coremod/`GalaxySpace` would supply, then
partially put the handler registrations back in `ff4c5c87b` ("fix nei recipe handler", #151,
2026-10-13). **The display maps were never restored in either pass**, so today the pages exist and
list nothing.

| File | Handler / data | Status |
|---|---|---|
| `core/nei/NEIGalacticraftConfig` | `RocketT1RecipeHandler` + map | registered (when GalaxySpace absent) since #151; **map never populated** |
| `core/nei/NEIGalacticraftConfig` | `BuggyRecipeHandler` + map | registered; **map never populated** |
| `core/nei/NEIGalacticraftConfig` | `IngotCompressorRecipeHandler` | still commented, `// Not used in GTNH` (`35a89e610`) |
| `core/nei/NEIGalacticraftConfig` | `ElectricIngotCompressorRecipeHandler` | still commented, `// Not used in GTNH` |
| `planets/mars/nei/NEIGalacticraftMarsConfig` | `RocketT2RecipeHandler` + map | registered since #151; **map never populated** |
| `planets/mars/nei/NEIGalacticraftMarsConfig` | `CargoRocketRecipeHandler` + map | registered since #151; **map never populated** |
| `planets/mars/nei/NEIGalacticraftMarsConfig` | `GasLiquefierRecipeHandler`, `MethaneSynthesizerRecipeHandler` | still absent (`697f953cd`) |
| `planets/asteroids/nei/NEIGalacticraftAsteroidsConfig` | `RocketT3RecipeHandler` + map | registered since #151; **map never populated** |
| `planets/asteroids/nei/NEIGalacticraftAsteroidsConfig` | `AstroMinerRecipeHandler` + map | registered since #151; **map never populated** |

Mechanically: `NEIGalacticraftConfig.registerRocketBenchRecipe` / `registerBuggyBenchRecipe` /
`registerCargoBenchRecipe` / `registerAstroMinerRecipe` have **no callers anywhere in the tree**. The
handlers read static maps that stay empty forever.

Two further notes:

- The compressor maps *are* filled — `NEIGalacticraftConfig.registerRecipes()` calls
  `addIngotCompressorRecipes()`, which walks the live `CompressorRecipes` list. Only the handler
  registration is missing, so those two machines work but have no NEI page.
- `registerRecipes()` is also where the Refinery and Circuit Fabricator display data come from, so
  those pages are fine.

- **Effect:** every NASA-bench and machine recipe page renders as an empty box. Exactly the symptom
  `FuzziTweaks/.../mixins/late/galacticraft/NEIGalacticraftConfigMixin.java` works around today.

> **Restoration note (implemented):** because upstream now registers the handlers itself, the fork
> only has to *populate the maps* — `LegacyRocketBenchNei.populate()` and the per-planet
> `addLegacyBenchRecipes()` / `addLegacyRocketBenchRecipes()` build them from the same variant lists
> the recipes are registered from. Registering the handlers as well would double every page.

### 3.10 Mod-integration features dropped

| Feature | Removed in | Notes |
|---|---|---|
| **CraftGuide** integration (`core/recipe/craftguide/CraftGuideIntegration`, `CraftGuideCompressorRecipes`, planets equivalents) | `52ea597d1` (2020-05-06) | zero `CraftGuide` references remain anywhere in the tree |
| **MineFactory Reloaded** Slimeling spawn handler (`planets/mars/entities/MFRSpawnHandlerSlimeling`) | `52ea597d1` | MFR Slimelings no longer spawn/behave on Mars |
| **Mekanism** energy conversion + oxygen-as-gas | `52ea597d1` + `7872f1ca7` | no Mekanism API on the classpath; `isMekanismLoaded()` still compiles but every Mekanism conduit path was gutted |
| **BuildCraft MJ** conduction | `52ea597d1` | `TileBaseUniversalConductor.initBC()` is now `{}`; `demandedEnergyUnits`, `injectEnergyUnits`, `getPowerReceiver`, `doWork` deleted |
| **Update/version checker** (`util/ThreadVersionCheck`, `ThreadRequirementMissing`) | `39f131b90` (2020-05-06) | intentional, harmless |
| **`/gchelp` command** (`command/CommandGCHelp`) | `e3026156e` (2025-08-30, "Remove old wiki references") | harmless |

These are lower priority than §3.1–3.9 but they *are* ungated feature removals and belong in the
list. BuildCraft/IC2 energy is only partially affected — `receiveEnergy`/`extractEnergy` (RF) and
the IC2 `acceptsEnergyFrom`/`emitsEnergyTo` signatures survive, and
`ElectricItemManagerIC2_1710` is still present (`7872f1ca7` was an FML-soft-dependency refactor, not
a feature removal).

### 3.11 False positives (checked, *not* removed)

- Asteroid `slime_ball` recipe (`RecipeManagerAsteroids`) — still present, only reformatted.
- `EntityBuggy` / `EntityFlag` / `EntitySlimeling` `DataWatcher.addObject` — extractor noise.
- `EntityBubble` / `ModelBubble` / `RenderBubble` — deleted as genuinely dead code (`df135db31`).
- IC2 support, oxygen network (`core/oxygen/*`), Space Station recipes, T1/T2/T3 *schematic page*
  registration — all intact.

---

## 4. Gate-quality bugs (must fix even if nothing is restored)

### 4.1 `SlotBuggyBench` leaves 12 dead slots outside GTNH

`core/inventory/SlotBuggyBench.java:72,77`:

```java
} else if (this.index >= 8 && this.index <= 11 && Loader.isModLoaded(Constants.MOD_ID_NEW_HORIZONS_CORE_MOD)) {
    return itemStack.getItem() == GameRegistry.findItem(Constants.MOD_ID_GREGTECH, "gt.metaitem.01")
            && itemStack.getItemDamage() == 23306;
...
} else if (this.index >= 17 && this.index <= 24 && Loader.isModLoaded(Constants.MOD_ID_NEW_HORIZONS_CORE_MOD)) {
    return itemStack.getItem() == GameRegistry.findItem(Constants.MOD_ID_GREGTECH, "gt.metaitem.01")
            && itemStack.getItemDamage() == 27306;
```

Adding `&& dreamcraft` did **not** restore a vanilla branch — it turned 12 GT slots into slots that
accept **nothing** without `dreamcraft`. `isItemValid` returns `false` and there is no `else` path.

### 4.2 `SlotSchematicAstroMiner` requires a GT item unconditionally

`planets/asteroids/inventory/SlotSchematicAstroMiner.java:86`:

```java
case 28, 29 -> itemStack.getItem() == GameRegistry.findItem(Constants.MOD_ID_GREGTECH, "gt.metaitem.01")
        && itemStack.getItemDamage() == 32603;
```

No `dreamcraft` guard at all (so no `ClassCastException` risk, but the slots are simply dead).
`GameRegistry.findItem` returns `null` when GT is absent, so the comparison is always `false`.

### 4.3 Three different "is GT here" ids

`dreamcraft` / `gregtech` / `gregtech_nh` (see §2). Any fork should normalise this to one helper.
Note `CompatibilityManager.isGT5uNHLoaded()` tests `gregtech_nh`, which is **GTNH's** GT5-Unofficial
mod id — a plain GT5-Unofficial install uses `gregtech` and will still fail that check.

---

## 5. Recommended architecture for this fork

Same ground rules as `MrFuzzihead/Railcraft`'s `gtnh-compat` branch:

### 5.1 Ground rule

> **Never delete, comment out, or rewrite GTNH's lines. Wrap them and add an `else` path.**

Add the legacy code in new files, call it from an `else`/`!isGTNH` branch, and leave GTNH's blocks
byte-identical so `git merge upstream/master` stays a trivial reconcile.

### 5.2 Fix the gate first — one predicate, three states

`dreamcraft` is a reasonable *proxy* for "the GTNH pack", but the fork should decide explicitly:

```java
// core/util/CompatGates.java  (new file)
public final class CompatGates {
    private static Boolean legacy;      // -1 auto / 0 force-legacy / 1 force-gtnh via config

    public static boolean isGTNHPack() {
        return Loader.isModLoaded(Constants.MOD_ID_NEW_HORIZONS_CORE_MOD);
    }
    public static boolean isGregTechLoaded() {
        return Loader.isModLoaded(Constants.MOD_ID_GREGTECH)
            || Loader.isModLoaded("gregtech_nh");
    }
    /** True when the *vanilla Galacticraft* content should be registered. */
    public static boolean useLegacyContent() {
        if (legacy != null) return legacy;
        return !isGTNHPack();           // default: unchanged from upstream
    }
    /** True when GT-shaped slot whitelists / items are valid. */
    public static boolean useGregTechItems() {
        return isGregTechLoaded();
    }
}
```

Then:
- recipe gates → `CompatGates.useLegacyContent()` (behaviourally identical to today by default);
- slot whitelists → `CompatGates.useGregTechItems()` instead of `dreamcraft`, so a GT-but-not-GTNH
  pack gets the GT slots filled rather than empty;
- add a config option `auto | forceLegacy | forceGTNH` so both paths can be tested without
  installing/removing `dreamcraft`.

### 5.3 Restore the deleted content in dedicated helper classes

```
micdoodle8/mods/galacticraft/core/recipe/legacy/
    LegacyNasaWorkbenchRecipes.java   // nasaWorkbench block recipe
    LegacyOxygenTankRecipes.java      // Light(copper)/Medium(tin)/Heavy(steel) ladder
    LegacyTier1RocketRecipes.java     // rocketTier1 metas 0-3 (8 chest permutations)
    LegacyBuggyRecipes.java           // buggy metas 0-3 (8 chest permutations)
micdoodle8/mods/galacticraft/planets/mars/recipe/legacy/
    LegacyTier2RocketRecipes.java     // spaceship metas 0-3
    LegacyCargoRocketRecipes.java     // spaceship metas 11-13
micdoodle8/mods/galacticraft/planets/asteroids/recipe/legacy/
    LegacyAstroMinerRecipes.java      // astroMiner meta 0
```

Each exposes one `register()` that self-gates on `CompatGates.useLegacyContent()`.

Hook points:

```java
// RecipeManagerGC.addUniversalRecipes() — next to the surviving oxTankLight block
if (CompatGates.useLegacyContent()) {
    LegacyNasaWorkbenchRecipes.register();
    LegacyOxygenTankRecipes.register();
    LegacyTier1RocketRecipes.register();
    LegacyBuggyRecipes.register();
}

// RecipeManagerMars.addUniversalRecipes()
if (CompatGates.useLegacyContent()) {
    LegacyTier2RocketRecipes.register();
    LegacyCargoRocketRecipes.register();
}

// AsteroidsModule.postInit()
if (CompatGates.useLegacyContent()) {
    LegacyAstroMinerRecipes.register();
}
```

### 5.4 The bench GUIs need a dual layout — this is the real work

`ContainerBuggyBench` (19 → 35 craft slots) and `ContainerSchematicAstroMiner` (inventory 15 → 30)
cannot simply be
reverted, because GTNH's layouts are what the coremod's GT recipes target. The clean approach is a
**layout switch**, not a revert:

```java
// ContainerBuggyBench
if (CompatGates.useLegacyContent()) {
    addLegacySlots();      // 19 craft slots, verbatim from 79be38a2b
} else {
    addGregTechSlots();    // GTNH layout, moved verbatim out of the constructor
}
```

The same split is needed in `SlotBuggyBench.isItemValid`, `SlotSchematicAstroMiner.isItemValid`,
`InventoryBuggyBench`/`InventorySchematicAstroMiner` sizes, and the GUI texture/offsets
(`GuiSchematicBuggy`, `GuiSchematicAstroMiner`, `GuiSchematicCargoRocket`) — expect new `.classic`
GUI textures or an offset table, mirroring the `.classic` lang-key trick used in the Railcraft fork.
`ContainerSchematicCargoRocket` (inventory 17 → 22) also needs the same treatment.

### 5.5 NEI

Restore the handlers under `if (CompatGates.useLegacyContent())` rather than deleting the comment:

```java
// NEIGalacticraftConfig.loadConfig()
if (CompatGates.useLegacyContent()) {
    API.registerRecipeHandler(new RocketT1RecipeHandler());
    API.registerUsageHandler(new RocketT1RecipeHandler());
    API.registerRecipeHandler(new IngotCompressorRecipeHandler());
    API.registerUsageHandler(new IngotCompressorRecipeHandler());
    API.registerRecipeHandler(new ElectricIngotCompressorRecipeHandler());
    API.registerUsageHandler(new ElectricIngotCompressorRecipeHandler());
}
```

Then populate the handler maps from the same `Legacy*Recipes` helpers (they must be the single
source of truth so NEI and the benches cannot drift). In `NEIGalacticraftMarsConfig` restore
`registerRecipes()` + `RocketT2RecipeHandler`/`GasLiquefierRecipeHandler`/
`MethaneSynthesizerRecipeHandler`; in `NEIGalacticraftAsteroidsConfig` restore `registerRecipes()`
+ `RocketT3RecipeHandler`.

This replaces `NEIGalacticraftConfigMixin` with real code — including the slot-coordinate tables,
which the mixin currently hardcodes anyway.

### 5.6 Mod-compat restorations (optional, in priority order)

1. **CraftGuide** — small, self-contained; re-add `core/recipe/craftguide/*` under the gate and
   re-vendor the CraftGuide API in `dependencies.gradle` as `compileOnly`.
2. **MFR Slimeling** — `MFRSpawnHandlerSlimeling` + the MFR API `compileOnly`.
3. **BuildCraft MJ / Mekanism energy** — largest effort, touches
   `TileBaseUniversalConductor`/`TileBaseUniversalElectrical*`/`EnergyConfigHandler`. Only worth it
   if you actually ship RF/MJ packs; the RF (`receiveEnergy`) path still works today.
4. Version checker / `/gchelp` — intentional removals, no need to restore.

### 5.7 Dual-target build

The GTNH fork compiles against `GT5-Unofficial` as `implementation`. To keep the legacy path
honest, either:
- keep GT as `compileOnly` and reach GT items only via `GameRegistry.findItem` /
  `Class.forName` reflection (the existing `CompatibilityManager` pattern), or
- keep `implementation` and rely on `@Optional`/`Loader.isModLoaded` guards plus runtime
  verification in a no-GT dev instance.

A `run/` instance with **no** GT/`dreamcraft` is essential — the entire §3 list is invisible
otherwise.

---

## 6. Concrete checklist

### Pass 1 — unblock the Moon (highest value)

- [ ] `CompatGates` helper + `auto | forceLegacy | forceGTNH` config
- [ ] `LegacyNasaWorkbenchRecipes` — restore the workbench block recipe
- [ ] `LegacyTier1RocketRecipes` — restore all 8 bench recipes (metas 0–3)
- [ ] `LegacyOxygenTankRecipes` — restore Light(copper)/Medium(tin)/Heavy(steel); keep GTNH's
      compressedAluminium light tank gated to `isGTNH`
- [ ] Restore `RocketT1RecipeHandler` + its map under the gate (§5.5)
- [ ] Verify: NASA Workbench craftable → T1 rocket assembles in bench → Moon reachable, in NEI too

### Pass 2 — planets (no GUI work needed)

- [ ] `LegacyTier2RocketRecipes` — 8 variants, slots 1–18, cargo 19–21 pinned. The container and
      `SlotSchematicTier2Rocket` are unchanged from upstream.
- [ ] `LegacyCargoRocketRecipes` — 3 variants written for the **current** layout (§3.8), outputs
      11/12/13.
- [ ] `LegacyTier3RocketRecipes` — 8 variants (§3.6); container unchanged.
- [ ] Restore Mars `RocketT2RecipeHandler` + `CargoRocketRecipeHandler` maps + `registerRecipes()`
- [ ] Restore Asteroids `RocketT3RecipeHandler` + map + `registerRecipes()`

### Pass 3 — benches whose GUI was redesigned (largest effort)

- [ ] `ContainerBuggyBench` / `SlotBuggyBench` / `InventoryBuggyBench` / `GuiSchematicBuggy` —
      dual layout: 19 craft slots (legacy) vs 35 (GT)
- [ ] `LegacyBuggyRecipes` — metas 0–3
- [x] `ContainerSchematicAstroMiner` — dual layout: 14 craft slots (legacy) vs 30 (GT)
- [x] `SlotSchematicAstroMiner` — dual whitelist (`isLegacyItemValid` / `isGregTechItemValid`)
- [x] `GuiSchematicAstroMiner` — legacy texture + `ySize` 221 vs 203, and the label offsets that go with it
- [x] `AstroMinerRecipeHandler` — legacy texture + upstream's crop (0,-8,3,32,168,104); no transfer rect in legacy
- [x] `LegacyAstroMinerRecipes` + `LegacyAstroMinerBenchNei` (NEI map population)
- [x] `schematic_astro_miner_legacy.png` restored from `79be38a2b` (GTNH's
      `schematic_astro_miner.png` is a different image, so a new filename keeps merges clean)
- [x] Fix unconditioned GT item check in `SlotSchematicAstroMiner` (§4.2)
- [ ] **Moon Buggy** — same shape, still to do:
      `ContainerBuggyBench` / `SlotBuggyBench` / `GuiSchematicBuggy` / `BuggyRecipeHandler`,
      `LegacyBuggyRecipes`, `buggybench_legacy.png`

### Pass 4 — gate hygiene

- [ ] `SlotBuggyBench` — replace `&& dreamcraft` with a real legacy branch (§4.1)
- [ ] Unify `dreamcraft` / `gregtech` / `gregtech_nh` behind `CompatGates` (§4.3)
- [ ] Restore `IngotCompressorRecipeHandler` / `ElectricIngotCompressorRecipeHandler` + maps

### Pass 5 — mod compat (optional)

- [ ] CraftGuide integration (core + planets)
- [ ] MFR Slimeling spawn handler
- [ ] BuildCraft MJ conduction in `TileBaseUniversalConductor`
- [ ] Mekanism energy/gas paths

### Remaining / decide per taste

- [ ] Version checker (`ThreadVersionCheck`), `/gchelp` — leave removed
- [ ] Add a `TESTING_CHECKLIST.md` and automated rebase scripts (copy the Railcraft
      `scripts/` + `REBASE_WORKFLOW.md` layout)

---

## 7. What is *properly* gated (leave alone)

These use `!Loader.isModLoaded("dreamcraft")` and are correct as-is:

| Area | File |
|---|---|
| Bulk universal recipes | `core/recipe/RecipeManagerGC.addUniversalRecipes` (`:163`, `:707`) |
| BC / IC2 / AppEng recipes | `core/recipe/RecipeManagerGC.loadRecipes` (`:43`) |
| Mars recipes (desh tools/armour, stairs, slabs …) | `planets/mars/recipe/RecipeManagerMars` (`:71`) |
| Asteroids recipes | `planets/asteroids/recipe/RecipeManagerAsteroids` (`:61`) |
| Compressor plating recipes | `planets/mars/MarsModule` (`:177`) |
| Ingot ore-dict registration | `core/items/GCItems` (`:231`, `:267`), `planets/asteroids/items/AsteroidsItems` (`:69`) |
| Evolved Zombie copper drop | `core/entities/EntityEvolvedZombie` (`:159`) |
| NEI item hiding (steel/meteoric/desh/titanium items) | `core/nei/NEIGalacticraftConfig` (`:54`) |

No action needed on any of these beyond switching the predicate per §5.2 if you want it centralised.

---

## 8. Changes since the audit (3.4.32 → 3.5.1)

The fork was rebased from `5c02df91a` (`3.4.32-GTNH`) onto `61f55046d` (`3.5.1-GTNH`), 8 commits.
Two of them matter here:

| Commit | Change | Effect on this document |
|---|---|---|
| `ff4c5c87b` | "fix nei recipe handler" (#151) | Re-registers `RocketT1/T2/T3`, `CargoRocket`, `AstroMiner` and `Buggy` handlers (rockets only when GalaxySpace is absent) and adds `API.addRecipeCatalyst(nasaWorkbench, …)` plus GUI overlay handlers. **The display maps are still never populated**, so §3.9 becomes "registered but empty" rather than "unregistered". Restoration therefore only fills the maps — registering handlers too would duplicate every page. |
| `61f55046d` | "Various exploit fixes" (#156) | Unrelated to the restoration. |

Also of note: upstream creates the rocket/bench containers through shared helpers now
(`ContainerSchematicTier2Rocket` lost ~120 lines), but **all slot indices and coordinates are
unchanged**, so the NEI coordinate tables in `LegacyRocketBenchNei` still match. The
`LegacyContentTest` slot/position assertions are the guard for exactly this.

A branch `disambiguate-nasa-workbench-schematics` exists upstream but is unmerged; it appears to
concern schematic item disambiguation, not recipes. Re-check it if it lands.


---

## Appendix — commit map

| Commit | Date | Summary | Non-GTNH impact |
|---|---|---|---|
| `52ea597d1` | 2020-05-06 | First GTNH commit; vendored deps removed, Mekanism + BuildCraft MJ dropped | **BC MJ / Mekanism / CraftGuide / MFR removed** |
| `39f131b90` | 2020-05-06 | Version checker removed | Intentional |
| `3cfded1b6` | 2020-07-31 | Oxygen tank recipes changed/removed | **Medium + Heavy uncraftable; Light re-materialed** |
| `459cfc415` | 2020-08-23 | "removed recipes handled by GalaxySpace" | **Commented out NASA Workbench, T1 rocket, Moon Buggy, T2 rocket, Cargo Rocket, Astro Miner recipes + NEI T1/T2/T3 handlers** |
| `df346caa0` | 2020-08-31 | Buggy / Cargo Rocket / Astro Miner recipes gregified | **Bench GUIs redesigned; vanilla recipes replaced** |
| `35a89e610` | 2021-01-16 | "Remove unused NEI handlers registration" | **Compressor NEI handlers commented out** |
| `7872f1ca7` | 2021-12-06 | FML soft-dependency refactor | `ElectricItemManagerIC2` renamed; IC2 support intact |
| `4a0c7b36e` | 2023-06-05 | Cleanup (#76) | `EntityUndeadAstronaut` removed |
| `697f953cd` | 2024-07-15 | **Move gregified recipes from GC to coremod (#89)** | **718 lines deleted: the commented-out T1/T2/T3 rocket, buggy, cargo rocket, astro miner recipes + all NEI maps** |
| `8405381d2` | 2025-01-04 | GCcore item and recipe cleanup (#98) | **Introduces the `!dreamcraft` gate in `RecipeManagerGC`** (4 sites); deletes the last NASA Workbench comment |
| `067aee068` | 2025-01-12 | GCPlanets item and recipe cleanup (#99) | **Introduces the `!dreamcraft` gate** in `RecipeManagerMars`, `MarsModule`, `RecipeManagerAsteroids` |
| `e3026156e` | 2025-08-30 | Remove old wiki references (#112) | `/gchelp` removed |

**Timeline matters:** the content was removed in **2020**, hard-deleted in **2024**, and the
`!dreamcraft` gate that protects the *survivors* was only added in **2025**. The deleted recipe
families were never given a gate at any point — they simply went from commented-out to gone.

### Upstream baseline reference

`79be38a2b` contains every recipe listed in §3 verbatim. When restoring, diff against it:

```bash
git show 79be38a2b:src/main/java/micdoodle8/mods/galacticraft/core/recipe/RecipeManagerGC.java
git show 79be38a2b:src/main/java/micdoodle8/mods/galacticraft/planets/mars/recipe/RecipeManagerMars.java
git show 79be38a2b:src/main/java/micdoodle8/mods/galacticraft/core/inventory/ContainerBuggyBench.java
git show 79be38a2b:src/main/java/micdoodle8/mods/galacticraft/planets/asteroids/inventory/SlotSchematicAstroMiner.java
```

### Relationship to `MrFuzzihead/FuzziTweaks`

`common/GalacticraftRecipes.java` + the three `mixins/late/galacticraft/*` classes already restore
§3.1, §3.3, §3.4 (partially), §3.2 (as a full re-ladder) and §3.9, plus Tier-3 in §3.6. They are a correct proof of
concept and a usable spec — but they cannot reach §3.5/§3.6, because those need container/GUI/slot
changes that a mixin cannot reasonably carry. That is the argument for moving this into the fork.
