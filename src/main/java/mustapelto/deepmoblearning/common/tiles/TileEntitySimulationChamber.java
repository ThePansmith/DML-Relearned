package mustapelto.deepmoblearning.common.tiles;

import io.netty.buffer.ByteBuf;
import mustapelto.deepmoblearning.DMLConstants;
import mustapelto.deepmoblearning.client.gui.GuiContainerBase;
import mustapelto.deepmoblearning.client.gui.GuiSimulationChamber;
import mustapelto.deepmoblearning.common.DMLConfig;
import mustapelto.deepmoblearning.common.inventory.*;
import mustapelto.deepmoblearning.common.util.DataModelHelper;
import mustapelto.deepmoblearning.common.util.ItemStackHelper;
import mustapelto.deepmoblearning.common.util.NBTHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import javax.annotation.Nullable;
import java.util.concurrent.ThreadLocalRandom;

public class TileEntitySimulationChamber extends TileEntityMachine {
    private final ItemHandlerDataModel inputDataModel = new ItemHandlerDataModel() {
        @Override
        protected void onContentsChanged(int slot) {
            onDataModelChanged();
        }
    };
    private final ItemHandlerInputWrapper dataModelWrapper = new ItemHandlerInputWrapper(inputDataModel);
    private final ItemHandlerPolymerClay inputPolymer = new ItemHandlerPolymerClay();
    private final ItemHandlerInputWrapper polymerWrapper = new ItemHandlerInputWrapper(inputPolymer);
    private final ItemHandlerOutput outputLiving = new ItemHandlerOutput();
    private final ItemHandlerOutput outputPristine = new ItemHandlerOutput();

    private boolean pristineSuccess = false;

    public TileEntitySimulationChamber() {
        super(DMLConstants.SimulationChamber.ENERGY_CAPACITY, DMLConstants.SimulationChamber.ENERGY_IN_MAX);
    }

    //
    // CRAFTING
    //

    @Override
    protected void startCrafting() {
        super.startCrafting();

        // Calculate Pristine Matter success
        int pristineChance = DataModelHelper.getPristineChance(getDataModel());
        int random = ThreadLocalRandom.current().nextInt(100);
        pristineSuccess = (random < pristineChance);

        // Consume Polymer Clay
        inputPolymer.voidItem();
    }

    @Override
    protected boolean canStartCrafting() {
        return super.canStartCrafting() && hasPolymerClay() && canContinueCrafting();
    }

    @Override
    protected boolean canContinueCrafting() {
        return super.canContinueCrafting() && hasDataModel() && canDataModelSimulate() && !isLivingMatterOutputFull() && !isPristineMatterOutputFull();
    }

    @Override
    protected void finishCrafting() {
        ItemStack dataModel = getDataModel();

        DataModelHelper.getDataModelMetadata(dataModel).ifPresent(metadata -> {
            DataModelHelper.addSimulation(dataModel);

            ItemStack oldLivingMatterOutput = outputLiving.getStackInSlot(0);
            ItemStack newLivingMatterOutput = metadata.getLivingMatter(oldLivingMatterOutput.getCount() + 1);
            outputLiving.setStackInSlot(0, newLivingMatterOutput);

            if (pristineSuccess) {
                ItemStack oldPristineMatterOutput = outputPristine.getStackInSlot(0);
                ItemStack newPristineMatterOutput = metadata.getPristineMatter(oldPristineMatterOutput.getCount() + 1);
                outputPristine.setStackInSlot(0, newPristineMatterOutput);
            }
        });

        resetCrafting();
    }

    @Override
    protected void resetCrafting() {
        super.resetCrafting();
        pristineSuccess = false;
    }

    @Override
    protected int getCraftingDuration() {
        return DMLConfig.MACHINE_SETTINGS.SIMULATION_CHAMBER_PROCESSING_TIME;
    }

    public boolean isPristineSuccess() {
        return pristineSuccess;
    }

    @Override
    public int getCraftingEnergyCost() {
        return DataModelHelper.getSimulationEnergy(getDataModel());
    }

    @Override
    protected CraftingState updateCraftingState() {
        if (!hasDataModel())
            return CraftingState.IDLE;
        else if (!canContinueCrafting() || (!this.isCrafting() && !canStartCrafting()))
            return CraftingState.ERROR;

        return CraftingState.RUNNING;
    }

    //
    // INVENTORY
    //


    @Override
    public ContainerTileEntity getContainer(InventoryPlayer inventoryPlayer) {
        return new ContainerSimulationChamber(this, inventoryPlayer);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public GuiContainerBase getGui(EntityPlayer player, World world) {
        return new GuiSimulationChamber(this, player, world);
    }

    /**
     * (Server only) Reset simulation state on data model change.
     */
    private void onDataModelChanged() {
        if (!world.isRemote) {
            resetCrafting();
        }
    }

    public ItemStack getDataModel() {
        return inputDataModel.getStackInSlot(0);
    }

    public boolean hasDataModel() {
        return ItemStackHelper.isDataModel(getDataModel());
    }

    public boolean canDataModelSimulate() {
        return DataModelHelper.canSimulate(getDataModel());
    }

    public ItemStack getPolymerClay() {
        return inputPolymer.getStackInSlot(0);
    }

    public boolean hasPolymerClay() {
        return ItemStackHelper.isPolymerClay(getPolymerClay());
    }

    public boolean isLivingMatterOutputFull() {
        ItemStack livingMatterStack = outputLiving.getStackInSlot(0);

        if (livingMatterStack.isEmpty())
            return false;

        boolean stackIsFull = (livingMatterStack.getCount() >= outputLiving.getSlotLimit(0));
        boolean stackMatchesDataModel = DataModelHelper.getDataModelMatchesLivingMatter(getDataModel(), livingMatterStack);

        return (stackIsFull || !stackMatchesDataModel);
    }

    public boolean isPristineMatterOutputFull() {
        ItemStack pristineMatterStack = outputPristine.getStackInSlot(0);

        if (pristineMatterStack.isEmpty())
            return false;

        boolean stackIsFull = (pristineMatterStack.getCount() >= outputPristine.getSlotLimit(0));
        boolean stackMatchesDataModel = DataModelHelper.getDataModelMatchesPristineMatter(getDataModel(), pristineMatterStack);

        return (stackIsFull || !stackMatchesDataModel);
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
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (facing == null) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(
                        new CombinedInvWrapper(inputDataModel, inputPolymer, outputLiving, outputPristine)
                );
            } else {
                if (!DMLConfig.MACHINE_SETTINGS.LEGACY_MACHINE_SIDEDNESS) {
                    return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(
                            new CombinedInvWrapper(dataModelWrapper, polymerWrapper, outputLiving, outputPristine)
                    );
                } else {
                    if (facing == EnumFacing.UP) {
                        return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(new CombinedInvWrapper(inputDataModel, inputPolymer));
                    } else {
                        return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(new CombinedInvWrapper(outputPristine, outputLiving));
                    }
                }
            }
        }

        return super.getCapability(capability, facing);
    }

    //
    // CLIENT/SERVER SYNC
    //

    @Override
    public ByteBuf getUpdateData() {
        ByteBuf buf = super.getUpdateData();

        buf.writeBoolean(pristineSuccess);

        return buf;
    }

    @Override
    public void handleUpdateData(ByteBuf buf) {
        super.handleUpdateData(buf);

        pristineSuccess = buf.readBoolean();
    }

    //
    // NBT WRITE/READ
    //

    // NBT Tag Names
    private static final String NBT_INPUT_DATA_MODEL = "inputDataModel";
    private static final String NBT_INPUT_POLYMER = "inputPolymer";
    private static final String NBT_OUTPUT_LIVING = "outputLiving";
    private static final String NBT_OUTPUT_PRISTINE = "outputPristine";
    private static final String NBT_PRISTINE_SUCCESS = "pristineSuccess";

    // Tag names from old mod, used for backwards compatibility
    private static final String NBT_LEGACY_INPUT_DATA_MODEL = "dataModel";
    private static final String NBT_LEGACY_INPUT_POLYMER = "polymer";
    private static final String NBT_LEGACY_OUTPUT_LIVING = "lOutput";
    private static final String NBT_LEGACY_OUTPUT_PRISTINE = "pOutput";
    private static final String NBT_LEGACY_PRISTINE_SUCCESS = "craftSuccess";

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        NBTTagCompound inventory = new NBTTagCompound();
        inventory.setTag(NBT_INPUT_DATA_MODEL, inputDataModel.serializeNBT());
        inventory.setTag(NBT_INPUT_POLYMER, inputPolymer.serializeNBT());
        inventory.setTag(NBT_OUTPUT_LIVING, outputLiving.serializeNBT());
        inventory.setTag(NBT_OUTPUT_PRISTINE, outputPristine.serializeNBT());
        compound.setTag(NBT_INVENTORY, inventory);

        compound.getCompoundTag(NBT_CRAFTING).setBoolean(NBT_PRISTINE_SUCCESS, pristineSuccess);

        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        if (isLegacyNBT(compound)) {
            // Original DML tag -> use old (non-nested) tag names
            inputDataModel.deserializeNBT(compound.getCompoundTag(NBT_LEGACY_INPUT_DATA_MODEL));
            inputPolymer.deserializeNBT(compound.getCompoundTag(NBT_LEGACY_INPUT_POLYMER));
            outputLiving.deserializeNBT(compound.getCompoundTag(NBT_LEGACY_OUTPUT_LIVING));
            outputPristine.deserializeNBT(compound.getCompoundTag(NBT_LEGACY_OUTPUT_PRISTINE));

            pristineSuccess = NBTHelper.getBoolean(compound, NBT_LEGACY_PRISTINE_SUCCESS, false);
        } else {
            // DML:Relearned tag -> use new (nested) tag names
            NBTTagCompound inventory = compound.getCompoundTag(NBT_INVENTORY);
            inputDataModel.deserializeNBT(inventory.getCompoundTag(NBT_INPUT_DATA_MODEL));
            inputPolymer.deserializeNBT(inventory.getCompoundTag(NBT_INPUT_POLYMER));
            outputLiving.deserializeNBT(inventory.getCompoundTag(NBT_OUTPUT_LIVING));
            outputPristine.deserializeNBT(inventory.getCompoundTag(NBT_OUTPUT_PRISTINE));

            pristineSuccess = NBTHelper.getBoolean(compound.getCompoundTag(NBT_CRAFTING), NBT_PRISTINE_SUCCESS, false);
        }
    }

    private static boolean isLegacyNBT(NBTTagCompound nbt) {
        return nbt.hasKey(NBT_LEGACY_INPUT_DATA_MODEL) ||
                nbt.hasKey(NBT_LEGACY_INPUT_POLYMER) ||
                nbt.hasKey(NBT_LEGACY_OUTPUT_LIVING) ||
                nbt.hasKey(NBT_LEGACY_OUTPUT_PRISTINE) ||
                nbt.hasKey(NBT_LEGACY_PRISTINE_SUCCESS);
    }
}
