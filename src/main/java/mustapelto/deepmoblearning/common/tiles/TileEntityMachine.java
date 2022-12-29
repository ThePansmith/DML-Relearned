package mustapelto.deepmoblearning.common.tiles;

import io.netty.buffer.ByteBuf;
import mustapelto.deepmoblearning.common.energy.DMLEnergyStorage;
import mustapelto.deepmoblearning.common.network.DMLPacketHandler;
import mustapelto.deepmoblearning.common.network.MessageCraftingState;
import mustapelto.deepmoblearning.common.util.NBTHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;

import javax.annotation.Nullable;

public abstract class TileEntityMachine extends TileEntityTickable {
    // Energy
    protected final DMLEnergyStorage energyStorage;

    // Redstone
    protected boolean redstonePowered;
    protected int redstoneLevel;
    protected RedstoneMode redstoneMode = RedstoneMode.ALWAYS_ON;

    // Crafting
    private CraftingState craftingState = CraftingState.IDLE;
    protected boolean crafting = false;
    protected int craftingProgress = 0;

    public TileEntityMachine(int energyCapacity, int energyMaxReceive) {
        energyStorage = new DMLEnergyStorage(energyCapacity, energyMaxReceive) {
            @Override
            protected void onEnergyChanged() {
                markDirty();
            }
        };
    }

    //
    // ITickable
    //

    @Override
    public void update() {
        super.update();

        if (this.getWorld().isRemote) {
            return;
        }

        if (!crafting && canStartCrafting()) {
            startCrafting();
        }

        if (crafting && canContinueCrafting()) {
            energyStorage.voidEnergy(getCraftingEnergyCost());
            advanceCraftingProgress();
        }

        CraftingState newCraftingState = updateCraftingState();
        if (craftingState != newCraftingState) {
            craftingState = newCraftingState;
            DMLPacketHandler.sendToClient(new MessageCraftingState(this), world, pos);
            markDirty();
        }
    }

    //
    // Crafting
    //

    protected void startCrafting() {
        crafting = true;
        markDirty();
    }

    /**
     * @return true if machine is redstone-activated and has enough energy
     */
    protected boolean canStartCrafting() {
        return canContinueCrafting();
    }

    /**
     * @return true if machine is redstone-activated and has enough energy
     */
    protected boolean canContinueCrafting() {
        return isRedstoneActive() && hasEnergyForCrafting();
    }

    private void advanceCraftingProgress() {
        craftingProgress++;
        if (craftingProgress >= getCraftingDuration())
            finishCrafting();

        markDirty();
        // not sending an update packet here
        // as the client doesn't have to know exact crafting progress
        // unless GUI is open (which is handled in update method)
    }

    protected abstract int getCraftingDuration();

    protected abstract void finishCrafting();

    protected void resetCrafting() {
        crafting = false;
        craftingProgress = 0;
        markDirty();
    }

    public float getRelativeCraftingProgress() {
        return (float)craftingProgress / getCraftingDuration();
    }

    public boolean isCrafting() {
        return crafting;
    }

    //
    // Energy
    //

    public boolean hasEnergyForCrafting() {
        return energyStorage.getEnergyStored() >= getCraftingEnergyCost();
    }

    public abstract int getCraftingEnergyCost();

    public int getEnergy() {
        return energyStorage.getEnergyStored();
    }

    public int getMaxEnergy() {
        return energyStorage.getMaxEnergyStored();
    }

    protected abstract CraftingState updateCraftingState();

    public CraftingState getCraftingState() {
        return craftingState;
    }

    public void setCraftingState(CraftingState newState) {
        craftingState = newState;
        sendBlockUpdate();
    }

    //
    // Redstone Control
    //

    public RedstoneMode getRedstoneMode() {
        return redstoneMode;
    }

    public void setRedstoneMode(RedstoneMode mode) {
        redstoneMode = mode;
        sendUpdatePacketToClient();
    }

    public boolean isRedstoneActive() {
        return RedstoneMode.isActive(redstoneMode, redstonePowered);
    }

    //
    // Capabilities
    //

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return (capability == CapabilityEnergy.ENERGY) ||
                super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(energyStorage);
        }

        return super.getCapability(capability, facing);
    }

    //
    // Client / Server Sync
    //
    @Override
    public ByteBuf getUpdateData() {
        ByteBuf buf = super.getUpdateData();
        energyStorage.writeToBuffer(buf);

        buf.writeInt(redstoneLevel);
        buf.writeBoolean(redstonePowered);
        buf.writeInt(redstoneMode.getIndex());

        buf.writeBoolean(crafting);
        buf.writeInt(craftingProgress);
        buf.writeInt(craftingState.getIndex());

        return buf;
    }

    @Override
    public void handleUpdateData(ByteBuf buf) {
        energyStorage.readFromBuffer(buf);

        redstoneLevel = buf.readInt();
        redstonePowered = buf.readBoolean();
        redstoneMode = RedstoneMode.byIndex(buf.readInt());

        crafting = buf.readBoolean();
        craftingProgress = buf.readInt();
        craftingState = CraftingState.byIndex(buf.readInt());

        sendBlockUpdate();
    }

    //
    // Block Update
    //

    public void onBlockPlaced() {
        onNeighborChange();
    }

    public void onNeighborChange() {
        boolean oldRedstonePowerState = redstonePowered;
        redstoneLevel = world.getRedstonePowerFromNeighbors(pos);
        redstonePowered = redstoneLevel > 0;

        if (redstonePowered != oldRedstonePowerState) {
            if (!world.isRemote)
                sendUpdatePacketToClient();
            else
                sendBlockUpdate();
        }
    }

    //
    // NBT Write / Read
    //

    private static final String NBT_REDSTONE = "redstone"; // Redstone state subtag
    private static final String NBT_REDSTONE_LEVEL = "level";
    private static final String NBT_REDSTONE_POWERED = "powered";
    private static final String NBT_REDSTONE_MODE = "mode";

    protected static final String NBT_CRAFTING = "crafting"; // Crafting state subtag
    private static final String NBT_IS_CRAFTING = "isCrafting"; // Old system uses same tag, only not nested
    private static final String NBT_CRAFTING_PROGRESS = "progress";

    // Tag names from old mod, used for backwards compatibility
    private static final String NBT_LEGACY_CRAFTING_PROGRESS_SIM_CHAMBER = "simulationProgress";
    private static final String NBT_LEGACY_CRAFTING_PROGRESS_LOOT_FAB = "crafingProgress"; // SIC!

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        energyStorage.writeToNBT(compound);

        NBTTagCompound redstoneTag = new NBTTagCompound();
        redstoneTag.setInteger(NBT_REDSTONE_LEVEL, redstoneLevel);
        redstoneTag.setBoolean(NBT_REDSTONE_POWERED, redstonePowered);
        redstoneTag.setInteger(NBT_REDSTONE_MODE, redstoneMode.getIndex());
        compound.setTag(NBT_REDSTONE, redstoneTag);

        NBTTagCompound craftingTag = new NBTTagCompound();
        craftingTag.setBoolean(NBT_IS_CRAFTING, crafting);
        craftingTag.setInteger(NBT_CRAFTING_PROGRESS, craftingProgress);
        compound.setTag(NBT_CRAFTING, craftingTag);

        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        energyStorage.readFromNBT(compound);

        NBTTagCompound redstoneTag = compound.getCompoundTag(NBT_REDSTONE);
        redstoneLevel = NBTHelper.getInteger(redstoneTag, NBT_REDSTONE_LEVEL, 0);
        redstonePowered = NBTHelper.getBoolean(redstoneTag, NBT_REDSTONE_POWERED, false);
        redstoneMode = RedstoneMode.byIndex(NBTHelper.getInteger(redstoneTag, NBT_REDSTONE_MODE, 0));

        if (isLegacyNBT(compound)) {
            // Original DML tag -> use old tag system without nesting and with machine-specific progress tag
            crafting = NBTHelper.getBoolean(compound, NBT_IS_CRAFTING, false);
            if (this instanceof TileEntitySimulationChamber)
                craftingProgress = NBTHelper.getInteger(compound, NBT_LEGACY_CRAFTING_PROGRESS_SIM_CHAMBER, 0);
            else if (this instanceof TileEntityLootFabricator)
                craftingProgress = NBTHelper.getInteger(compound, NBT_LEGACY_CRAFTING_PROGRESS_LOOT_FAB, 0);
        } else {
            // DML:Relearned tag -> use new tag system
            NBTTagCompound craftingTag = compound.getCompoundTag(NBT_CRAFTING);
            crafting = NBTHelper.getBoolean(craftingTag, NBT_IS_CRAFTING, false);
            craftingProgress = NBTHelper.getInteger(craftingTag, NBT_CRAFTING_PROGRESS, 0);
        }
    }

    private static boolean isLegacyNBT(NBTTagCompound nbt) {
        return nbt.hasKey(NBT_IS_CRAFTING) ||
                nbt.hasKey(NBT_LEGACY_CRAFTING_PROGRESS_LOOT_FAB) ||
                nbt.hasKey(NBT_LEGACY_CRAFTING_PROGRESS_SIM_CHAMBER);
    }
}
