package micdoodle8.mods.galacticraft.core.nei;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.minecraft.item.ItemStack;

import codechicken.nei.PositionedStack;
import micdoodle8.mods.galacticraft.core.recipe.legacy.LegacyBuggyRecipes;

/**
 * NEI coordinates for the restored (non-GTNH) Moon Buggy bench page.
 *
 * <p>
 * GTNH registers {@code BuggyRecipeHandler} but never populates the {@code NEIGalacticraftConfig#buggyBenchRecipes} map
 * it reads, so the page renders and lists nothing. This fills it from {@link LegacyBuggyRecipes}.
 *
 * <p>
 * Coordinates are the slot positions of {@code ContainerBuggyBench#addLegacySlots} minus the crop upstream's handler
 * applies in {@code drawBackground} ({@code drawTexturedModalRect(0, 0, 3, 4, 168, 130)}), i.e. {@code (x-3, y-4)}:
 *
 * <pre>
 *   slot 0 (output)      (142, 106) -> (139, 102)
 *   slots 1-12 body      3 columns at x 39/57/75, 4 rows at y 41/59/77/95
 *   slots 13-16 wheels   x 21/93, y 41/95
 *   slots 17-19 add-ons  x 93/119/145, y 12
 * </pre>
 */
public final class LegacyBuggyBenchNei {

    /** Output bullet position. */
    public static final int[] OUTPUT = { 139, 102 };

    /** Highest input slot index. */
    public static final int MAX_SLOT = LegacyBuggyRecipes.SLOT_COUNT;

    /** NEI x/y per bench slot index. Index 0 is the output and is intentionally null. */
    private static final int[][] SLOT_POS = new int[MAX_SLOT + 1][];

    static {
        // 4x3 body: slot = column * 4 + row + 1
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 3; col++) {
                SLOT_POS[col * 4 + row + 1] = new int[] { 36 + col * 18, 37 + row * 18 };
            }
        }
        // 2x2 wheels: slot = column * 2 + row + 13
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) {
                SLOT_POS[col * 2 + row + 13] = new int[] { 18 + col * 72, 37 + row * 54 };
            }
        }
        // 3 cargo add-ons
        for (int i = 0; i < 3; i++) {
            SLOT_POS[17 + i] = new int[] { 90 + i * 26, 8 };
        }
    }

    private LegacyBuggyBenchNei() {}

    /** NEI coordinate of a bench slot, or {@code null} if it has none. Returns a copy. */
    public static int[] position(int slot) {
        if (slot < 0 || slot >= SLOT_POS.length || SLOT_POS[slot] == null) {
            return null;
        }
        return SLOT_POS[slot].clone();
    }

    /**
     * Builds the NEI display map for one recipe, skipping slots the recipe pins to empty.
     */
    public static HashMap<Integer, PositionedStack> displayMap(HashMap<Integer, ItemStack> input) {
        final HashMap<Integer, PositionedStack> display = new HashMap<>(input.size());

        for (final Entry<Integer, ItemStack> slot : new TreeMap<>(input).entrySet()) {
            final ItemStack stack = slot.getValue();
            if (stack == null) {
                continue;
            }
            final int[] pos = position(slot.getKey());
            if (pos != null) {
                display.put(slot.getKey(), new PositionedStack(stack, pos[0], pos[1]));
            }
        }
        return display;
    }
}
