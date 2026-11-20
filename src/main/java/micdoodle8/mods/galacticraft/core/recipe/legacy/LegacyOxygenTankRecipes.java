package micdoodle8.mods.galacticraft.core.recipe.legacy;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import micdoodle8.mods.galacticraft.core.items.GCItems;
import micdoodle8.mods.galacticraft.core.util.CompatGates;
import micdoodle8.mods.galacticraft.core.util.RecipeUtil;

/**
 * The upstream three-tier oxygen tank ladder.
 *
 * <p>
 * GTNH commit <code>3cfded1b6</code> deleted the Medium and Heavy recipes and re-materialed the Light one from
 * compressed copper to compressed aluminium, with no gate and no replacement outside the coremod. Restoring upstream
 * keeps the progression: each tier uses the metal the player has at that point of the game, so the capacity ladder
 * tracks the tech ladder.
 *
 * <pre>
 *   Light    compressed copper   lime wool    (1x3)
 *   Medium   compressed tin      orange wool  (2x3)
 *   Heavy    compressed steel    red wool     (3x3)
 * </pre>
 *
 * <p>
 * Outputs are empty tanks ({@code damage == maxDamage}), refillable in the Oxygen Compressor, and consume a tin
 * canister (meta 0) exactly as upstream did. GTNH's compressed-aluminium Light recipe stays available on the other side
 * of the gate in {@code RecipeManagerGC}.
 */
public final class LegacyOxygenTankRecipes {

    private static final int TIN_CANISTER = 0;

    private LegacyOxygenTankRecipes() {}

    public static void register() {
        if (!CompatGates.useLegacyContent()) {
            return;
        }
        addTank(GCItems.oxTankLight, new Object[] { "Z", "X", "Y" }, "compressedCopper", Blocks.wool, 5);
        addTank(GCItems.oxTankMedium, new Object[] { "ZZ", "XX", "YY" }, "compressedTin", Blocks.wool, 1);
        addTank(GCItems.oxTankHeavy, new Object[] { "ZZZ", "XXX", "YYY" }, "compressedSteel", Blocks.wool, 14);
    }

    /**
     * Registers one tank recipe (empty tank out, 7 plates + canister + wool in).
     *
     * @param tank     the oxygen tank item
     * @param shape    the 3 rows, where X is the canister, Y the metal and Z the wool
     * @param metalOre the ore-dictionary tag for the tank's metal
     * @param wool     the wool block
     * @param woolMeta the wool colour metadata
     */
    private static void addTank(Item tank, Object[] shape, String metalOre, Block wool, int woolMeta) {
        RecipeUtil.addRecipe(
                new ItemStack(tank, 1, tank.getMaxDamage()),
                new Object[] { shape[0], shape[1], shape[2], 'X', new ItemStack(GCItems.canister, 1, TIN_CANISTER), 'Y',
                        metalOre, 'Z', new ItemStack(wool, 1, woolMeta) });
    }
}
