package mustapelto.deepmoblearning.client.jei;

import mezz.jei.api.*;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;

import javax.annotation.Nonnull;

@JEIPlugin
public class DMLJeiPlugin implements IModPlugin {

    @Override
    public void registerItemSubtypes(@Nonnull ISubtypeRegistry subtypeRegistry) {
        // Register NBT handlers here
        // May or may not be necessary, depending on how the items in this mod are done
        // Currently, this shouldn't be necessary
    }

    @Override
    public void registerCategories(@Nonnull IRecipeCategoryRegistration registry) {
        // Register "categories" here
        // Will likely want a category for the Loot Fabricator, and for the Simulation Chamber
        // (or just 1, if you want them all on one page)
        // register like this:

        // registry.addRecipeCategories(new SimulationChamberCategory());

        // where "SimulationChamberCategory" implements IRecipeCategory
    }

    @Override
    public void register(@Nonnull IModRegistry registry) {
        // Register other stuff here
        // Most of the work can probably instead be done in the IRecipeCategory classes
    }

    @Override
    public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime) {
        // Use this to get and cache an instance of IJeiRuntime if you need it somewhere for any reason
    }
}
