package mustapelto.deepmoblearning.client.jei.trial;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TrialKeystoneRecipe {

    public static List<TrialKeystoneRecipe> recipes = new ArrayList<>();
    public final ItemStack input;
    public final String keyType;
    public final String keyTier;
    public final ImmutableList<ItemStack> outputs;

    private TrialKeystoneRecipe(ItemStack input, ImmutableList<ItemStack> outputs, String keyType, String keyTier) {
        this.input = input;
        this.outputs = outputs;
        this.keyType = keyType;
        this.keyTier = keyTier;
    }

    public static void addRecipe(ItemStack input, ImmutableList<ItemStack> outputs, String keyType, String keyTier) {
        recipes.add(new TrialKeystoneRecipe(input, outputs, keyType, keyTier));
    }
}
