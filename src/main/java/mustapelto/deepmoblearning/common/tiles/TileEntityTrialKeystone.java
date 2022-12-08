package mustapelto.deepmoblearning.common.tiles;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import mustapelto.deepmoblearning.common.inventory.ContainerTileEntity;
import mustapelto.deepmoblearning.common.inventory.ContainerTrialKeystone;
import mustapelto.deepmoblearning.common.inventory.ItemHandlerTrialKey;
import mustapelto.deepmoblearning.common.network.*;
import mustapelto.deepmoblearning.common.trials.AttunementData;
import mustapelto.deepmoblearning.common.trials.affix.TrialAffix;
import mustapelto.deepmoblearning.common.util.ItemStackHelper;
import mustapelto.deepmoblearning.common.util.TrialKeyHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;

public class TileEntityTrialKeystone extends TileEntityTickable {

    private final ItemHandlerTrialKey trialKeyInventory = new ItemHandlerTrialKey();
    private final TrialHandler trialHandler = new TrialHandler(this);

    @Override
    public void update() {
        super.update();
        if (!world.isRemote) {

            // Progress the trial
            if (trialHandler.isTrialActive()) {
                trialHandler.runTrial();

                // Every 15 seconds send a block update
                // TODO is this actually needed?
                if (getTimer() % 300 == 0) {
                    sendBlockUpdate();
                }
            }

            // Every 5 seconds mark the tile dirty
            // TODO Is this actually needed?
            if (getTimer() % 100 == 0) {
                markDirty();
            }
        }
    }

    //
    // Trial actions
    //

    public void startTrial() {

        // Do not start if its already running
        if (isTrialActive()) return;
        // Do not start if there is no attuned key in inventory
        if (!hasTrialKeyInInventory() || !TrialKeyHelper.isAttuned(getTrialKeyFromInventory())) return;
        // Do not start if the area is not clear
        if (!isTrialAreaClear()) return;

        // Start the trial
        if (world.isRemote) {
            DMLPacketHandler.sendToServer(new MessageTrialStart(this));
        } else if (trialHandler.startTrial(trialKeyInventory.getStackInSlot(0))) {
            trialKeyInventory.setStackInSlot(0, ItemStack.EMPTY);
            sendBlockUpdate();
            markDirty();
        }
    }

    public void stopTrial(boolean abort, boolean sendMessages) {
        trialHandler.stopTrial(abort, sendMessages);
    }

    public void onPlayerDied(EntityPlayerMP player) {
        trialHandler.onPlayerDied(player);
    }

    public void onMobDied() {
        trialHandler.onMobDied();
    }

    //
    // Trial state and conditions
    //

    public boolean isTrialActive() {
        return trialHandler.isTrialActive();
    }

    public boolean isTrialAreaClear() {
        return trialHandler.isTrialAreaClear();
    }

    public ImmutableList<TrialAffix> getAffixes() {
        ImmutableList<TrialAffix> affixes = trialHandler.getAffixes();

        // Try to get the affixes from the inventory trial key if the trial handler cannot give us anything.
        if (affixes == ImmutableList.<TrialAffix>of() && hasTrialKeyInInventory()) {
            affixes = TrialKeyHelper.getAffixes(getTrialKeyFromInventory(), getPos(), getWorld());
        }
        return affixes;
    }

    public int getCurrentWave() {
        return trialHandler.getCurrentWave();
    }

    public int getLastWave() {
        return trialHandler.getLastWave();
    }

    // TODO Remove
    @Nullable
    public AttunementData getTrialData() {
        return TrialKeyHelper.getAttunement(getTrialKeyFromInventory()).orElse(null);
    }

    //
    // Inventory
    //

    @Override
    public ContainerTileEntity getContainer(InventoryPlayer inventoryPlayer) {
        return new ContainerTrialKeystone(this, inventoryPlayer);
    }

    public ItemStack getTrialKeyFromInventory() {
        return trialKeyInventory.getStackInSlot(0);
    }

    public boolean hasTrialKeyInInventory() {
        return ItemStackHelper.isTrialKey(getTrialKeyFromInventory());
    }

    //
    // RENDER
    //

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos(), getPos().add(1, 2, 1));
    }

    //
    // SERVER/CLIENT SYNC
    //

    @Override
    public ByteBuf getUpdateData() {
        ByteBuf buf = super.getUpdateData();
        return trialHandler.getUpdateData(buf);
    }

    @Override
    public void handleUpdateData(ByteBuf buf) {
        super.handleUpdateData(buf);
        trialHandler.handleUpdateData(buf);
    }

    //
    // CAPABILITIES
    //

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) ||
                super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(trialKeyInventory);

        return super.getCapability(capability, facing);
    }

    //
    // NBT WRITE/READ
    //

    // NBT Tag Names
    private static final String NBT_TRIAL_KEY = "trialKey";
    private static final String NBT_TRIAL_STATE = "trialState";

    // Legacy NBT Tag Names
    private static final String NBT_LEGACY_TRIAL_KEY = "inventory";

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        NBTTagCompound inventory = new NBTTagCompound();
        inventory.setTag(NBT_TRIAL_KEY, trialKeyInventory.serializeNBT());
        compound.setTag(NBT_INVENTORY, inventory);

        NBTTagCompound trialState = new NBTTagCompound();
        trialHandler.writeToNBT(trialState);
        compound.setTag(NBT_TRIAL_STATE, trialState);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        if (isLegacyNBT(compound)) {
            // Original DML tag -> read Trial Key from legacy inventory tag and set Trial State to default values
            trialKeyInventory.deserializeNBT(compound.getCompoundTag(NBT_LEGACY_TRIAL_KEY));
            trialHandler.resetTrial();
        } else {
            // DML:Relearned tag -> use new (nested) tag names
            NBTTagCompound inventory = compound.getCompoundTag(NBT_INVENTORY);
            trialKeyInventory.deserializeNBT(inventory.getCompoundTag(NBT_TRIAL_KEY));
            trialHandler.readFromNBT(compound.getCompoundTag(NBT_TRIAL_STATE));
        }
    }

    private static boolean isLegacyNBT(NBTTagCompound nbt) {
        return !nbt.hasKey(NBT_TRIAL_STATE);
    }
}