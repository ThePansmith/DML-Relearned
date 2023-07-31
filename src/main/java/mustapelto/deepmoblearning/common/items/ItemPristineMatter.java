package mustapelto.deepmoblearning.common.items;

import mustapelto.deepmoblearning.DMLRelearned;
import mustapelto.deepmoblearning.common.metadata.MetadataDataModel;
import mustapelto.deepmoblearning.common.util.DMLRHelper;
import mustapelto.deepmoblearning.common.util.ItemStackHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class ItemPristineMatter extends ItemBase {
    private final MetadataDataModel metadata;

    public ItemPristineMatter(MetadataDataModel metadata) {
        super(metadata.getPristineMatterRegistryID(), 64, DMLRHelper.isModLoaded(metadata.getModID()));
        this.metadata = metadata;
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return true; // Make items glow
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return DMLRelearned.proxy.getLocalizedString("deepmoblearning.pristine_matter.display_name", metadata.getDisplayName());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(TextFormatting.AQUA + I18n.format("deepmoblearning.pristine_matter.loot_items") + TextFormatting.RESET);
        for (ItemStack lootStack : metadata.getLootItems()) {
            tooltip.add(lootStack.getDisplayName());
        }
    }

    public static Optional<MetadataDataModel> getDataModelMetadata(ItemStack stack) {
        return ItemStackHelper.isPristineMatter(stack) ?
               Optional.of(((ItemPristineMatter) stack.getItem()).metadata) :
               Optional.empty();
    }
}
