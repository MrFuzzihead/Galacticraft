package micdoodle8.mods.galacticraft.core.recipe.legacy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.item.ItemStack;

import micdoodle8.mods.galacticraft.core.items.GCItems;
import micdoodle8.mods.galacticraft.core.util.CompatGates;
import micdoodle8.mods.galacticraft.core.util.RecipeUtil;

/**
 * The upstream Moon Buggy bench recipes: one basic buggy plus seven cargo variants.
 *
 * <p>
 * GTNH commented these out in <code>459cfc415</code> and deleted them in <code>697f953cd</code> ("Move gregified
 * recipes from GC to coremod"). Unlike the rockets, the buggy bench was also physically redesigned (19 craft slots
 * became 35, with GregTech stainless sticks and screws), so the recipes only become placeable again together with
 * {@code ContainerBuggyBench#addLegacySlots} and {@code SlotBuggyBench#isLegacyItemValid}.
 *
 * <p>
 * Layout from the restored legacy bench: 11 heavy-plating body slots (1&ndash;5, 7&ndash;12) with the cockpit seat in
 * slot 6, four wheels in 13&ndash;16, and three cargo add-ons in 17&ndash;19. Unlike the rockets the add-ons take a
 * buggy storage module ({@code partBuggy} meta 2), not a chest, so the cargo depth is still the output damage value
 * (0&ndash;3); see {@link LegacyCargoLayout}.
 */
public final class LegacyBuggyRecipes {

    /** Cargo add-on slot indices shared with {@code ContainerBuggyBench}. */
    public static final int[] CARGO_SLOTS = { 17, 18, 19 };

    /** Number of input slots in the legacy buggy bench (1-19 inclusive). */
    public static final int SLOT_COUNT = 19;

    /** {@code partBuggy} damage value of the storage module that fills a cargo add-on slot. */
    public static final int STORAGE_MODULE_META = 2;

    private LegacyBuggyRecipes() {}

    public static void register() {
        if (!CompatGates.useLegacyContent()) {
            return;
        }
        for (final Variant variant : variants()) {
            RecipeUtil.addBuggyBenchRecipe(variant.output, variant.input);
        }
    }

    /** The eight recipes, in registration order (basic first). Single source for the NEI page too. */
    public static List<Variant> variants() {
        final ItemStack storage = new ItemStack(GCItems.partBuggy, 1, STORAGE_MODULE_META);
        final List<Variant> variants = new ArrayList<>(LegacyCargoLayout.COUNT);

        for (int i = 0; i < LegacyCargoLayout.COUNT; i++) {
            final int mask = LegacyCargoLayout.MASKS[i];
            variants.add(
                    variant(
                            LegacyCargoLayout.OUTPUT_METAS[i],
                            LegacyCargoLayout.hasChest(mask, 0) ? storage : null,
                            LegacyCargoLayout.hasChest(mask, 1) ? storage : null,
                            LegacyCargoLayout.hasChest(mask, 2) ? storage : null));
        }
        return variants;
    }

    private static Variant variant(int outputMeta, ItemStack slot17, ItemStack slot18, ItemStack slot19) {
        final HashMap<Integer, ItemStack> input = new HashMap<>(24);
        for (int slot = 1; slot <= 12; slot++) {
            input.put(
                    slot,
                    slot == 6 ? new ItemStack(GCItems.partBuggy, 1, 1) : new ItemStack(GCItems.heavyPlatingTier1));
        }
        for (int slot = 13; slot <= 16; slot++) {
            input.put(slot, new ItemStack(GCItems.partBuggy, 1, 0));
        }
        input.put(17, slot17);
        input.put(18, slot18);
        input.put(19, slot19);

        return new Variant(new ItemStack(GCItems.buggy, 1, outputMeta), input);
    }

    /** One bench recipe: the output stack and its slot map. */
    public static final class Variant {

        public final ItemStack output;
        public final HashMap<Integer, ItemStack> input;

        Variant(ItemStack output, HashMap<Integer, ItemStack> input) {
            this.output = output;
            this.input = input;
        }
    }
}
