package micdoodle8.mods.galacticraft.planets.asteroids.nei;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.minecraft.item.ItemStack;

import codechicken.nei.PositionedStack;
import micdoodle8.mods.galacticraft.planets.asteroids.recipe.legacy.LegacyAstroMinerRecipes;

/**
 * NEI coordinates for the restored (non-GTNH) Astro Miner bench page.
 *
 * <p>
 * GTNH registers {@code AstroMinerRecipeHandler} but never populates the
 * {@code NEIGalacticraftAsteroidsConfig#astroMinerRecipes} map it reads, so the page renders and lists nothing. This
 * fills it from {@link LegacyAstroMinerRecipes} so NEI cannot drift from the recipe that is actually registered.
 *
 * <p>
 * Coordinates are the slot positions of {@code ContainerSchematicAstroMiner#addLegacySlots} minus the crop upstream's
 * handler applies in {@code drawBackground} ({@code drawTexturedModalRect(0, -8, 3, 32, 168, 104)}), i.e.
 * {@code (x-3, y-40)}:
 *
 * <pre>
 *   slot 0 (output)      (142, 98)  ->  (139, 58)
 *   slots 1-4  top layer (27/45/63/81, 61)  ->  y 21
 *   slots 5-9  mid layer (16..88, 79)       ->  y 39
 *   slots 10-12 bottom   (44/62/80, 97)     ->  y 57
 *   slots 13-14 laser    (8/26, 103)        ->  y 63
 * </pre>
 */
public final class LegacyAstroMinerBenchNei {

    /** Output bullet position. */
    public static final int[] OUTPUT = { 139, 58 };

    /** Highest input slot index. */
    public static final int MAX_SLOT = LegacyAstroMinerRecipes.SLOT_COUNT;

    /** NEI x/y per bench slot index. Index 0 is the output and is intentionally null. */
    private static final int[][] SLOT_POS = new int[MAX_SLOT + 1][];

    static {
        SLOT_POS[1] = new int[] { 24, 21 };
        SLOT_POS[2] = new int[] { 42, 21 };
        SLOT_POS[3] = new int[] { 60, 21 };
        SLOT_POS[4] = new int[] { 78, 21 };
        SLOT_POS[5] = new int[] { 13, 39 };
        SLOT_POS[6] = new int[] { 31, 39 };
        SLOT_POS[7] = new int[] { 49, 39 };
        SLOT_POS[8] = new int[] { 67, 39 };
        SLOT_POS[9] = new int[] { 85, 39 };
        SLOT_POS[10] = new int[] { 41, 57 };
        SLOT_POS[11] = new int[] { 59, 57 };
        SLOT_POS[12] = new int[] { 77, 57 };
        SLOT_POS[13] = new int[] { 5, 63 };
        SLOT_POS[14] = new int[] { 23, 63 };
    }

    private LegacyAstroMinerBenchNei() {}

    /** NEI coordinate of a bench slot, or {@code null} if it has none. Returns a copy. */
    public static int[] position(int slot) {
        if (slot < 0 || slot >= SLOT_POS.length || SLOT_POS[slot] == null) {
            return null;
        }
        return SLOT_POS[slot].clone();
    }

    /** Builds the NEI display list for the restored recipe, ordered by slot index. */
    public static ArrayList<PositionedStack> displayList() {
        final HashMap<Integer, ItemStack> input = LegacyAstroMinerRecipes.inputs();
        final ArrayList<PositionedStack> display = new ArrayList<>(input.size());

        for (final Entry<Integer, ItemStack> slot : new TreeMap<>(input).entrySet()) {
            final int[] pos = position(slot.getKey());
            if (pos != null) {
                display.add(new PositionedStack(slot.getValue(), pos[0], pos[1]));
            }
        }
        return display;
    }
}
