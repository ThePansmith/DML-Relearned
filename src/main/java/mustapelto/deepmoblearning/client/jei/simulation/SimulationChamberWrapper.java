package mustapelto.deepmoblearning.client.jei.simulation;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import mustapelto.deepmoblearning.DMLConstants.Gui.Colors;
import mustapelto.deepmoblearning.common.util.DataModelHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import javax.annotation.Nonnull;

public class SimulationChamberWrapper implements IRecipeWrapper {

    private long ticks = 0;
    private long lastWorldTime;
    private final ItemStack dataModel;
    private final NonNullList<ItemStack> inputs;
    private final NonNullList<ItemStack> outputs;

    public SimulationChamberWrapper(SimulationChamberRecipe recipe) {
        this.dataModel = recipe.dataModel;

        this.inputs = NonNullList.create();
        this.inputs.add(dataModel);
        this.inputs.add(recipe.input);

        this.outputs = NonNullList.create();
        this.outputs.add(recipe.livingOutput);
        this.outputs.add(recipe.pristineOutput);
    }


    @Override
    public void getIngredients(@Nonnull IIngredients ingredients) {
        ingredients.setInputs(VanillaTypes.ITEM, inputs);
        ingredients.setOutputs(VanillaTypes.ITEM, outputs);
    }

    @Override
    public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        renderPristineChance(minecraft);

        if (lastWorldTime == minecraft.world.getTotalWorldTime()) {
            return;
        } else {
            ticks++;
            lastWorldTime = minecraft.world.getTotalWorldTime();
        }

        if (ticks % 30 == 0) {
            cycleTier();
        }
    }

    private void cycleTier() {
        if (DataModelHelper.isMaxTier(dataModel)) {
            DataModelHelper.setTierLevel(dataModel, 1);
        } else {
            DataModelHelper.setTierLevel(dataModel, DataModelHelper.getTier(dataModel) + 1);
        }
    }

    private void renderPristineChance(@Nonnull Minecraft minecraft) {
        FontRenderer render = minecraft.fontRenderer;

        String tierName = DataModelHelper.getTierDisplayNameFormatted(dataModel);
        render.drawStringWithShadow(tierName, 70 - render.getStringWidth(tierName), 30, Colors.WHITE);

        int pristineChance = DataModelHelper.getPristineChance(dataModel);
        String chanceText = pristineChance + "%";
        render.drawStringWithShadow(chanceText, 97, 31, Colors.WHITE);
    }
}
