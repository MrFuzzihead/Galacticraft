package micdoodle8.mods.galacticraft.core.recipe.legacy;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import micdoodle8.mods.galacticraft.core.blocks.GCBlocks;
import micdoodle8.mods.galacticraft.core.util.RecipeUtil;

/**
 * NASA Workbench block crafting recipe, as shipped by upstream Galacticraft.
 *
 * <p>
 * The GTNH fork removed this outright (commented out in <code>459cfc415</code>, deleted in <code>8405381d2</code>)
 * believing the coremod would supply it. The workbench is the only block that assembles a rocket, so without this
 * recipe the Tier-1 rocket is uncraftable and the Moon is unreachable in any pack that is not GTNH.
 */
public final class LegacyNasaWorkbenchRecipes {

    private LegacyNasaWorkbenchRecipes() {}

    public static void register() {
        // X = compressedSteel Z = crafting table
        // U = lever W = advanced wafer
        // Y = compressedSteel V = redstone torch
        RecipeUtil.addRecipe(
                new ItemStack(GCBlocks.nasaWorkbench),
                new Object[] { "XZX", "UWU", "YVY", 'U', Blocks.lever, 'V', Blocks.redstone_torch, 'W', "waferAdvanced",
                        'X', "compressedSteel", 'Y', "compressedSteel", 'Z', Blocks.crafting_table });
    }
}
