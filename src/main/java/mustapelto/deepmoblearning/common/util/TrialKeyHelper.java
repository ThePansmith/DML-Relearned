package mustapelto.deepmoblearning.common.util;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;
import mustapelto.deepmoblearning.common.trials.AttunementData;
import mustapelto.deepmoblearning.common.trials.affix.TrialAffix;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.Optional;

public class TrialKeyHelper {

    private static final String NBT_ATTUNEMENT = "attunement";
    private static final String NBT_TIER = "tier";
    private static final String NBT_AFFIXES = "affixes";
    private static final String NBT_LEGACY_MOB_KEY = "mobKey";

    private static final Table<String, Integer, AttunementData> attunementDataCache = HashBasedTable.create();

    public static void attune(ItemStack trialKey, ItemStack dataModel, EntityPlayerMP player) {
        DataModelHelper.getDataModelMetadata(dataModel)
                .ifPresent(metadata -> {
                    NBTHelper.setString(trialKey, NBT_ATTUNEMENT, metadata.getID());
                    NBTHelper.setInteger(trialKey, NBT_TIER, DataModelHelper.getTier(dataModel));

                    player.sendMessage(new TextComponentTranslation("deepmoblearning.trial_key.attunement_message", trialKey.getDisplayName(), metadata.getDisplayName()));
                });
    }

    public static boolean isAttuned(ItemStack trialKey) {
        return getAttunement(trialKey).isPresent();
    }

    public static Optional<AttunementData> getAttunement(ItemStack trialKey) {
        String mob = getAttunementString(trialKey).orElse("");
        int tier = NBTHelper.getInteger(trialKey, NBT_TIER, -1);

        if (!attunementDataCache.contains(mob, tier))
            AttunementData.create(mob, tier)
                    .ifPresent(data -> attunementDataCache.put(mob, tier, data));

        AttunementData result = attunementDataCache.get(mob, tier);
        return (result != null) ? Optional.of(result) : Optional.empty();
    }

    public static ImmutableList<TrialAffix> getAffixes(ItemStack trialKey, BlockPos pos, World world) {
        ImmutableList<String> affixKeys = getAffixList(trialKey);
        return affixKeys.stream()
                .map(key -> (TrialAffix) AffixHelper.createAffix(key, pos, world))
                .collect(ImmutableList.toImmutableList());
    }

    public static ImmutableList<String> getAffixList(ItemStack stack) {
        ImmutableList.Builder<String> builder = new ImmutableList.Builder<>();
        NBTTagList list = NBTHelper.getTagList(stack, NBT_AFFIXES);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            builder.add(tag.getString(i + ""));
        }
        return builder.build();
    }

    public static void setAffixList(ItemStack stack, ImmutableList<String> affixes) {
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < affixes.size(); i++) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString(i + "", affixes.get(i));
            list.appendTag(tag);
        }
        NBTHelper.setTagList(stack, NBT_AFFIXES, list);
    }

    public static int getNumberOfAffixes(ItemStack stack) {
        AttunementData data = getAttunement(stack).orElse(null);
        if (data != null) {
            return data.getAffixCount();
        }
        return 0;
    }

    private static Optional<String> getAttunementString(ItemStack trialKey) {
        if (!ItemStackHelper.isTrialKey(trialKey))
            return Optional.empty();

        if (NBTHelper.hasKey(trialKey, NBT_LEGACY_MOB_KEY))
            convertNBT(trialKey);

        String attunement = NBTHelper.getString(trialKey, NBT_ATTUNEMENT);
        return !attunement.isEmpty() ? Optional.of(attunement) : Optional.empty();
    }

    private static void convertNBT(ItemStack stack) {
        String mobKey = NBTHelper.getString(stack, NBT_LEGACY_MOB_KEY);
        NBTHelper.setString(stack, NBT_ATTUNEMENT, mobKey);
        NBTHelper.removeKey(stack, NBT_LEGACY_MOB_KEY);
    }
}
