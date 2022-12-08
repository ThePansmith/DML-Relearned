package mustapelto.deepmoblearning.common.items;

import com.google.common.collect.ImmutableList;
import mustapelto.deepmoblearning.client.util.KeyboardHelper;
import mustapelto.deepmoblearning.common.metadata.MetadataManager;
import mustapelto.deepmoblearning.common.trials.AttunementData;
import mustapelto.deepmoblearning.common.trials.affix.TrialAffix;
import mustapelto.deepmoblearning.common.util.StringHelper;
import mustapelto.deepmoblearning.common.util.TrialKeyHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemTrialKey extends ItemBase {
    public ItemTrialKey() {
        super("trial_key", 1);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return StringHelper.getFormattedString(super.getItemStackDisplayName(stack), TextFormatting.AQUA);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (!KeyboardHelper.isHoldingSneakKey()) {
            String sneakString = StringHelper.getFormattedString(TextFormatting.ITALIC, KeyboardHelper.getSneakKeyName(), TextFormatting.GRAY);
            tooltip.add(TextFormatting.GRAY + I18n.format("deepmoblearning.general.more_info", sneakString) + TextFormatting.RESET);
        } else {
            AttunementData attunementData = TrialKeyHelper.getAttunement(stack).orElse(null);
            if (attunementData == null) {
                tooltip.add(StringHelper.getFormattedString(I18n.format("deepmoblearning.trial_key.tooltip.not_attuned"), TextFormatting.GRAY));
                tooltip.add(StringHelper.getFormattedString(I18n.format("deepmoblearning.trial_key.tooltip.available_attunements"), TextFormatting.AQUA));

                ImmutableList<String> availableTrials = MetadataManager.getAvailableTrials();
                for (String trial : availableTrials)
                    tooltip.add(StringHelper.getFormattedString("  - " + trial, TextFormatting.WHITE));
            } else {
                String mobName = StringHelper.getFormattedString(attunementData.getMobDisplayName(), TextFormatting.GRAY);
                tooltip.add(I18n.format("deepmoblearning.trial_key.tooltip.attunement", mobName));

                String tierName = attunementData.getTierDisplayNameFormatted();
                tooltip.add(I18n.format("deepmoblearning.trial_key.tooltip.tier",tierName));

                ImmutableList<TrialAffix> affixes = TrialKeyHelper.getAffixes(stack, BlockPos.ORIGIN, worldIn);
                if (affixes.isEmpty()) {
                    tooltip.add(I18n.format("deepmoblearning.trial_key.tooltip.affixes_faulty"));
                } else {
                    StringBuilder affixList = new StringBuilder();
                    for (int i = 0; i < affixes.size(); i++) {
                        affixList.append(affixes.get(i).getAffixName()).append(i == affixes.size() - 1 ? " " : ", ");
                    }
                    tooltip.add(I18n.format("deepmoblearning.trial_key.tooltip.affixes", affixList.toString()));
                    tooltip.add(I18n.format("deepmoblearning.trial_key.tooltip.affix_info"));
                }
            }
        }
    }
}
