package mustapelto.deepmoblearning.client.jei.trial;

import com.google.common.collect.ImmutableList;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import mustapelto.deepmoblearning.DMLConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class TrialKeystoneRecipeWrapper implements IRecipeWrapper {

    private final ItemStack input;
    private final String keyType;
    private final String keyTier;
    private final ImmutableList<ItemStack> outputs;

    public TrialKeystoneRecipeWrapper(TrialKeystoneRecipe recipe) {
        this.input = recipe.input;
        this.outputs = recipe.outputs;
        this.keyType = recipe.keyType;
        this.keyTier = recipe.keyTier;
    }

    @Override
    public void getIngredients(@Nonnull IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.ITEM, input);
        ingredients.setOutputs(VanillaTypes.ITEM, outputs);
    }

    @Override
    public void drawInfo(@Nonnull Minecraft mc, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        mc.fontRenderer.drawStringWithShadow(keyTier, 52 - mc.fontRenderer.getStringWidth(keyTier), 4, DMLConstants.Gui.Colors.WHITE);
        mc.fontRenderer.drawStringWithShadow(keyType, 64, 4, DMLConstants.Gui.Colors.WHITE);
    }
}
