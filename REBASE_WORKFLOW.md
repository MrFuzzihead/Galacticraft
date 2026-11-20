# Rebase workflow — keeping the non-GTNH restoration on top of GTNH

This repository has two kinds of branches:

| Branch | Meaning |
|---|---|
| `master` | **Mirror of GTNH** (`upstream/master`, i.e. `GTNewHorizons/Galacticraft`). Never commit your own work here. |
| `gtnh-compat` | The non-GTNH restoration work **on top of** `master`. This is what gets built and dropped into the modpack. |

Remotes:
- `origin` → your fork (e.g. `https://github.com/MrFuzzihead/Galacticraft.git`)
- `upstream` → `https://github.com/GTNewHorizons/Galacticraft.git` (GTNH)

---

## One-time setup

```bash
git remote add upstream https://github.com/GTNewHorizons/Galacticraft.git   # if missing
git config rerere.enabled true
git config rerere.autoupdate true      # auto-apply recorded conflict resolutions
```

`rerere` remembers how you resolved a conflict and replays it on the next rebase, which makes
repeated GTNH updates much less painful. The helper scripts set this for you too.

---

## Whenever GTNH pushes a new release

### Option A — script (recommended)

Windows:
```bat
scripts\rebase-gtnh.bat
```
Git Bash / Linux / macOS:
```bash
bash scripts/rebase-gtnh.sh
```

The script fetches `upstream`, rebases `gtnh-compat` onto `upstream/master`, runs `./gradlew build`,
and prints the newest release jar.

### Option B — manual

```bash
git fetch upstream --prune
git switch gtnh-compat
git rebase upstream/master        # or: git rebase <release-tag>
./gradlew build
```

---

## Picking the jar for the modpack

`./gradlew build` writes to `build/libs/`:

| File | Use |
|---|---|
| `Galacticraft-<version>.jar` | **This is the one.** Drop into `mods/`. |
| `Galacticraft-<version>-dev.jar` | Dev/deobfuscated — do not ship. |
| `Galacticraft-<version>-sources.jar` | Sources — do not ship. |
| `Galacticraft-<version>-api.jar` | API — do not ship. |
| `Galacticraft-<version>-dirty*.jar` | Built with uncommitted/untracked files — ignore; rebuild on a clean tree. |

The version string is `<GTNH-version>-<branch>+<sha>`; the suffix is just this fork's commits.

> Tip: build on a clean working tree (`git status`) so you do not get a `-dirty` jar.

---

## Optional — keep the fork's `master` current

```bash
git fetch upstream --prune
git switch master
git merge --ff-only upstream/master
git push origin master
```

Then rebase the feature branch (Option A/B above), and publish it:

```bash
git push --force-with-lease origin gtnh-compat
```

(`--force-with-lease` is required because rebasing rewrites the feature commits.)

---

## If a rebase hits a conflict

1. Resolve the conflicting files.
2. `git add <files>`
3. `git rebase --continue`
4. To bail out: `git rebase --abort` (returns to the pre-rebase state; the pushed
   `origin/gtnh-compat` is always a safe fallback).

### Expected conflict hotspots

All of the restoration work is additive and gated, so conflicts are usually trivial. In rough order
of likelihood:

| File | What we changed |
|---|---|
| `core/recipe/RecipeManagerGC.java` | wrapped the two compressed-aluminium light-tank recipes in `if (CompatGates.useLegacyContent()) … else …`, and added the legacy `register()` calls in the `else` |
| `planets/mars/recipe/RecipeManagerMars.java` | legacy `register()` calls appended at the end of `addUniversalRecipes()` |
| `planets/asteroids/AsteroidsModule.java` | `LegacyTier3RocketRecipes.register()` right after `RecipeManagerAsteroids.loadRecipes()` |
| `core/nei/NEIGalacticraftConfig.java` | `LegacyRocketBenchNei.register(this)` + compressor handler block, both inside `if (CompatGates.useLegacyContent())` |
| `planets/mars/nei/NEIGalacticraftMarsConfig.java` | `addLegacyBenchRecipes()` call + method |
| `planets/asteroids/nei/NEIGalacticraftAsteroidsConfig.java` | `addLegacyRocketBenchRecipes()` call + method |
| `core/inventory/SlotBuggyBench.java` | `Loader.isModLoaded("dreamcraft")` → `CompatGates.useGregTechItems()` on the two GT branches |
| `planets/asteroids/inventory/SlotSchematicAstroMiner.java` | added the missing `CompatGates.useGregTechItems()` guard on slots 28/29 |
| `core/Constants.java` | added `MOD_ID_GREGTECH_NH` |
| `core/util/ConfigManagerCore.java` | added `gtnhCompatMode` field + config entry next to "Enable Debug Messages" |
| `assets/galacticraftcore/lang/en_US.lang` | added `gc.configgui.gtnhCompatMode` next to `gc.configgui.enableDebug` — keep both sides |

Everything else lives in new files, which never conflict:

```
core/util/CompatGates.java
core/recipe/legacy/LegacyCargoLayout.java
core/recipe/legacy/LegacyNasaWorkbenchRecipes.java
core/recipe/legacy/LegacyOxygenTankRecipes.java
core/recipe/legacy/LegacyTier1RocketRecipes.java
core/nei/LegacyRocketBenchNei.java
planets/mars/recipe/legacy/LegacyTier2RocketRecipes.java
planets/mars/recipe/legacy/LegacyCargoRocketRecipes.java
planets/asteroids/recipe/legacy/LegacyTier3RocketRecipes.java
test/…/LegacyContentTest.java
```

### After a rebase: re-check the things that silently rot

A clean rebase can still leave the restoration logically wrong. After every rebase run:

```bash
./gradlew test        # LegacyContentTest guards the slot/meta/NEI tables
grep -c "CompatGates" src/main/java/micdoodle8/mods/galacticraft/core/recipe/RecipeManagerGC.java   # expect 2
```

and confirm GTNH has not reintroduced gating inside the code we now wrap (see
`TESTING_CHECKLIST.md`).
