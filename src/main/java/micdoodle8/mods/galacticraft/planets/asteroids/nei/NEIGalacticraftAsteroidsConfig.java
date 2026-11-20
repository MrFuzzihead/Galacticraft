package micdoodle8.mods.galacticraft.planets.asteroids.nei;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.item.ItemStack;

import codechicken.nei.PositionedStack;
import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import micdoodle8.mods.galacticraft.core.Constants;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.blocks.GCBlocks;
import micdoodle8.mods.galacticraft.core.nei.LegacyRocketBenchNei;
import micdoodle8.mods.galacticraft.core.nei.NasaWorkbenchOverlayHandler;
import micdoodle8.mods.galacticraft.core.util.CompatGates;
import micdoodle8.mods.galacticraft.planets.asteroids.blocks.AsteroidBlocks;
import micdoodle8.mods.galacticraft.planets.asteroids.client.gui.GuiSchematicAstroMiner;
import micdoodle8.mods.galacticraft.planets.asteroids.client.gui.GuiSchematicTier3Rocket;
import micdoodle8.mods.galacticraft.planets.asteroids.items.AsteroidsItems;
import micdoodle8.mods.galacticraft.planets.asteroids.recipe.legacy.LegacyTier3RocketRecipes;
import micdoodle8.mods.galacticraft.planets.mars.nei.NEIGalacticraftMarsConfig;

public class NEIGalacticraftAsteroidsConfig implements IConfigureNEI {

    private static final HashMap<ArrayList<PositionedStack>, PositionedStack> rocketBenchRecipes = new HashMap<>();
    private static final HashMap<ArrayList<PositionedStack>, PositionedStack> astroMinerRecipes = new HashMap<>();

    @Override
    public void loadConfig() {
        if (!GalacticraftCore.isGalaxySpaceLoaded) {
            RocketT3RecipeHandler rocketHandler = new RocketT3RecipeHandler();
            API.registerRecipeHandler(rocketHandler);
            API.registerUsageHandler(new RocketT3RecipeHandler());
            API.addRecipeCatalyst(new ItemStack(GCBlocks.nasaWorkbench), rocketHandler);
        }
        if (CompatGates.useLegacyContent()) {
            this.addLegacyRocketBenchRecipes();
            this.addLegacyAstroMinerRecipe();
        }
        AstroMinerRecipeHandler astroMinerHandler = new AstroMinerRecipeHandler();
        API.registerRecipeHandler(astroMinerHandler);
        API.registerUsageHandler(new AstroMinerRecipeHandler());
        API.addRecipeCatalyst(new ItemStack(GCBlocks.nasaWorkbench), astroMinerHandler);
        API.registerGuiOverlay(GuiSchematicTier3Rocket.class, "galacticraft.rocketT3", 8, 12);
        API.registerGuiOverlayHandler(
                GuiSchematicTier3Rocket.class,
                new NasaWorkbenchOverlayHandler(8, 12),
                "galacticraft.rocketT3");
        API.registerGuiOverlay(GuiSchematicAstroMiner.class, "galacticraft.astroMiner", 4, 16);
        API.registerGuiOverlayHandler(
                GuiSchematicAstroMiner.class,
                new NasaWorkbenchOverlayHandler(4, 16),
                "galacticraft.astroMiner");
        API.registerHighlightIdentifier(AsteroidBlocks.blockBasic, NEIGalacticraftMarsConfig.planetsHighlightHandler);
    }

    /**
     * Populates the Tier-3 rocket NEI page. GTNH deleted both the handler registration and the map population in
     * <code>697f953cd</code>; the recipes come from the legacy helper so NEI cannot drift from the registry.
     */
    /**
     * Populates the Astro Miner NEI page. GTNH registers {@code AstroMinerRecipeHandler} but never fills the map it
     * reads, so the page existed and listed nothing.
     */
    private void addLegacyAstroMinerRecipe() {
        this.registerAstroMinerRecipe(
                LegacyAstroMinerBenchNei.displayList(),
                new PositionedStack(
                        new ItemStack(AsteroidsItems.astroMiner, 1, 0),
                        LegacyAstroMinerBenchNei.OUTPUT[0],
                        LegacyAstroMinerBenchNei.OUTPUT[1]));
    }

    private void addLegacyRocketBenchRecipes() {
        for (final LegacyTier3RocketRecipes.Variant variant : LegacyTier3RocketRecipes.variants()) {
            this.registerRocketBenchRecipe(
                    LegacyRocketBenchNei.displayList(variant.input, LegacyRocketBenchNei.BENCH_TIER2_3),
                    new PositionedStack(
                            variant.output,
                            LegacyRocketBenchNei.OUTPUT_TIER2_3[0],
                            LegacyRocketBenchNei.OUTPUT_TIER2_3[1]));
        }
    }

    @Override
    public String getName() {
        return "Galacticraft Asteroids NEI Plugin";
    }

    @Override
    public String getVersion() {
        return Constants.VERSION;
    }

    public void registerRocketBenchRecipe(ArrayList<PositionedStack> input, PositionedStack output) {
        NEIGalacticraftAsteroidsConfig.rocketBenchRecipes.put(input, output);
    }

    public static Set<Map.Entry<ArrayList<PositionedStack>, PositionedStack>> getRocketBenchRecipes() {
        return NEIGalacticraftAsteroidsConfig.rocketBenchRecipes.entrySet();
    }

    public void registerAstroMinerRecipe(ArrayList<PositionedStack> input, PositionedStack output) {
        NEIGalacticraftAsteroidsConfig.astroMinerRecipes.put(input, output);
    }

    public static Set<Map.Entry<ArrayList<PositionedStack>, PositionedStack>> getAstroMinerRecipes() {
        return NEIGalacticraftAsteroidsConfig.astroMinerRecipes.entrySet();
    }
}
