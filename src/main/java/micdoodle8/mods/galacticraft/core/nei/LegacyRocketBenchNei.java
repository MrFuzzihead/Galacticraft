package micdoodle8.mods.galacticraft.core.nei;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.minecraft.item.ItemStack;

import codechicken.nei.PositionedStack;
import micdoodle8.mods.galacticraft.core.recipe.legacy.LegacyTier1RocketRecipes;

/**
 * NEI coordinates and display building for every NASA-bench rocket page restored by this fork.
 *
 * <p>
 * GTNH commented the {@code RocketT1RecipeHandler} registration out in <code>459cfc415</code> ("removed recipes handled
 * by GalaxySpace"), deleted the Tier-2/Tier-3 handler registrations in <code>697f953cd</code>, and never repopulated
 * the display maps, so the rocket benches are invisible in NEI outside the GTNH pack.
 *
 * <p>
 * Coordinates are the container slot coordinates minus the crop each handler applies in {@code drawBackground}:
 *
 * <pre>
 *   Tier 1          drawTexturedModalRect(0,    0, 3, 4, 168, 130)  ->  (x-3,  y-4)
 *   Tier 2 / Tier 3 drawTexturedModalRect(0,   -8, 3, 4, 168, 140)  ->  (x-3,  y-12)
 *   Cargo Rocket    drawTexturedModalRect(-1, -12, 3, 4, 168, 122)  ->  (x-4,  y-16)
 * </pre>
 *
 * <p>
 * Slot indices come from {@code ContainerSchematicTier1Rocket}, {@code …Tier2Rocket}, {@code …Tier3Rocket} (Tier 2 and
 * Tier 3 share a layout) and {@code ContainerSchematicCargoRocket}. These tables are the single source of those
 * numbers; the recipe helpers supply the items, so NEI cannot drift from what is registered.
 */
public final class LegacyRocketBenchNei {

    /** Bench selector. */
    public static final int BENCH_TIER1 = 0;
    public static final int BENCH_TIER2_3 = 1;
    public static final int BENCH_CARGO = 2;

    /** Output bullet positions, per bench. */
    public static final int[] OUTPUT_TIER1 = { 139, 92 };
    public static final int[] OUTPUT_TIER2_3 = { 139, 102 };
    public static final int[] OUTPUT_CARGO = { 130, 57 };

    /** Highest input slot index any bench has. */
    public static final int MAX_SLOT = 21;

    /** NEI x/y per bench slot index. Index 0 is the output and is intentionally null. */
    private static final int[][] TIER1 = new int[MAX_SLOT + 1][];
    private static final int[][] TIER2_3 = new int[MAX_SLOT + 1][];
    private static final int[][] CARGO = new int[MAX_SLOT + 1][];

    static {
        // ---- Tier 1 (ContainerSchematicTier1Rocket) ----
        TIER1[1] = new int[] { 45, 15 };
        for (int row = 0; row < 4; row++) {
            TIER1[2 + row] = new int[] { 36, 33 + row * 18 };
            TIER1[6 + row] = new int[] { 54, 33 + row * 18 };
        }
        TIER1[10] = new int[] { 18, 87 };
        TIER1[11] = new int[] { 18, 105 };
        TIER1[12] = new int[] { 45, 105 };
        TIER1[13] = new int[] { 72, 87 };
        TIER1[14] = new int[] { 72, 105 };
        for (int i = 0; i < 3; i++) {
            TIER1[15 + i] = new int[] { 90 + i * 26, 8 };
        }

        // ---- Tier 2 and Tier 3 (…Tier2Rocket / …Tier3Rocket, identical layouts) ----
        TIER2_3[1] = new int[] { 45, 7 };
        for (int row = 0; row < 5; row++) {
            TIER2_3[2 + row] = new int[] { 36, 25 + row * 18 };
            TIER2_3[7 + row] = new int[] { 54, 25 + row * 18 };
        }
        TIER2_3[12] = new int[] { 18, 79 };
        TIER2_3[13] = new int[] { 18, 97 };
        TIER2_3[14] = new int[] { 18, 115 };
        TIER2_3[15] = new int[] { 45, 115 };
        TIER2_3[16] = new int[] { 72, 79 };
        TIER2_3[17] = new int[] { 72, 97 };
        TIER2_3[18] = new int[] { 72, 115 };
        for (int i = 0; i < 3; i++) {
            TIER2_3[19 + i] = new int[] { 90 + i * 26, 0 };
        }

        // ---- Cargo Rocket (ContainerSchematicCargoRocket) ----
        CARGO[1] = new int[] { 130, -6 };
        CARGO[2] = new int[] { 130, 12 };
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                CARGO[3 + i * 2 + j] = new int[] { 112 + j * 36, 3 + i * 18 };
            }
        }
        CARGO[7] = new int[] { 49, 3 };
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 2; j++) {
                CARGO[8 + i * 2 + j] = new int[] { 40 + j * 18, 21 + i * 18 };
            }
        }
        CARGO[16] = new int[] { 49, 93 };
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                CARGO[17 + i * 2 + j] = new int[] { 22 + j * 54, 75 + i * 18 };
            }
        }
        CARGO[21] = new int[] { 130, 30 };
    }

    private LegacyRocketBenchNei() {}

    /** NEI coordinate of a slot on the given bench, or {@code null} if it has none. Returns a copy. */
    public static int[] position(int bench, int slot) {
        final int[][] table = tableFor(bench);
        if (slot < 0 || slot >= table.length || table[slot] == null) {
            return null;
        }
        return table[slot].clone();
    }

    /** Output bullet position for the given bench. Returns a copy. */
    public static int[] outputPosition(int bench) {
        switch (bench) {
            case BENCH_TIER1:
                return OUTPUT_TIER1.clone();
            case BENCH_CARGO:
                return OUTPUT_CARGO.clone();
            default:
                return OUTPUT_TIER2_3.clone();
        }
    }

    /**
     * Builds the NEI display map for one recipe, skipping slots the recipe pins to empty. Slot 0 (the output) is never
     * in the input map, so it is not handled here.
     */
    public static HashMap<Integer, PositionedStack> displayMap(HashMap<Integer, ItemStack> input, int bench) {
        final HashMap<Integer, PositionedStack> display = new HashMap<>(input.size());

        for (final Entry<Integer, ItemStack> slot : new TreeMap<>(input).entrySet()) {
            final ItemStack stack = slot.getValue();
            if (stack == null) {
                continue;
            }
            final int[] pos = position(bench, slot.getKey());
            if (pos != null) {
                display.put(slot.getKey(), new PositionedStack(stack, pos[0], pos[1]));
            }
        }
        return display;
    }

    /**
     * List-style variant of {@link #displayMap}, ordered by slot index, for the Mars and Asteroids handlers whose maps
     * are keyed by {@code ArrayList<PositionedStack>}.
     */
    public static ArrayList<PositionedStack> displayList(HashMap<Integer, ItemStack> input, int bench) {
        final ArrayList<PositionedStack> display = new ArrayList<>(input.size());

        for (final Entry<Integer, ItemStack> slot : new TreeMap<>(input).entrySet()) {
            final ItemStack stack = slot.getValue();
            if (stack == null) {
                continue;
            }
            final int[] pos = position(bench, slot.getKey());
            if (pos != null) {
                display.add(new PositionedStack(stack, pos[0], pos[1]));
            }
        }
        return display;
    }

    /**
     * Fills the Tier-1 rocket NEI display map. GTNH registers {@code RocketT1RecipeHandler} but never populates the map
     * it reads, so the page exists and lists nothing.
     */
    public static void populate(NEIGalacticraftConfig config) {
        for (final LegacyTier1RocketRecipes.Variant variant : LegacyTier1RocketRecipes.variants()) {
            config.registerRocketBenchRecipe(
                    displayMap(variant.input, BENCH_TIER1),
                    new PositionedStack(variant.output, OUTPUT_TIER1[0], OUTPUT_TIER1[1]));
        }
    }

    private static int[][] tableFor(int bench) {
        switch (bench) {
            case BENCH_TIER1:
                return TIER1;
            case BENCH_CARGO:
                return CARGO;
            default:
                return TIER2_3;
        }
    }
}
