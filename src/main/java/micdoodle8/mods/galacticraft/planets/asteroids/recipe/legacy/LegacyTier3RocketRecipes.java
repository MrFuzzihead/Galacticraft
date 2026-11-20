package micdoodle8.mods.galacticraft.planets.asteroids.recipe.legacy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import micdoodle8.mods.galacticraft.api.GalacticraftRegistry;
import micdoodle8.mods.galacticraft.core.items.GCItems;
import micdoodle8.mods.galacticraft.core.recipe.NasaWorkbenchRecipe;
import micdoodle8.mods.galacticraft.core.recipe.legacy.LegacyCargoLayout;
import micdoodle8.mods.galacticraft.core.util.CompatGates;
import micdoodle8.mods.galacticraft.planets.asteroids.items.AsteroidsItems;

/**
 * The upstream Tier-3 (Asteroids) rocket NASA-bench recipes: one basic rocket plus seven cargo variants.
 *
 * <p>
 * GTNH commented these out in <code>459cfc415</code> and deleted them in <code>697f953cd</code>. They lived in
 * {@code AsteroidsModule} rather than {@code RecipeManagerAsteroids}, alongside the planet registration &mdash; which
 * is why a recipe-focused audit that only looked at the recipe managers missed them. The Asteroids dimension is only
 * reachable with a Tier-3 rocket, so this is on the critical path even though it is not the Moon.
 *
 * <p>
 * Layout from {@code ContainerSchematicTier3Rocket} (identical to the Tier-2 container): 1 heavy nose cone, 2&ndash;11
 * heavy plating T3 ({@code basicItem} meta 0), 12 booster, 13/14 T3 fins (meta 2), 15 T3 engine (meta 1), 16 booster,
 * 17/18 T3 fins, 19&ndash;21 cargo add-ons. The container and {@code SlotSchematicTier3Rocket} are unchanged from
 * upstream, so no GUI work is needed.
 */
public final class LegacyTier3RocketRecipes {

    /** Cargo add-on slot indices shared with {@code ContainerSchematicTier3Rocket}. */
    public static final int[] CARGO_SLOTS = { 19, 20, 21 };

    /** Number of input slots in the Tier-3 bench map (1-21 inclusive). */
    public static final int SLOT_COUNT = 21;

    private LegacyTier3RocketRecipes() {}

    public static void register() {
        if (!CompatGates.useLegacyContent()) {
            return;
        }
        for (final Variant variant : variants()) {
            GalacticraftRegistry.addT3RocketRecipe(new NasaWorkbenchRecipe(variant.output, variant.input));
        }
    }

    /** The eight recipes, in registration order (basic first). Single source for the NEI page too. */
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

    private static Variant variant(int outputMeta, ItemStack slot19, ItemStack slot20, ItemStack slot21) {
        final HashMap<Integer, ItemStack> input = new HashMap<>(24);
        input.put(1, new ItemStack(AsteroidsItems.heavyNoseCone));
        for (int slot = 2; slot <= 11; slot++) {
            input.put(slot, new ItemStack(AsteroidsItems.basicItem, 1, 0));
        }
        input.put(12, new ItemStack(GCItems.rocketEngine, 1, 1));
        input.put(13, new ItemStack(AsteroidsItems.basicItem, 1, 2));
        input.put(14, new ItemStack(AsteroidsItems.basicItem, 1, 2));
        input.put(15, new ItemStack(AsteroidsItems.basicItem, 1, 1));
        input.put(16, new ItemStack(GCItems.rocketEngine, 1, 1));
        input.put(17, new ItemStack(AsteroidsItems.basicItem, 1, 2));
        input.put(18, new ItemStack(AsteroidsItems.basicItem, 1, 2));
        input.put(19, slot19);
        input.put(20, slot20);
        input.put(21, slot21);

        return new Variant(new ItemStack(AsteroidsItems.tier3Rocket, 1, outputMeta), input);
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
