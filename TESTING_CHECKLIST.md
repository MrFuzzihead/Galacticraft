# Testing checklist — non-GTNH content restoration

Run this in a **pack with no `dreamcraft`** (and ideally no GregTech at all). Nothing in the audit
list is reachable otherwise, and a GTNH-pack run exercises the opposite branch of every gate.

Set `Content Mode` in `config/Galacticraft/core.conf` to test both paths without swapping mods:

| Value | Meaning |
|---|---|
| `-1` | auto — original content unless `dreamcraft` is loaded (default) |
| `0` | force original Galacticraft content |
| `1` | force GTNH content (needs the coremod) |

---

## 1. Progression (the reason for this fork)

- [ ] **NASA Workbench** crafts from 2× compressed steel, crafting table, lever, advanced wafer,
      redstone torch
- [ ] Placing it opens the rocket bench; the **Tier-1 rocket** assembles from nose cone,
      8× heavy plating, 4× fins, engine
- [ ] All 8 Tier-1 variants craft: no chest → `rocketTier1:0`, one → `:1`, two → `:2`, three → `:3`
- [ ] **NEI** shows the Tier-1 rockets (R/U on the workbench and on a rocket)
- [ ] Launch to the Moon; **the Moon is reachable**

## 2. Oxygen tanks

- [ ] Light = 7× compressed copper + tin canister + lime wool
- [ ] Medium = 7× compressed tin + tin canister + orange wool
- [ ] Heavy = 7× compressed steel + tin canister + red wool
- [ ] All three output **empty** tanks and refill in the Oxygen Compressor
- [ ] In `-1` mode with `dreamcraft` present, only the GTNH compressed-aluminium Light recipe exists

## 3. Mars

- [ ] Tier-2 rocket assembles (8 variants) and NEI shows them
- [ ] Cargo Rocket assembles for all three chest sizes → `spaceship:11/12/13`
- [ ] NEI shows the Cargo Rocket page and the chest in slot 21
- [ ] **Mars is reachable**

## 4. Asteroids

- [ ] Tier-3 rocket assembles (8 variants) and NEI shows them
- [ ] **The Asteroids are reachable**

## 5. Benches

For both redesigned benches, the container/slot/GUI code was **moved**, not rewritten, on the GTNH
side - so a bug here is almost certainly in the legacy branch or in a wrong `CompatGates` decision.

- [ ] Shift-click a part from the player inventory into the bench: it should land in a matching
      craft slot, not bounce back or land in the output. (The transfer range is computed as
      `inventorySlots.size() - 36`, which resolves differently per layout.)
- [ ] Shift-click the output: the finished machine should transfer to the player inventory.

- [x] **Moon Buggy** — bench shows the original 19-slot layout (4x3 body with the seat in the
      middle, 2x2 wheels, 3 cargo add-ons), every slot accepts its part, and all 8 variants craft
      (0–3 storage modules → `buggy:0-3`). NEI shows the same page over the legacy texture.
- [x] **Astro Miner** — bench shows the original 14-slot miner-shaped layout, every slot accepts
      its part (plating, orion drives, advanced wafer, chests, beam core, flag pole), and the
      machine crafts. NEI shows the same page over the legacy texture.
- [ ] Cargo Rocket bench slots line up with the recipe (advanced wafer 1, nose cone 7, eight plates
      8–15, engine 16, fins 17–20, chest 21)

## 6. NEI completeness and **alignment** (highest-risk item)

- [ ] Ingot Compressor and Electric Ingot Compressor have recipe pages
- [ ] Gas Liquefier and Methane Synthesizer have recipe pages
- [ ] Refinery, Circuit Fabricator, Buggy and rocket pages render without a missing-texture crash
- [ ] **Every item icon on the Buggy and Astro Miner pages sits on its slot outline.** These pages
      use hand-written coordinate tables that no test can validate - a constant offset of a few
      pixels is the most likely bug in this branch. Compare against the slot the recipe fills.
- [ ] On the rocket pages, the same check for slots 1-21 (these tables predate the buggy one but
      were verified against the container source, not in-game)

**Expected, not bugs:**

- The NASA Workbench shows **6 recipe tabs** (Rocket T1, Buggy, Rocket T2, Cargo Rocket, Rocket T3,
  Astro Miner). Each comes from an `API.addRecipeCatalyst(nasaWorkbench, handler)` call added by
  upstream `ff4c5c87b` (#151); this fork adds none. Rockets drop out if GalaxySpace is installed.
- NEI draws its own `Page x/y` label (`nei.recipe.page`) at `guiTop + 17` in the header strip. Its
  position comes from NEI's `initGui` (window size, GUI scale, `inventory.guirecipe.maxHeight` and
  the tab-strip height) — **a handler's `drawBackground` cannot move or clip it**, because the
  background is drawn first. If it looks crowded, it is NEI layout reacting to the tab strip.

## 7. Regression checks (the GTNH path must be untouched)

Also worth one dedicated run: **GregTech installed, `dreamcraft` absent.** Before this branch,
`SlotBuggyBench` gated its GregTech item lookups on `dreamcraft`, leaving 12 slots that accepted
nothing; they now key off "is GregTech present". Expect the GT slots to accept `gt.metaitem.01`
items and no `NoClassDefFoundError` in the log.

Switch Content Mode to `1` (or install the GTNH pack) and confirm:

- [ ] No duplicate recipes for oxygen tanks / rockets
- [ ] No crash on startup; no `CompatGates` stack traces in the log
- [ ] GTNH's compressed-aluminium Light tank recipe is present
- [ ] `./gradlew test` still passes (the `LegacyContentTest` tables)

## 8. Rebasing

- [ ] `./gradlew test` after each GTNH rebase
- [ ] `grep -c CompatGates …/RecipeManagerGC.java` still returns 2 (the gate and the else branch)
- [ ] GTNH has not added its own gating *inside* the blocks we now wrap

---

## Known gaps

Both redesigned benches (Moon Buggy and Astro Miner) are restored. What is still missing:

- **CraftGuide / MFR / BuildCraft MJ / Mekanism** integration: removed in GTNH's very first commit;
  not restored (see `GTNH_FORK_AUDIT.md` §3.10).
- **NEI rocket pages require GalaxySpace to be absent** — that ordering is upstream's
  (`if (!GalaxySpaceLoaded)`), not ours.
