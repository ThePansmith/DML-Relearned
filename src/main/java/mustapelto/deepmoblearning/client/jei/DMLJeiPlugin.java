package mustapelto.deepmoblearning.client.jei;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mustapelto.deepmoblearning.client.jei.fabricator.*;
import mustapelto.deepmoblearning.client.jei.simulation.*;
import mustapelto.deepmoblearning.common.DMLRegistry;
import mustapelto.deepmoblearning.common.items.ItemDataModel;
import mustapelto.deepmoblearning.common.items.ItemPristineMatter;
import mustapelto.deepmoblearning.common.metadata.MetadataDataModel;
import mustapelto.deepmoblearning.common.util.DataModelHelper;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.*;

@JEIPlugin
public class DMLJeiPlugin implements IModPlugin {

    private static SimulationChamberCategory simChamberCategory;
    private static LootFabricatorCategory lootFabricatorCategory;

    @Override
    public void registerCategories(@Nonnull IRecipeCategoryRegistration registry) {
        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();

        simChamberCategory = new SimulationChamberCategory(guiHelper);
        lootFabricatorCategory = new LootFabricatorCategory(guiHelper);

        registry.addRecipeCategories(simChamberCategory);
        registry.addRecipeCategories(lootFabricatorCategory);
    }

    @Override
    public void register(@Nonnull IModRegistry registry) {

        // Simulation Chamber
        registry.handleRecipes(SimulationChamberRecipe.class, SimulationChamberWrapper::new, simChamberCategory.getUid());
        for (ItemDataModel itemDataModel : DMLRegistry.getDataModels()) {
            MetadataDataModel metadata = itemDataModel.getDataModelMetadata();
            if (!metadata.isEnabled()) continue;

            ItemStack dataModel = new ItemStack(itemDataModel);
            ItemStack livingMatter = metadata.getLivingMatter();
            ItemStack pristineMatter = metadata.getPristineMatter();

            DataModelHelper.setTierLevel(dataModel, 1);
            SimulationChamberRecipe.addRecipe(dataModel, livingMatter, pristineMatter);
        }
        registry.addRecipes(new ArrayList<>(SimulationChamberRecipe.recipes), simChamberCategory.getUid());
        simChamberCategory.addCatalysts(registry);

        // Loot Fabricator
        registry.handleRecipes(LootFabricatorRecipe.class, LootFabricatorWrapper::new, lootFabricatorCategory.getUid());
        Map<ItemStack, List<ItemStack>> pristineTables = new HashMap<>();
        for (ItemPristineMatter pristineMatter : DMLRegistry.getPristineMatters()) {
            ItemStack pristineStack = new ItemStack(pristineMatter);
            Optional<MetadataDataModel> metadataOptional = ItemPristineMatter.getDataModelMetadata(pristineStack);
            if (!metadataOptional.isPresent()) continue;

            MetadataDataModel metadata = metadataOptional.get();
            if (!metadata.isEnabled()) continue;

            pristineTables.put(pristineStack, metadata.getLootItems());
        }
        pristineTables.forEach((input, outputs) -> {
            outputs.forEach(output -> LootFabricatorRecipe.addRecipe(input, output));
        });
        registry.addRecipes(new ArrayList<>(LootFabricatorRecipe.recipes), lootFabricatorCategory.getUid());
        // consider adding this, though it conflicts with the current GUI tooltip if enabled
        // registry.addRecipeClickArea(GuiLootFabricator.class, 84, 22, 6, 36, lootFabricatorCategory.getUid());
        lootFabricatorCategory.addCatalysts(registry);

        // Glitch-Infused Ingot
        registry.addIngredientInfo(new ItemStack(DMLRegistry.ITEM_GLITCH_INGOT), VanillaTypes.ITEM,
                "deepmoblearning.glitch_ingot.jei_info_1", "",
                "deepmoblearning.glitch_ingot.jei_info_2",
                "deepmoblearning.glitch_ingot.jei_info_3", "",
                "deepmoblearning.glitch_ingot.jei_info_4");
    }
}
