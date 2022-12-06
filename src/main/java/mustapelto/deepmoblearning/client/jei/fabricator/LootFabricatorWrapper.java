package mustapelto.deepmoblearning.client.jei.fabricator;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import javax.annotation.Nonnull;

public class LootFabricatorWrapper implements IRecipeWrapper {

    private final NonNullList<ItemStack> inputs;
    private final NonNullList<ItemStack> outputs;

    public LootFabricatorWrapper(LootFabricatorRecipe recipe) {
        this.inputs = NonNullList.create();
        this.inputs.add(recipe.input);
        this.outputs = NonNullList.create();
        this.outputs.add(recipe.output);
    }

    @Override
    public void getIngredients(@Nonnull IIngredients ingredients) {
        ingredients.setInputs(VanillaTypes.ITEM, inputs);
        ingredients.setOutputs(VanillaTypes.ITEM, outputs);
    }
}
