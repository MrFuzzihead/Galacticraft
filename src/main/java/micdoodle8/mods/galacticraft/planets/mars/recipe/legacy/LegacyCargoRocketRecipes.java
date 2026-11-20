package micdoodle8.mods.galacticraft.planets.mars.recipe.legacy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.item.ItemStack;

import micdoodle8.mods.galacticraft.core.items.GCItems;
import micdoodle8.mods.galacticraft.core.util.CompatGates;
import micdoodle8.mods.galacticraft.core.util.RecipeUtil;
import micdoodle8.mods.galacticraft.planets.mars.items.MarsItems;
import micdoodle8.mods.galacticraft.planets.mars.util.MarsUtil;

/**
 * The three Cargo Rocket NASA-bench recipes (safe transport ship to Mars).
 *
 * <p>
 * GTNH removed these in <code>697f953cd</code>. They must be restored against the <em>current</em> bench layout, not
 * the 2017 one: {@code ContainerSchematicCargoRocket} was reworked in <code>df346caa0</code> and the chest recipes
 * reintegrated in <code>d18e22fa5</code>, and that layout is still what HEAD ships.
 *
 * <p>
 * Layout from the current container: slot 1 advanced wafer, 2&ndash;6 GalaxySpace gear, 7 nose cone, 8&ndash;15
 * reinforced plating T2 (eight plates), 16 engine, 17&ndash;20 fins, 21 cargo chest. The GalaxySpace slots (2&ndash;6)
 * are intentionally <em>not</em> pinned, matching upstream: the output does not depend on them.
 *
 * <p>
 * The chest metadata selects the cargo size and therefore the output: 3 &rarr; {@code spaceship:11}, 0 &rarr;
 * {@code :12}, 1 &rarr; {@code :13}.
 */
public final class LegacyCargoRocketRecipes {

    /** Number of input slots in the cargo bench map (1-21 inclusive). */
    public static final int SLOT_COUNT = 21;

    /** Chest metadata, paired with {@link #OUTPUT_METAS}. */
    public static final int[] CHEST_METAS = { 3, 0, 1 };

    /** {@code spaceship} damage value for each entry in {@link #CHEST_METAS}. */
    public static final int[] OUTPUT_METAS = { 11, 12, 13 };

    private LegacyCargoRocketRecipes() {}

    public static void register() {
        if (!CompatGates.useLegacyContent()) {
            return;
        }
        for (final Variant variant : variants()) {
            MarsUtil.adCargoRocketRecipe(variant.output, variant.input);
        }
    }

    /** The three recipes, in registration order. Single source for the NEI page too. */
    public static List<Variant> variants() {
        final List<Variant> variants = new ArrayList<>(CHEST_METAS.length);

        for (int i = 0; i < CHEST_METAS.length; i++) {
            variants.add(variant(OUTPUT_METAS[i], CHEST_METAS[i]));
        }
        return variants;
    }

    private static Variant variant(int outputMeta, int chestMeta) {
        final HashMap<Integer, ItemStack> input = new HashMap<>(24);
        input.put(1, new ItemStack(GCItems.basicItem, 1, 14));
        input.put(7, new ItemStack(GCItems.partNoseCone));
        for (int slot = 8; slot <= 15; slot++) {
            input.put(slot, new ItemStack(MarsItems.marsItemBasic, 1, 3));
        }
        input.put(16, new ItemStack(GCItems.rocketEngine, 1, 0));
        for (int slot = 17; slot <= 20; slot++) {
            input.put(slot, new ItemStack(GCItems.partFins));
        }
        input.put(21, RecipeUtil.getChestItemStack(1, chestMeta));

        return new Variant(new ItemStack(MarsItems.spaceship, 1, outputMeta), input);
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
