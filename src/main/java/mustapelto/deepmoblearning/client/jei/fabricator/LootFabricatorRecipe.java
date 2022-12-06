package mustapelto.deepmoblearning.client.jei.fabricator;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class LootFabricatorRecipe {

    public static List<LootFabricatorRecipe> recipes = new ArrayList<>();
    public final ItemStack input;
    public final ItemStack output;

    public LootFabricatorRecipe(ItemStack input, ItemStack output) {
        this.input = input;
        this.output = output;
    }

    public static void addRecipe(ItemStack input, ItemStack output) {
        recipes.add(new LootFabricatorRecipe(input, output));
    }
}
