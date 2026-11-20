package micdoodle8.mods.galacticraft.core.recipe.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.item.ItemStack;

import org.junit.jupiter.api.Test;

import micdoodle8.mods.galacticraft.core.nei.LegacyRocketBenchNei;
import micdoodle8.mods.galacticraft.planets.asteroids.nei.LegacyAstroMinerBenchNei;
import micdoodle8.mods.galacticraft.planets.asteroids.recipe.legacy.LegacyAstroMinerRecipes;
import micdoodle8.mods.galacticraft.planets.asteroids.recipe.legacy.LegacyTier3RocketRecipes;
import micdoodle8.mods.galacticraft.planets.mars.recipe.legacy.LegacyCargoRocketRecipes;
import micdoodle8.mods.galacticraft.planets.mars.recipe.legacy.LegacyTier2RocketRecipes;

/**
 * Guards the restored NASA-bench data and the NEI coordinate tables.
 *
 * <p>
 * These are the parts most likely to be broken by a bad rebase against GTNH: the recipe maps are hand-written slot
 * indices and the NEI tables are hand-written coordinate lists. The plain-data assertions run everywhere; the ones that
 * build {@code ItemStack}s are skipped when the game classes (and Galacticraft's {@code compileOnly} dependencies) are
 * not on the test classpath.
 */
class LegacyContentTest {

    private static Boolean recipesUsable;

    private static boolean recipesUsable() {
        if (recipesUsable == null) {
            try {
                LegacyTier1RocketRecipes.variant(0, null, null, null);
                recipesUsable = Boolean.TRUE;
            } catch (final Throwable ignored) {
                recipesUsable = Boolean.FALSE;
            }
        }
        return recipesUsable;
    }

    // ---------------------------------------------------------------- cargo layout

    @Test
    void cargoLayoutHasEightDistinctVariants() {
        assertEquals(8, LegacyCargoLayout.COUNT);
        assertEquals(8, LegacyCargoLayout.MASKS.length);
        assertEquals(8, LegacyCargoLayout.OUTPUT_METAS.length);

        final Set<Integer> masks = new HashSet<>();
        for (final int mask : LegacyCargoLayout.MASKS) {
            assertTrue(mask >= 0 && mask <= 0x7, "cargo mask out of range: " + mask);
            assertTrue(masks.add(mask), "duplicate cargo mask: " + mask);
        }
    }

    @Test
    void cargoMetaEqualsChestCount() {
        for (int i = 0; i < LegacyCargoLayout.COUNT; i++) {
            final int chests = Integer.bitCount(LegacyCargoLayout.MASKS[i]);
            assertEquals(
                    chests,
                    LegacyCargoLayout.OUTPUT_METAS[i],
                    "variant " + i + " has " + chests + " chests but meta " + LegacyCargoLayout.OUTPUT_METAS[i]);
        }
    }

    @Test
    void hasChestReadsTheRightBit() {
        assertTrue(LegacyCargoLayout.hasChest(0x1, 0));
        assertTrue(LegacyCargoLayout.hasChest(0x2, 1));
        assertTrue(LegacyCargoLayout.hasChest(0x4, 2));
        assertTrue(!LegacyCargoLayout.hasChest(0x0, 0));
        assertTrue(!LegacyCargoLayout.hasChest(0x1, 1));
    }

    // ---------------------------------------------------------------- recipe maps

    @Test
    void tier1VariantCoversEverySlotAndPinsTheCargoSlots() {
        assumeTrue(recipesUsable(), "game classes are not on the test classpath");

        final LegacyTier1RocketRecipes.Variant variant = LegacyTier1RocketRecipes.variant(0, null, null, null);

        assertEquals(0, variant.output.getItemDamage());
        assertEquals(
                LegacyTier1RocketRecipes.SLOT_COUNT,
                variant.input.size(),
                "every slot 1-17 must be pinned, otherwise the recipe also matches a cargo layout");

        for (int slot = 1; slot <= LegacyTier1RocketRecipes.SLOT_COUNT; slot++) {
            assertTrue(variant.input.containsKey(slot), "missing slot " + slot);
        }
        for (final int slot : LegacyTier1RocketRecipes.CARGO_SLOTS) {
            assertNull(variant.input.get(slot), "cargo slot " + slot + " must be pinned to empty");
        }
    }

    // ---------------------------------------------------------------- NEI tables

    @Test
    void everyRecipeSlotHasAnNeiPosition() {
        assumeTrue(recipesUsable(), "game classes are not on the test classpath");

        for (final LegacyTier1RocketRecipes.Variant v : LegacyTier1RocketRecipes.variants()) {
            assertEverySlotMapped(LegacyRocketBenchNei.BENCH_TIER1, v.input, "Tier 1");
        }
        for (final LegacyTier2RocketRecipes.Variant v : LegacyTier2RocketRecipes.variants()) {
            assertEverySlotMapped(LegacyRocketBenchNei.BENCH_TIER2_3, v.input, "Tier 2");
        }
        for (final LegacyTier3RocketRecipes.Variant v : LegacyTier3RocketRecipes.variants()) {
            assertEverySlotMapped(LegacyRocketBenchNei.BENCH_TIER2_3, v.input, "Tier 3");
        }
        for (final LegacyCargoRocketRecipes.Variant v : LegacyCargoRocketRecipes.variants()) {
            assertEverySlotMapped(LegacyRocketBenchNei.BENCH_CARGO, v.input, "Cargo");
        }
    }

    private static void assertEverySlotMapped(int bench, Map<Integer, ItemStack> input, String name) {
        for (final Map.Entry<Integer, ItemStack> slot : input.entrySet()) {
            if (slot.getValue() == null) {
                continue;
            }
            assertNotNull(
                    LegacyRocketBenchNei.position(bench, slot.getKey()),
                    name + " recipe slot " + slot.getKey() + " has no NEI position");
        }
    }

    @Test
    void neiTableCoversEveryBenchSlot() {
        assertTableComplete(LegacyRocketBenchNei.BENCH_TIER1, LegacyTier1RocketRecipes.SLOT_COUNT);
        assertTableComplete(LegacyRocketBenchNei.BENCH_TIER2_3, 21);
        assertTableComplete(LegacyRocketBenchNei.BENCH_CARGO, 21);
    }

    private static void assertTableComplete(int bench, int maxSlot) {
        for (int slot = 1; slot <= maxSlot; slot++) {
            assertNotNull(
                    LegacyRocketBenchNei.position(bench, slot),
                    "NEI has no position for bench " + bench + " slot " + slot);
        }
        assertNull(LegacyRocketBenchNei.position(bench, 0), "slot 0 is the output and has no input position");
        assertNull(
                LegacyRocketBenchNei.position(bench, maxSlot + 1),
                "bench " + bench + " should not define slot " + (maxSlot + 1));
    }

    @Test
    void neiPositionsAreUniqueWithinEachBench() {
        assertUnique(LegacyRocketBenchNei.BENCH_TIER1, 17);
        assertUnique(LegacyRocketBenchNei.BENCH_TIER2_3, 21);
        assertUnique(LegacyRocketBenchNei.BENCH_CARGO, 21);
    }

    private static void assertUnique(int bench, int maxSlot) {
        final Set<Long> seen = new HashSet<>();
        for (int slot = 1; slot <= maxSlot; slot++) {
            final int[] pos = LegacyRocketBenchNei.position(bench, slot);
            final long key = (long) pos[0] << 32 | pos[1] & 0xFFFFFFFFL;
            assertTrue(seen.add(key), "duplicate NEI position for bench " + bench + " slot " + slot);
        }
    }

    @Test
    void neiPositionsAreInTheTextureFrame() {
        // Every rocket bench texture is drawn as a 168x140 crop, with some inputs deliberately
        // sitting slightly outside it (the cargo rocket's top row). Keep the check loose but real.
        for (int bench = LegacyRocketBenchNei.BENCH_TIER1; bench <= LegacyRocketBenchNei.BENCH_CARGO; bench++) {
            for (int slot = 1; slot <= LegacyRocketBenchNei.MAX_SLOT; slot++) {
                final int[] pos = LegacyRocketBenchNei.position(bench, slot);
                if (pos == null) {
                    continue;
                }
                assertTrue(pos[0] >= 0 && pos[0] <= 168, "bench " + bench + " slot " + slot + " x " + pos[0]);
                assertTrue(pos[1] >= -8 && pos[1] <= 140, "bench " + bench + " slot " + slot + " y " + pos[1]);
            }
        }
    }

    @Test
    void neiPositionsAreCopies() {
        final int[] pos = LegacyRocketBenchNei.position(LegacyRocketBenchNei.BENCH_TIER1, 1);
        assertEquals(45, pos[0]);
        assertEquals(15, pos[1]);
        pos[0] = -1;
        assertEquals(45, LegacyRocketBenchNei.position(LegacyRocketBenchNei.BENCH_TIER1, 1)[0], "table was mutated");
    }

    @Test
    void everyBenchHasAnOutputPosition() {
        for (int bench = LegacyRocketBenchNei.BENCH_TIER1; bench <= LegacyRocketBenchNei.BENCH_CARGO; bench++) {
            final int[] out = LegacyRocketBenchNei.outputPosition(bench);
            assertNotNull(out, "bench " + bench + " has no output position");
            assertEquals(2, out.length);
            assertTrue(out[0] > 0 && out[1] > 0, "bench " + bench + " output position looks wrong");
        }
    }

    // ---------------------------------------------------------------- Astro Miner bench

    @Test
    void astroMinerNeiTableCoversEverySlot() {
        for (int slot = 1; slot <= LegacyAstroMinerRecipes.SLOT_COUNT; slot++) {
            assertNotNull(LegacyAstroMinerBenchNei.position(slot), "NEI has no position for astro miner slot " + slot);
        }
        assertNull(LegacyAstroMinerBenchNei.position(0), "slot 0 is the output");
        assertNull(
                LegacyAstroMinerBenchNei.position(LegacyAstroMinerRecipes.SLOT_COUNT + 1),
                "slot 15 does not exist on the legacy bench");
    }

    @Test
    void astroMinerNeiPositionsAreUnique() {
        final Set<Long> seen = new HashSet<>();
        for (int slot = 1; slot <= LegacyAstroMinerRecipes.SLOT_COUNT; slot++) {
            final int[] pos = LegacyAstroMinerBenchNei.position(slot);
            final long key = (long) pos[0] << 32 | pos[1] & 0xFFFFFFFFL;
            assertTrue(seen.add(key), "duplicate NEI position for astro miner slot " + slot);
        }
    }

    @Test
    void astroMinerRecipeStaysInsideTheLegacyBench() {
        assumeTrue(recipesUsable(), "game classes are not on the test classpath");

        final java.util.Map<Integer, ItemStack> input = LegacyAstroMinerRecipes.inputs();
        assertEquals(
                LegacyAstroMinerRecipes.SLOT_COUNT,
                input.size(),
                "the upstream astro miner recipe fills every one of the 14 slots");

        for (final java.util.Map.Entry<Integer, ItemStack> slot : input.entrySet()) {
            assertTrue(
                    slot.getKey() >= 1 && slot.getKey() <= LegacyAstroMinerRecipes.SLOT_COUNT,
                    "recipe uses slot " + slot.getKey() + " which the legacy bench does not have");
            assertNotNull(slot.getValue(), "slot " + slot.getKey() + " must have a part");
            assertNotNull(
                    LegacyAstroMinerBenchNei.position(slot.getKey()),
                    "recipe slot " + slot.getKey() + " has no NEI position");
        }
    }
}
