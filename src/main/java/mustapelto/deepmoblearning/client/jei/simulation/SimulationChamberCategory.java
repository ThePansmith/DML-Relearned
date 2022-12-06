package mustapelto.deepmoblearning.client.jei.simulation;

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
import java.util.List;

public class SimulationChamberCategory implements IRecipeCategory<SimulationChamberWrapper> {

    private final ItemStack catalyst;
    private final IDrawable background;
    private final IDrawableAnimated progress;

    public SimulationChamberCategory(IGuiHelper guiHelper) {
        ResourceLocation base = new ResourceLocation(DMLConstants.ModInfo.ID, "textures/gui/jei/simulation_chamber.png");
        this.catalyst = new ItemStack(DMLRegistry.BLOCK_SIMULATION_CHAMBER);
        this.background = guiHelper.createDrawable(base, 0, 0, 116, 43);
        IDrawableStatic progress = guiHelper.createDrawable(base, 0, 43, 35, 6);
        this.progress = guiHelper.createAnimatedDrawable(progress, 120, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayout layout, @Nonnull SimulationChamberWrapper wrapper, @Nonnull IIngredients ingredients) {
        IGuiItemStackGroup guiItemStacks = layout.getItemStacks();
        List<List<ItemStack>> inputs = ingredients.getInputs(VanillaTypes.ITEM);
        List<List<ItemStack>> outputs = ingredients.getOutputs(VanillaTypes.ITEM);

        guiItemStacks.init(0, true, 3, 3);
        guiItemStacks.set(0, inputs.get(0));
        guiItemStacks.init(1, true, 27, 3);
        guiItemStacks.set(1, inputs.get(1));

        guiItemStacks.init(2, false, 95, 3);
        guiItemStacks.set(2, outputs.get(0));
        guiItemStacks.init(3, false, 75, 25);
        guiItemStacks.set(3, outputs.get(1));
    }

    @Override
    public void drawExtras(@Nonnull Minecraft minecraft) {
        progress.draw(minecraft, 52, 9);
    }

    @Nonnull
    @Override
    public String getUid() {
        return DMLConstants.ModInfo.ID + ".simulation_chamber";
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

    public void addCatalysts(IModRegistry registry) {
        registry.addRecipeCatalyst(catalyst, getUid());
    }
}
