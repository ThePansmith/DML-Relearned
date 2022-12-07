package mustapelto.deepmoblearning.common.trials;

import com.google.common.collect.ImmutableList;
import mustapelto.deepmoblearning.DMLRelearned;
import mustapelto.deepmoblearning.common.metadata.MetadataDataModel;
import mustapelto.deepmoblearning.common.metadata.MetadataDataModelTier;
import mustapelto.deepmoblearning.common.metadata.MetadataManager;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Optional;

public class AttunementData {

    public static final String NBT_MOB_ID = "mobID";
    public static final String NBT_TIER = "tier";

    private final MetadataDataModel mob;
    private final MetadataDataModelTier tier;
    private final MetadataDataModel.TrialData modelTrialData;
    private final MetadataDataModelTier.TierTrialData tierTrialData;
    private final ImmutableList<ItemStack> rewards;

    private AttunementData(String mobID, int tier) {
        MetadataDataModel mob = MetadataManager.getDataModelMetadata(mobID).orElse(null);
        if (mob != null && !mob.getTrialData().hasEntity())
            mob = null; // No available Trial entity -> can't run Trial

        this.mob = mob;
        this.tier = MetadataManager.getDataModelTierData(tier).orElse(null);

        if (!isInvalidData()) {
            modelTrialData = this.mob.getTrialData();
            tierTrialData = this.tier.getTierTrialData();
            rewards = buildRewards();
        } else {
            modelTrialData = null;
            tierTrialData = null;
            rewards = ImmutableList.of();
        }
    }

    public static Optional<AttunementData> create(String mob, int tier) {
        AttunementData data = new AttunementData(mob, tier);

        return data.isInvalidData() ? Optional.empty() : Optional.of(data);
    }

    private boolean isInvalidData() {
        return mob == null || tier == null;
    }

    public String getMobDisplayName() {
        return mob.getDisplayName();
    }

    public String getTierDisplayNameFormatted() {
        return tier.getDisplayNameFormatted();
    }

    public ImmutableList<ItemStack> getRewards() {
        return rewards;
    }

    private ImmutableList<ItemStack> buildRewards() {
        ImmutableList.Builder<ItemStack> builder = ImmutableList.builder();

        int pristineAmount = tierTrialData.getPristine();
        builder.add(mob.getPristineMatter(pristineAmount));

        if (MetadataManager.isMaxDataModelTier(tier.getTier()))
            builder.addAll(modelTrialData.getRewards());

        return builder.build();
    }

    public int getMaxWave() {
        return tierTrialData.getMaxWave();
    }

    public int getAffixCount() {
        return tierTrialData.getAffixes();
    }

    public int getGlitchChance() {
        return tierTrialData.getGlitchChance();
    }

    public double getSpawnDelay() {
        return modelTrialData.getSpawnDelay();
    }

    public Optional<EntityLiving> getRandomEntity(World world) {
        Optional<EntityLiving> result = modelTrialData.getRandomEntity(world);
        if (!result.isPresent())
            DMLRelearned.logger.warn("Could not create Trial entity!");
        return result;
    }

    public int getCurrentWaveMobTotal(int currentWave) {
        return modelTrialData.getMobsPerWave(currentWave);
    }
}
