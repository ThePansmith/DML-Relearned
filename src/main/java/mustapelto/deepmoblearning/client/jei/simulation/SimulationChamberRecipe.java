package mustapelto.deepmoblearning.client.jei.simulation;

import mustapelto.deepmoblearning.common.DMLRegistry;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SimulationChamberRecipe {

    public static List<SimulationChamberRecipe> recipes = new ArrayList<>();
    public final ItemStack dataModel;
    public final ItemStack input = new ItemStack(DMLRegistry.ITEM_POLYMER_CLAY);
    public final ItemStack livingOutput;
    public final ItemStack pristineOutput;


    private SimulationChamberRecipe(ItemStack dataModel, ItemStack livingOutput, ItemStack pristineOutput) {
        this.dataModel = dataModel;
        this.livingOutput = livingOutput;
        this.pristineOutput = pristineOutput;
    }

    public static void addRecipe(ItemStack dataModel, ItemStack livingOutput, ItemStack pristineOutput) {
        recipes.add(new SimulationChamberRecipe(dataModel, livingOutput, pristineOutput));
    }
}
