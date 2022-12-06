package mustapelto.deepmoblearning.common.items;

import mustapelto.deepmoblearning.DMLRelearned;
import mustapelto.deepmoblearning.client.util.KeyboardHelper;
import mustapelto.deepmoblearning.common.DMLConfig;
import mustapelto.deepmoblearning.common.metadata.MetadataDataModel;
import mustapelto.deepmoblearning.common.metadata.MetadataDataModelTier;
import mustapelto.deepmoblearning.common.util.DMLRHelper;
import mustapelto.deepmoblearning.common.util.DataModelHelper;
import mustapelto.deepmoblearning.common.util.StringHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class ItemDataModel extends ItemBase {
    private final MetadataDataModel metadata;

    public ItemDataModel(MetadataDataModel metadata) {
        super(metadata.getDataModelRegistryID(), 1, DMLRHelper.isModLoaded(metadata.getModID()));
        this.metadata = metadata;
    }

    public MetadataDataModel getDataModelMetadata() {
        return metadata;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        Optional<MetadataDataModel> metadata = DataModelHelper.getDataModelMetadata(stack);

        if (!metadata.isPresent())
            return;

        if (!metadata.get().isEnabled()) {
            tooltip.add(TextFormatting.GRAY + I18n.format("deepmoblearning.data_model.disabled") + TextFormatting.RESET);
            return;
        }

        String extraToolTip = metadata.get().getExtraTooltip();
        if (!extraToolTip.equals("")) {
            tooltip.add(extraToolTip);
        }

        if (!KeyboardHelper.isHoldingSneakKey()) {
            String sneakString = StringHelper.getFormattedString(TextFormatting.ITALIC, KeyboardHelper.getSneakKeyName(), TextFormatting.GRAY);
            tooltip.add(TextFormatting.GRAY + I18n.format("deepmoblearning.general.more_info", sneakString) + TextFormatting.RESET);
        } else {
            if (!DMLConfig.MISC_SETTINGS.SHOW_TIER_IN_NAME) {
                // Tier not shown in item name -> show in tooltip
                String displayName = DataModelHelper.getTierDisplayNameFormatted(stack);
                tooltip.add(TextFormatting.RESET + I18n.format("deepmoblearning.data_model.tier", displayName) + TextFormatting.RESET);
            }

            if (!DataModelHelper.isMaxTier(stack)) {
                int currentData = DataModelHelper.getCurrentTierDataCount(stack);
                int requiredData = DataModelHelper.getTierRequiredData(stack);
                int currentKillMultiplier = DataModelHelper.getTierKillMultiplier(stack);
                tooltip.add(TextFormatting.RESET + I18n.format("deepmoblearning.data_model.data_collected", TextFormatting.GRAY + String.valueOf(currentData), String.valueOf(requiredData) + TextFormatting.RESET));
                tooltip.add(TextFormatting.RESET + I18n.format("deepmoblearning.data_model.kill_multiplier", TextFormatting.GRAY + String.valueOf(currentKillMultiplier) + TextFormatting.RESET));
            }

            int rfCost = metadata.get().getSimulationRFCost();
            tooltip.add(TextFormatting.RESET + I18n.format("deepmoblearning.data_model.rf_cost", TextFormatting.GRAY + String.valueOf(rfCost)) + TextFormatting.RESET);

            ItemStack livingMatter = metadata.get().getLivingMatter();
            ItemLivingMatter livingMatterItem = (ItemLivingMatter) livingMatter.getItem();
            tooltip.add(TextFormatting.RESET + I18n.format("deepmoblearning.data_model.type", livingMatterItem.getLivingMatterData().getDisplayNameFormatted()));

            boolean canSimulate = DataModelHelper.canSimulate(stack);
            if (!canSimulate) {
                tooltip.add(TextFormatting.RESET + "" + TextFormatting.RED + I18n.format("deepmoblearning.data_model.cannot_simulate") + TextFormatting.RESET);
            }
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        Optional<MetadataDataModel> metadata = DataModelHelper.getDataModelMetadata(stack);
        if (!metadata.isPresent())
            return "";

        String name = DMLRelearned.proxy.getLocalizedString("deepmoblearning.data_model.display_name", metadata.get().getDisplayName());
        String tier = "";
        if (DMLConfig.MISC_SETTINGS.SHOW_TIER_IN_NAME) {
            Optional<MetadataDataModelTier> tierData = DataModelHelper.getTierData(stack);
            if (tierData.isPresent()) {
                String tierName = tierData.get().getDisplayName();
                TextFormatting tierColor = tierData.get().getDisplayColor();
                tier = StringHelper.getFormattedString(String.format(" (%s)", tierName), tierColor);
            }
        }
        return StringHelper.getFormattedString(name + tier, TextFormatting.AQUA);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (metadata.isEnabled()) {
            super.getSubItems(tab, items);
        }
    }
}
