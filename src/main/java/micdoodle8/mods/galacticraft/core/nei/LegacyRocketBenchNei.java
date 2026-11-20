package micdoodle8.mods.galacticraft.core.nei;

import java.util.HashMap;
import java.util.Map.Entry;

import net.minecraft.item.ItemStack;

import codechicken.nei.PositionedStack;
import micdoodle8.mods.galacticraft.core.recipe.legacy.LegacyTier1RocketRecipes;

/**
 * NEI display for the restored Tier-1 rocket recipes.
 *
 * <p>
 * GTNH commented the {@code RocketT1RecipeHandler} registration out in <code>459cfc415</code> ("removed recipes handled
 * by GalaxySpace") and never populated {@code NEIGalacticraftConfig#rocketBenchRecipes}, so the bench is invisible in
 * NEI outside the GTNH pack. This re-registers the handler and fills the display map from
 * {@link LegacyTier1RocketRecipes} so NEI and the actual recipes cannot drift apart.
 *
 * <p>
 * Slot coordinates are {@code ContainerSchematicTier1Rocket}'s container coordinates minus the {@code (3, 4)} texture
 * crop that {@link RocketT1RecipeHandler#drawBackground} applies when drawing {@code rocketbench.png}. The output
 * bullet sits at {@code (139, 92)}.
 */
public final class LegacyRocketBenchNei {

    /** Container width/height used by the bench GUI, for reference only. */
    public static final int OUTPUT_X = 139;
    public static final int OUTPUT_Y = 92;

    /** NEI x/y per bench slot index (1-17). */
    private static final int[][] SLOT_POS = new int[18][];

    static {
        SLOT_POS[1] = new int[] { 45, 15 };
        for (int row = 0; row < 4; row++) {
            SLOT_POS[2 + row] = new int[] { 36, 33 + row * 18 };
            SLOT_POS[6 + row] = new int[] { 54, 33 + row * 18 };
        }
        SLOT_POS[10] = new int[] { 18, 87 };
        SLOT_POS[11] = new int[] { 18, 105 };
        SLOT_POS[12] = new int[] { 45, 105 };
        SLOT_POS[13] = new int[] { 72, 87 };
        SLOT_POS[14] = new int[] { 72, 105 };
        SLOT_POS[15] = new int[] { 90, 8 };
        SLOT_POS[16] = new int[] { 116, 8 };
        SLOT_POS[17] = new int[] { 142, 8 };
    }

    private LegacyRocketBenchNei() {}

    /**
     * NEI coordinate of a bench slot, or {@code null} if the slot has no display position. Returns a copy so callers
     * cannot mutate the table.
     *
     * @param slot bench slot index (1-17)
     */
    public static int[] neiPosition(int slot) {
        if (slot < 0 || slot >= SLOT_POS.length || SLOT_POS[slot] == null) {
            return null;
        }
        return SLOT_POS[slot].clone();
    }

    /**
     * Fills the Tier-1 rocket NEI display map. GTNH registers {@code RocketT1RecipeHandler} but never
     * populates the map it reads, so the page exists and lists nothing.
     */
    public static void populate(NEIGalacticraftConfig config) {
        for (final LegacyTier1RocketRecipes.Variant variant : LegacyTier1RocketRecipes.variants()) {
            final HashMap<Integer, PositionedStack> display = new HashMap<>(variant.input.size());
            for (final Entry<Integer, ItemStack> slot : variant.input.entrySet()) {
                final ItemStack stack = slot.getValue();
                if (stack == null) {
                    continue;
                }
                final int[] pos = SLOT_POS[slot.getKey()];
                display.put(slot.getKey(), new PositionedStack(stack, pos[0], pos[1]));
            }
            config.registerRocketBenchRecipe(display, new PositionedStack(variant.output, OUTPUT_X, OUTPUT_Y));
        }
    }
}
