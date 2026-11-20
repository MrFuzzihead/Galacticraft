package micdoodle8.mods.galacticraft.planets.asteroids.recipe.legacy;

import java.util.HashMap;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import micdoodle8.mods.galacticraft.api.GalacticraftRegistry;
import micdoodle8.mods.galacticraft.core.items.GCItems;
import micdoodle8.mods.galacticraft.core.recipe.NasaWorkbenchRecipe;
import micdoodle8.mods.galacticraft.core.util.CompatGates;
import micdoodle8.mods.galacticraft.planets.asteroids.items.AsteroidsItems;

/**
 * The upstream Astro Miner NASA-bench recipe.
 *
 * <p>
 * GTNH commented this out in <code>459cfc415</code> ("removed recipes handled by GalaxySpace") and deleted it in
 * <code>697f953cd</code>, from the same block as the Tier-3 rocket recipes in {@code AsteroidsModule}. Without it the
 * Asteroids are technically reachable but the machine that makes them worth visiting cannot be built.
 *
 * <p>
 * Slot layout from upstream's 14-slot bench, restored by {@code ContainerSchematicAstroMiner#addLegacySlots}: 4 top
 * layer, 5 middle layer, 3 bottom layer, 2 laser. Plating sits at 1/3/5/11, orion drives at 2/4/9/10/12, the advanced
 * wafer at 6, storage at 7/8, the laser beam core at 13 and the laser pole at 14.
 */
public final class LegacyAstroMinerRecipes {

    /** Number of input slots in the legacy bench (1-14 inclusive). */
    public static final int SLOT_COUNT = 14;

    private LegacyAstroMinerRecipes() {}

    public static void register() {
        if (!CompatGates.useLegacyContent()) {
            return;
        }
        GalacticraftRegistry
                .addAstroMinerRecipe(new NasaWorkbenchRecipe(new ItemStack(AsteroidsItems.astroMiner, 1, 0), inputs()));
    }

    /** The single Astro Miner bench recipe's slot map. Also the source for the NEI page. */
    public static HashMap<Integer, ItemStack> inputs() {
        final HashMap<Integer, ItemStack> input = new HashMap<>(20);

        // Tier 1 plating through the miner's shell
        input.put(1, new ItemStack(GCItems.heavyPlatingTier1));
        input.put(3, new ItemStack(GCItems.heavyPlatingTier1));
        input.put(5, new ItemStack(GCItems.heavyPlatingTier1));
        input.put(11, new ItemStack(GCItems.heavyPlatingTier1));

        // Orion drives
        input.put(2, new ItemStack(AsteroidsItems.orionDrive));
        input.put(4, new ItemStack(AsteroidsItems.orionDrive));
        input.put(9, new ItemStack(AsteroidsItems.orionDrive));
        input.put(10, new ItemStack(AsteroidsItems.orionDrive));
        input.put(12, new ItemStack(AsteroidsItems.orionDrive));

        input.put(6, new ItemStack(GCItems.basicItem, 1, 14)); // advanced wafer
        input.put(7, new ItemStack(Blocks.chest));
        input.put(8, new ItemStack(Blocks.chest));
        input.put(13, new ItemStack(AsteroidsItems.basicItem, 1, 8)); // laser beam core
        input.put(14, new ItemStack(GCItems.flagPole)); // laser pole

        return input;
    }
}
