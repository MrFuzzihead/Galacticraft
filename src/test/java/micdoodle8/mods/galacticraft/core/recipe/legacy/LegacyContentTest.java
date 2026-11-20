package micdoodle8.mods.galacticraft.core.recipe.legacy;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.jupiter.api.Test;

import micdoodle8.mods.galacticraft.core.nei.LegacyRocketBenchNei;

/**
 * Guards the restored Tier-1 rocket bench data and its NEI display table.
 *
 * <p>
 * These are the parts most likely to be broken by a bad rebase against GTNH: the recipe maps are hand-written slot
 * indices and the NEI table is a hand-written coordinate list. Everything asserted here is plain data (no Minecraft
 * bootstrap required).
 */
class LegacyContentTest {

    private static Boolean recipesUsable;

    /**
     * Building {@code ItemStack}s needs the game classes plus Galacticraft's {@code compileOnly} dependencies, which
     * are not all on the plain-JUnit classpath. Skip those assertions rather than fail the build; the plain-data
     * assertions below still run everywhere.
     */
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

    @Test
    void tier1HasEightVariants() {
        assertEquals(8, LegacyTier1RocketRecipes.CARGO_MASKS.length);
        assertEquals(8, LegacyTier1RocketRecipes.OUTPUT_METAS.length);
    }

    @Test
    void tier1CargoMasksAreTheSevenDistinctNonEmptyCombinations() {
        final Set<Integer> masks = new HashSet<>();
        for (final int mask : LegacyTier1RocketRecipes.CARGO_MASKS) {
            assertTrue(mask >= 0 && mask <= 0x7, "cargo mask out of range: " + mask);
            assertTrue(masks.add(mask), "duplicate cargo mask: " + mask);
        }
        // basic rocket (no chest) plus all seven non-empty subsets of the three add-on slots
        assertEquals(8, masks.size());
    }

    @Test
    void tier1CargoMaskMatchesItsOutputMeta() {
        // 0 chests -> tier 0, 1 -> tier 1, 2 -> tier 2, 3 -> tier 3
        for (int i = 0; i < LegacyTier1RocketRecipes.CARGO_MASKS.length; i++) {
            final int chests = Integer.bitCount(LegacyTier1RocketRecipes.CARGO_MASKS[i]);
            assertEquals(
                    chests,
                    LegacyTier1RocketRecipes.OUTPUT_METAS[i],
                    "variant " + i + " has " + chests + " chests but meta " + LegacyTier1RocketRecipes.OUTPUT_METAS[i]);
        }
    }

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

    @Test
    void tier1NonCargoSlotsAreAlwaysPopulated() {
        assumeTrue(recipesUsable(), "game classes are not on the test classpath");

        for (final LegacyTier1RocketRecipes.Variant variant : LegacyTier1RocketRecipes.variants()) {
            for (final Entry<Integer, net.minecraft.item.ItemStack> slot : variant.input.entrySet()) {
                final boolean cargo = slot.getKey() >= 15 && slot.getKey() <= 17;
                if (!cargo) {
                    assertNotNull(slot.getValue(), "slot " + slot.getKey() + " must have a part");
                }
            }
        }
    }

    @Test
    void neiTableDefinesEveryBenchSlotExactlyOnce() {
        final Set<Long> seen = new HashSet<>();
        for (int slot = 1; slot <= LegacyTier1RocketRecipes.SLOT_COUNT; slot++) {
            final int[] pos = LegacyRocketBenchNei.neiPosition(slot);
            assertNotNull(pos, "NEI has no position for bench slot " + slot);
            assertEquals(2, pos.length);
            assertTrue(seen.add((long) pos[0] << 32 | pos[1] & 0xFFFFFFFFL), "duplicate NEI position for slot " + slot);
        }
        assertNull(LegacyRocketBenchNei.neiPosition(0), "slot 0 is the output and has no input position");
        assertNull(LegacyRocketBenchNei.neiPosition(18), "slot 18 does not exist");
    }

    @Test
    void neiPositionsAreInTheTextureFrame() {
        // rocketbench.png is drawn at (3,4) with a 168x130 crop; inputs must land inside it.
        for (int slot = 1; slot <= LegacyTier1RocketRecipes.SLOT_COUNT; slot++) {
            final int[] pos = LegacyRocketBenchNei.neiPosition(slot);
            assertTrue(pos[0] >= 0 && pos[0] <= 168, "slot " + slot + " x out of frame: " + pos[0]);
            assertTrue(pos[1] >= 0 && pos[1] <= 130, "slot " + slot + " y out of frame: " + pos[1]);
        }
    }

    @Test
    void neiTableIsACopy() {
        final int[] pos = LegacyRocketBenchNei.neiPosition(1);
        assertArrayEquals(new int[] { 45, 15 }, pos);
        pos[0] = -1;
        assertArrayEquals(new int[] { 45, 15 }, LegacyRocketBenchNei.neiPosition(1), "caller mutated the table");
    }
}
