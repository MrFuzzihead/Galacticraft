package micdoodle8.mods.galacticraft.core.recipe.optional;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import micdoodle8.mods.galacticraft.core.blocks.GCBlocks;
import micdoodle8.mods.galacticraft.core.items.GCItems;
import micdoodle8.mods.galacticraft.core.util.CompatGates;
import micdoodle8.mods.galacticraft.core.util.ConfigManagerCore;
import micdoodle8.mods.galacticraft.core.util.RecipeUtil;

/**
 * Crafting recipes for the two oxygen tanks GTNH added: Super Heavy (tier 4) and Ultra Heavy (tier 5).
 *
 * <p>
 * <strong>This is not a restoration.</strong> Upstream Galacticraft only ever had three tiers, so
 * {@link micdoodle8.mods.galacticraft.core.recipe.legacy.LegacyOxygenTankRecipes} deliberately stops at Heavy. GTNH
 * added the tier 4 and 5 items but never shipped recipes for them in this fork, which leaves both uncraftable outside
 * the GTNH pack. The recipes here are the ones FuzziTweaks used to provide, moved into the fork so they can live next
 * to the tanks themselves.
 *
 * <p>
 * Opt-in via {@code ConfigManagerCore.addGtnhOxygenTankTiers} (default {@code false}) so the default experience matches
 * original Galacticraft. Also requires {@link CompatGates#useLegacyContent()}: in the GTNH pack the coremod owns these
 * recipes and adding a second copy here would duplicate them.
 *
 * <p>
 * Shape is a ring of seven compressed plates with the canister in the middle and an oxygen pipe on top, matching the
 * lower tiers. Both canister metals get a variant, as FuzziTweaks did.
 */
public final class OptionalOxygenTankRecipes {

    private OptionalOxygenTankRecipes() {}

    public static void register() {
        if (!CompatGates.useLegacyContent() || !ConfigManagerCore.addGtnhOxygenTankTiers) {
            return;
        }

        for (int canisterMeta = 0; canisterMeta <= 1; canisterMeta++) {
            addTank(GCItems.oxTankSuperHeavy, "MPM", "MCM", "MMM", 'M', "compressedMeteoricIron", canisterMeta);
            addTank(GCItems.oxTankUltraHeavy, "DPD", "DCD", "DDD", 'D', "compressedDesh", canisterMeta);
        }
    }

    private static void addTank(Item tank, String top, String mid, String bottom, char matKey, String matOre,
            int canisterMeta) {
        RecipeUtil.addRecipe(
                new ItemStack(tank, 1, tank.getMaxDamage()),
                new Object[] { top, mid, bottom, matKey, matOre, 'C', new ItemStack(GCItems.canister, 1, canisterMeta),
                        'P', GCBlocks.oxygenPipe });
    }
}
