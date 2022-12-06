package mustapelto.deepmoblearning.client.jei.fabricator;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.gui.*;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategory;
import mustapelto.deepmoblearning.DMLConstants;
import mustapelto.deepmoblearning.common.DMLRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class LootFabricatorCategory implements IRecipeCategory<LootFabricatorWrapper> {

    private final ItemStack catalyst;
    private final IDrawable background;
    private final IDrawableAnimated progress;

    public LootFabricatorCategory(IGuiHelper guiHelper) {
        ResourceLocation base = new ResourceLocation(DMLConstants.ModInfo.ID, "textures/gui/jei/extraction_chamber.png");
        this.catalyst = new ItemStack(DMLRegistry.BLOCK_LOOT_FABRICATOR);
        this.background = guiHelper.createDrawable(base, 0, 0, 103, 30);
        IDrawableStatic progress = guiHelper.createDrawable(base, 0, 30, 35, 6);
        this.progress = guiHelper.createAnimatedDrawable(progress, 120, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, @Nonnull LootFabricatorWrapper wrapper, IIngredients ingredients) {
        IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
        guiItemStacks.init(0, true, 8, 6);
        guiItemStacks.set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));

        guiItemStacks.init(1, false, 76, 6);
        guiItemStacks.set(1, ingredients.getOutputs(VanillaTypes.ITEM).get(0));
    }

    @Nonnull
    @Override
    public String getUid() {
        return DMLConstants.ModInfo.ID + ".extraction_chamber";
    }

    @Nonnull
    @Override
    public String getTitle() {
        return catalyst.getDisplayName();
    }

    @Nonnull
    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void drawExtras(@Nonnull Minecraft minecraft) {
        progress.draw(minecraft, 34, 12);
    }

    @Nonnull
    @Override
    public String getModName() {
        return DMLConstants.ModInfo.ID;
    }

    public void addCatalysts(IModRegistry registry) {
        registry.addRecipeCatalyst(catalyst, getUid());
    }
}
