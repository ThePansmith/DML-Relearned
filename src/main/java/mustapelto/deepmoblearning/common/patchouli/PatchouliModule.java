package mustapelto.deepmoblearning.common.patchouli;

import mustapelto.deepmoblearning.DMLConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import vazkii.patchouli.api.PatchouliAPI;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

public class PatchouliModule {

    public static final NonNullList<IRecipe> dmlItemRecipes = NonNullList.create();

    public static void init() {
        ResourceLocation recipeLookup = new ResourceLocation(DMLConstants.ModInfo.ID, "patchouli_templates/recipelookup.json");

        Supplier<InputStream> recipeLookupSupplier = () -> {
            try {
                return Minecraft.getMinecraft().getResourceManager().getResource(recipeLookup).getInputStream();
            } catch (IOException e) {
                //
            }
            return null;
        };

        PatchouliAPI.instance.registerTemplateAsBuiltin(new ResourceLocation(DMLConstants.ModInfo.ID, "recipelookup"), recipeLookupSupplier);
    }


    public static void postInit() {
        /* Get the recipes that belong to us, check the output since other mods could have tweaked the recipe */
        ForgeRegistries.RECIPES.getEntries().forEach((r) -> {
            if(r.getValue().getRecipeOutput().getItem().getRegistryName().getNamespace().equals(DMLConstants.ModInfo.ID)) {
                dmlItemRecipes.add(r.getValue());
            }
            /* Add for blood magic addon aswell */
            //if(r.getValue().getRecipeOutput().getItem().getRegistryName().getNamespace().equals(ModConstants.MODID)) {
            //    dmlItemRecipes.add(r.getValue());
            //}
        });
        PatchouliAPI.instance.reloadBookContents();
    }
}
