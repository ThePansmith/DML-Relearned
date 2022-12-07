package mustapelto.deepmoblearning.common.events;

import com.google.common.collect.ImmutableList;
import mustapelto.deepmoblearning.DMLConstants;
import mustapelto.deepmoblearning.common.capability.CapabilityPlayerTrialProvider;
import mustapelto.deepmoblearning.common.capability.ICapabilityPlayerTrial;
import mustapelto.deepmoblearning.common.items.ItemDeepLearner;
import mustapelto.deepmoblearning.common.items.ItemGlitchArmor;
import mustapelto.deepmoblearning.common.items.ItemGlitchSword;
import mustapelto.deepmoblearning.common.metadata.MetadataDataModel;
import mustapelto.deepmoblearning.common.tiles.TileEntityTrialKeystone;
import mustapelto.deepmoblearning.common.util.AffixHelper;
import mustapelto.deepmoblearning.common.util.DataModelHelper;
import mustapelto.deepmoblearning.common.util.ItemStackHelper;
import mustapelto.deepmoblearning.common.util.TrialKeyHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.UUID;

@SuppressWarnings("ConstantConditions")
@EventBusSubscriber
public class EntityDeathEventHandler {

    private static final Integer entityUUIDBlacklistCap = 1000;
    private static final NonNullList<UUID> killedEntityUUIDBlacklist = NonNullList.create();

    @SubscribeEvent
    public static void dropEvent(LivingDropsEvent event) {
        EntityLivingBase e = event.getEntityLiving();
        NBTTagCompound data = e.getEntityData();
        World world = e.getEntityWorld();

        // Cancel the event if the mob was spawned by the trial
        if (data.hasKey(DMLConstants.Trials.TRIAL_KEYSTONE_POS)) {
            event.setCanceled(true);
        } else if (data.hasKey(DMLConstants.Trials.TRIAL_AFFIX_CONNECTION)) {
            String affixKey = data.getString(DMLConstants.Trials.TRIAL_AFFIX_CONNECTION);

            // Drop the held block from the loot hoarders
            if (affixKey.equals(DMLConstants.Trials.Affix.LOOT_HOARDERS)) {
                event.getDrops().clear();
                event.getDrops().add(new EntityItem(world, e.posX, e.posY, e.posZ, e.getHeldItemMainhand()));
            }
        }
    }

    @SubscribeEvent
    public static void entityDeath(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            handlePlayerDeath((EntityPlayer) event.getEntityLiving());
        } else {
            handleMobDeath(event.getSource().getTrueSource(), event.getEntityLiving());
        }
    }

    private static void handlePlayerDeath(EntityPlayer player) {
        ICapabilityPlayerTrial cap = player.getCapability(CapabilityPlayerTrialProvider.PLAYER_TRIAL_CAP, null);

        BlockPos tilePos = BlockPos.fromLong(cap.getTilePos());
        TileEntity tile = player.getEntityWorld().getTileEntity(tilePos);

        if (tile instanceof TileEntityTrialKeystone) {
            TileEntityTrialKeystone keystone = (TileEntityTrialKeystone) tile;
            if (keystone.isTrialActive()) {
                keystone.onPlayerDied((EntityPlayerMP) player);
            }
        }
    }

    private static void handleMobDeath(Entity source, EntityLivingBase target) {

        // If blacklist is at cap -> clear list
        if (killedEntityUUIDBlacklist.size() >= entityUUIDBlacklistCap) {
            cullEntityUUIDBlacklist();
        }

        if (isEntityUUIDBlacklisted(target)) {
            return;
        }

        if (target.getEntityData().hasKey(DMLConstants.Trials.TRIAL_KEYSTONE_POS)) {
            handleTrialMobDeath(target);
        }

        if (source instanceof EntityPlayer) {
            handlePlayerKill((EntityPlayerMP) source, target);
        }

        killedEntityUUIDBlacklist.add(target.getUniqueID());
    }

    private static void handleTrialMobDeath(EntityLivingBase target) {
        long pos = target.getEntityData().getLong(DMLConstants.Trials.TRIAL_KEYSTONE_POS);
        BlockPos tilePos = BlockPos.fromLong(pos);
        TileEntity tile = target.getEntityWorld().getTileEntity(tilePos);

        if (tile instanceof TileEntityTrialKeystone) {
            TileEntityTrialKeystone keystone = (TileEntityTrialKeystone) tile;
            if (keystone.isTrialActive()) {
                keystone.onMobDied();
            }
        }
    }

    private static void handlePlayerKill(EntityPlayerMP player, EntityLivingBase target) {
        NonNullList<ItemStack> inventory = NonNullList.create();
        inventory.addAll(player.inventory.mainInventory);
        inventory.addAll(player.inventory.offHandInventory);

        // Find deep learners and trial keys from inventory
        ImmutableList<ItemStack> deepLearners = inventory.stream()
                .filter(ItemStackHelper::isDeepLearner)
                .collect(ImmutableList.toImmutableList());
        ImmutableList<ItemStack> trialKeys = inventory.stream()
                .filter(key -> ItemStackHelper.isTrialKey(key) && !TrialKeyHelper.isAttuned(key))
                .collect(ImmutableList.toImmutableList());

        ImmutableList<ItemStack> updatedModels = updateModels(deepLearners, player, target);

        if (updatedModels.isEmpty()) {
            return; // No models found -> nothing more to do
        }

        ItemStack highestTierModel = DataModelHelper.getHighestTierDataModelFromList(updatedModels);

        // Chance to drop pristine matter from the model that gained data
        if (ItemGlitchArmor.isSetEquipped(player)) {
            ICapabilityPlayerTrial cap = player.getCapability(CapabilityPlayerTrialProvider.PLAYER_TRIAL_CAP, null);
            if (!cap.isTrialActive()) {
                ItemGlitchArmor.dropPristineMatter(target.world, target.getPosition(), highestTierModel);
            }
        }

        if (ItemStackHelper.isGlitchSword(player.getHeldItemMainhand())) {
            ICapabilityPlayerTrial cap = player.getCapability(CapabilityPlayerTrialProvider.PLAYER_TRIAL_CAP, null);
            if (!cap.isTrialActive()) {
                ItemStack sword = player.getHeldItemMainhand();
                if (ItemGlitchSword.canIncreaseDamage(sword)) {
                    ItemGlitchSword.increaseDamage(sword, player);
                }
            }
        }

        // Attune the trial key if possible
        trialKeys.forEach(key -> attuneTrialKey(key, highestTierModel, player));
    }

    //
    // Helper Functions
    //

    /** Update all Data Models of the appropriate type
     *
     * @param deepLearners List of Deep Learners in player's inventory
     * @param player Player who made the kill
     * @param target Entity that was killed
     * @return List of updated Data Models
     */
    private static ImmutableList<ItemStack> updateModels(ImmutableList<ItemStack> deepLearners, EntityPlayerMP player, EntityLivingBase target) {
        ImmutableList.Builder<ItemStack> updatedModelsBuilder = ImmutableList.builder();

        deepLearners.forEach(deepLearner -> {
           NonNullList<ItemStack> containedItems = ItemDeepLearner.getContainedItems(deepLearner);

           containedItems.forEach(stack ->
                   DataModelHelper.getDataModelMetadata(stack)
                           .ifPresent(metadata -> {
                               if (metadata.isAssociatedMob(target)) {
                                   DataModelHelper.addKill(stack, player);
                                   updatedModelsBuilder.add(stack);
                               }
                           })
           );

            ItemDeepLearner.setContainedItems(deepLearner, containedItems);
        });

        return updatedModelsBuilder.build();
    }

    // UUID Blacklist Functions

    private static void cullEntityUUIDBlacklist() {
        UUID lastUUID = killedEntityUUIDBlacklist.get(killedEntityUUIDBlacklist.size() - 1);
        killedEntityUUIDBlacklist.clear();
        killedEntityUUIDBlacklist.add(lastUUID);
    }

    private static boolean isEntityUUIDBlacklisted(EntityLivingBase entityLiving) {
        return killedEntityUUIDBlacklist.stream()
                .anyMatch(uuid -> uuid.compareTo(entityLiving.getUniqueID()) == 0);
    }

    private static void attuneTrialKey(ItemStack trialKey, ItemStack dataModel, EntityPlayerMP player) {
        // Don't have to test for isTrialKey - isAttuned takes care of that
        if (TrialKeyHelper.isAttuned(trialKey) || dataModel.isEmpty() || !ItemStackHelper.isDataModel(dataModel)) {
            return;
        }

        DataModelHelper.getDataModelMetadata(dataModel).ifPresent(metadata -> {
            MetadataDataModel.TrialData trialData = metadata.getTrialData();
            if (trialData.hasEntity()) {
                TrialKeyHelper.attune(trialKey, dataModel, player);
                addAffixes(trialKey, dataModel);
            } else {
                player.sendMessage(new TextComponentTranslation("deepmoblearning.trial_key.cannot_attune_message", trialKey.getDisplayName(), metadata.getDisplayName()));
            }
        });
    }

    private static void addAffixes(ItemStack trialKey, ItemStack dataModel) {
        int numberOfAffixes = TrialKeyHelper.getNumberOfAffixes(trialKey);
        ImmutableList.Builder<String> affixes = new ImmutableList.Builder<>();

        for (int i = 0; i < numberOfAffixes; i++) {
            affixes.add(AffixHelper.getRandomAffixKey(affixes.build()));
        }

        TrialKeyHelper.setAffixList(trialKey, affixes.build());
    }
}
