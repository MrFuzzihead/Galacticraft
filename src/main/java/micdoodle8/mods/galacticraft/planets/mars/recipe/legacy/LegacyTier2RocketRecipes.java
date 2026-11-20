package micdoodle8.mods.galacticraft.planets.mars.recipe.legacy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import micdoodle8.mods.galacticraft.core.items.GCItems;
import micdoodle8.mods.galacticraft.core.recipe.legacy.LegacyCargoLayout;
import micdoodle8.mods.galacticraft.core.util.CompatGates;
import micdoodle8.mods.galacticraft.planets.mars.items.MarsItems;
import micdoodle8.mods.galacticraft.planets.mars.util.MarsUtil;

/**
 * The upstream Tier-2 (Mars) rocket NASA-bench recipes: one basic rocket plus seven cargo variants.
 *
 * <p>
 * GTNH commented these out in <code>459cfc415</code> and deleted them in <code>697f953cd</code> ("Move gregified
 * recipes from GC to coremod"). Unlike the Cargo Rocket, the Tier-2 bench container and its
 * {@code SlotSchematicTier2Rocket} whitelist were never changed, so the upstream recipe still matches the current GUI
 * exactly.
 *
 * <p>
 * Layout from {@code ContainerSchematicTier2Rocket}: 1 nose cone, 2&ndash;11 reinforced plating T2
 * ({@code marsItemBasic} meta 3), 12 booster, 13/14 fins, 15 engine, 16 booster, 17/18 fins, 19&ndash;21 cargo add-ons
 * (pinned, see {@link LegacyCargoLayout}).
 */
public final class LegacyTier2RocketRecipes {

    /** Cargo add-on slot indices shared with {@code ContainerSchematicTier2Rocket}. */
    public static final int[] CARGO_SLOTS = { 19, 20, 21 };

    /** Number of input slots in the Tier-2 bench map (1-21 inclusive). */
    public static final int SLOT_COUNT = 21;

    private LegacyTier2RocketRecipes() {}

    public static void register() {
        if (!CompatGates.useLegacyContent()) {
            return;
        }
        for (final Variant variant : variants()) {
            MarsUtil.addRocketBenchT2Recipe(variant.output, variant.input);
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
        input.put(1, new ItemStack(GCItems.partNoseCone));
        for (int slot = 2; slot <= 11; slot++) {
            input.put(slot, new ItemStack(MarsItems.marsItemBasic, 1, 3));
        }
        input.put(12, new ItemStack(GCItems.rocketEngine, 1, 1));
        input.put(13, new ItemStack(GCItems.partFins));
        input.put(14, new ItemStack(GCItems.partFins));
        input.put(15, new ItemStack(GCItems.rocketEngine, 1, 0));
        input.put(16, new ItemStack(GCItems.rocketEngine, 1, 1));
        input.put(17, new ItemStack(GCItems.partFins));
        input.put(18, new ItemStack(GCItems.partFins));
        input.put(19, slot19);
        input.put(20, slot20);
        input.put(21, slot21);

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
