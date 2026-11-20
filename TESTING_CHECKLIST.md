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

- [ ] Moon Buggy bench opens; the GregTech-only slots (8–11, 17–24) accept nothing but do not crash
      *(expected until the legacy layout lands — see "Known gaps")*
- [ ] Astro Miner bench opens; slots 28–29 accept nothing but do not crash
- [ ] Cargo Rocket bench slots line up with the recipe (advanced wafer 1, nose cone 7, eight plates
      8–15, engine 16, fins 17–20, chest 21)

## 6. NEI completeness

- [ ] Ingot Compressor and Electric Ingot Compressor have recipe pages
- [ ] Refinery, Circuit Fabricator, Buggy and rocket pages render without a missing-texture crash

## 7. Regression checks (the GTNH path must be untouched)

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

These are documented in `GTNH_FORK_AUDIT.md` §3.5–3.7 and are **not** implemented yet:

- Moon Buggy bench: GTNH redesigned the container from 19 to 35 craft slots
  (`InventoryBuggyBench` 32 → 36) and recompressed `buggybench.png`. Restoring the vanilla layout
  needs a dual container/slot/GUI path and the old texture asset.
- Astro Miner bench: inventory 15 → 30, `ySize` 221 → 203, `schematic_astro_miner.png` recompressed.
  Same dual-layout work.
- CraftGuide / MFR / BuildCraft MJ / Mekanism integration: removed in the first GTNH commit; not
  restored.
