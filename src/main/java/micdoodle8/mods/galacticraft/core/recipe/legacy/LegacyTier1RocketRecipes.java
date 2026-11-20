package micdoodle8.mods.galacticraft.core.recipe.legacy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import micdoodle8.mods.galacticraft.core.items.GCItems;
import micdoodle8.mods.galacticraft.core.util.CompatGates;
import micdoodle8.mods.galacticraft.core.util.RecipeUtil;

/**
 * The upstream Tier-1 rocket NASA-bench recipes: one basic rocket plus seven cargo variants.
 *
 * <p>
 * GTNH commented these out in <code>459cfc415</code> ("removed recipes handled by GalaxySpace") and deleted them in
 * <code>697f953cd</code> ("Move gregified recipes from GC to coremod"), leaving the bench with no recipes at all
 * outside the coremod. {@code SlotRocketBench} still whitelists exactly these parts, so restoring the recipes is
 * sufficient &mdash; no GUI work is needed here, unlike the Moon Buggy and Astro Miner benches.
 *
 * <p>
 * The slot layout is the one {@code ContainerSchematicTier1Rocket} builds: 1 nose cone, 2&ndash;9 heavy plating, 10/11
 * fins, 12 engine, 13/14 fins, 15&ndash;17 cargo add-ons. Cargo capacity is the output damage value (0 = none, 1 = 18
 * slots, 2 = 36, 3 = 54); see {@link LegacyCargoLayout}.
 *
 * <p>
 * The add-on slots are always present in the input map &mdash; {@code null} meaning "must be empty" &mdash; because
 * {@code NasaWorkbenchRecipe.matches} only checks the entries it is given. Omitting them would make the basic rocket
 * also match when a chest is placed in slot 15.
 */
public final class LegacyTier1RocketRecipes {

    /** Cargo add-on slot indices shared with {@code ContainerSchematicTier1Rocket}. */
    public static final int[] CARGO_SLOTS = { 15, 16, 17 };

    /** Number of input slots in the Tier-1 bench map (1-17 inclusive). */
    public static final int SLOT_COUNT = 17;

    private LegacyTier1RocketRecipes() {}

    public static void register() {
        if (!CompatGates.useLegacyContent()) {
            return;
        }
        for (final Variant variant : variants()) {
            RecipeUtil.addRocketBenchRecipe(variant.output, variant.input);
        }
    }

    /**
     * The eight upstream recipes, in registration order (basic first).
     *
     * <p>
     * Kept as the single source of truth so the NEI handler can render exactly what is registered.
     */
    public static List<Variant> variants() {
        final ItemStack chest = new ItemStack(Blocks.chest);
        final List<Variant> variants = new ArrayList<>(LegacyCargoLayout.COUNT);

        for (int i = 0; i < LegacyCargoLayout.COUNT; i++) {
            final int mask = LegacyCargoLayout.MASKS[i];
            variants.add(
                    variant(
                            LegacyCargoLayout.OUTPUT_METAS[i],
                            LegacyCargoLayout.hasChest(mask, 0) ? chest : null,
                            LegacyCargoLayout.hasChest(mask, 1) ? chest : null,
                            LegacyCargoLayout.hasChest(mask, 2) ? chest : null));
        }
        return variants;
    }

    /**
     * Builds one rocket bench input map.
     *
     * @param outputMeta the {@code rocketTier1} damage value (cargo tier)
     * @param slot15     chest in add-on slot 15, or {@code null} for "must be empty"
     * @param slot16     chest in add-on slot 16, or {@code null}
     * @param slot17     chest in add-on slot 17, or {@code null}
     */
    public static Variant variant(int outputMeta, ItemStack slot15, ItemStack slot16, ItemStack slot17) {
        final HashMap<Integer, ItemStack> input = new HashMap<>(20);
        input.put(1, new ItemStack(GCItems.partNoseCone));
        for (int slot = 2; slot <= 9; slot++) {
            input.put(slot, new ItemStack(GCItems.heavyPlatingTier1));
        }
        input.put(10, new ItemStack(GCItems.partFins));
        input.put(11, new ItemStack(GCItems.partFins));
        input.put(12, new ItemStack(GCItems.rocketEngine, 1, 0));
        input.put(13, new ItemStack(GCItems.partFins));
        input.put(14, new ItemStack(GCItems.partFins));
        input.put(15, slot15);
        input.put(16, slot16);
        input.put(17, slot17);

        return new Variant(new ItemStack(GCItems.rocketTier1, 1, outputMeta), input);
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
