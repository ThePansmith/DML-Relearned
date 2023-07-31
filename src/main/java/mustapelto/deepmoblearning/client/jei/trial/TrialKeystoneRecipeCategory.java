package mustapelto.deepmoblearning.client.jei.trial;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategory;
import mustapelto.deepmoblearning.DMLConstants;
import mustapelto.deepmoblearning.common.DMLRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class TrialKeystoneRecipeCategory implements IRecipeCategory<TrialKeystoneRecipeWrapper> {

    private final ItemStack catalyst;
    private final IDrawable background;

    public TrialKeystoneRecipeCategory(IGuiHelper guiHelper) {
        ResourceLocation base = new ResourceLocation(DMLConstants.ModInfo.ID, "textures/gui/jei/trial_keystone.png");
        this.catalyst = new ItemStack(DMLRegistry.BLOCK_TRIAL_KEYSTONE);
        this.background = guiHelper.createDrawable(base, 0, 0, 100, 18, 18, 0, 36, 18);
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull TrialKeystoneRecipeWrapper recipeWrapper, @Nonnull IIngredients ingredients) {
        IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
        guiItemStacks.init(0, true, 36, 18);
        guiItemStacks.set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));

        guiItemStacks.init(1, false, 64, 18);
        guiItemStacks.init(2, false, 82, 18);
        guiItemStacks.init(3, false, 100, 18);
        guiItemStacks.init(4, false, 118, 18);

        var outputs = ingredients.getOutputs(VanillaTypes.ITEM);
        for (int i = 0; i < Math.min(outputs.size(), 4); i++) {
            guiItemStacks.set(i + 1, outputs.get(i));
        }
    }

    public void addCatalysts(IModRegistry registry) {
        registry.addRecipeCatalyst(catalyst, getUid());
    }

    @Nonnull
    @Override
    public String getUid() {
        return DMLConstants.ModInfo.ID + ".trial_keystone";
    }

    @Nonnull
    @Override
    public String getTitle() {
        return catalyst.getDisplayName();
    }

    @Nonnull
    @Override
    public String getModName() {
        return DMLConstants.ModInfo.ID;
    }

    @Nonnull
    @Override
    public IDrawable getBackground() {
        return background;
    }
}
