package mustapelto.deepmoblearning.common.tiles;

import io.netty.buffer.ByteBuf;
import mustapelto.deepmoblearning.DMLConstants;
import mustapelto.deepmoblearning.DMLRelearned;
import mustapelto.deepmoblearning.client.gui.GuiContainerBase;
import mustapelto.deepmoblearning.client.gui.GuiLootFabricator;
import mustapelto.deepmoblearning.common.DMLConfig;
import mustapelto.deepmoblearning.common.inventory.*;
import mustapelto.deepmoblearning.common.metadata.MetadataDataModel;
import mustapelto.deepmoblearning.common.network.DMLPacketHandler;
import mustapelto.deepmoblearning.common.network.MessageLootFabOutputItem;
import mustapelto.deepmoblearning.common.util.ItemStackHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import javax.annotation.Nullable;

public class TileEntityLootFabricator extends TileEntityMachine {
    private final ItemHandlerPristineMatter inputPristineMatter = new ItemHandlerPristineMatter() {
        @Override
        protected void onMetadataChanged() {
            if (this.pristineMatterMetadata != null && !isValidOutputItem())
                outputItem = ItemStack.EMPTY; // Don't reset output item if stack empties or refills with same item

            resetCrafting();
        }
    };
    private final ItemHandlerInputWrapper pristineMatterWrapper = new ItemHandlerInputWrapper(inputPristineMatter);
    private final ItemHandlerOutput output = new ItemHandlerOutput(16);

    private ItemStack outputItem = ItemStack.EMPTY;

    public TileEntityLootFabricator() {
        super(getEnergyCapacity(), getEnergyPerTick());
    }

    private static int getEnergyCapacity() {
        int energyCost = DMLConfig.MACHINE_SETTINGS.LOOT_FABRICATOR_RF_COST;
        long energyCapacity = 1_000_000L * (energyCost / 100); // 1mil * the hundreds place of the RF/t
        return (int) Math.min(energyCapacity, Integer.MAX_VALUE);
    }

    private static int getEnergyPerTick() {
        int energyCost = DMLConfig.MACHINE_SETTINGS.LOOT_FABRICATOR_RF_COST;
        long energyPerTick = 100L * energyCost; // 100x the RF/t
        return (int) Math.min(energyPerTick, Integer.MAX_VALUE);
    }

    //
    // CRAFTING
    //


    @Override
    public void update() {
        if (!inputPristineMatter.getStackInSlot(0).isEmpty() && !isValidOutputItem()) {
            outputItem = ItemStack.EMPTY;
            resetCrafting();
        }

        super.update();
    }

    @Override
    public boolean canStartCrafting() {
        return super.canStartCrafting() && hasPristineMatter() && hasRoomForOutput() && isValidOutputItem();
    }

    @Override
    protected void finishCrafting() {
        // Loot Fabricator consumes Pristine Matter only when crafting is finished
        // This allows to change output when crafting has already started
        // without losing Pristine Matter
        // (same behavior as vanilla furnace)

        resetCrafting();

        if (outputItem.isEmpty()) {
            // Crafting without selected output. Shouldn't happen i.e. something went wrong.
            DMLRelearned.logger.warn("Loot Fabricator at {} crafted without selected output!", pos.toString());
            return;
        }

        if (!isValidOutputItem()) {
            // Crafting with invalid output item selected. Shouldn't happen i.e. something went wrong.
            DMLRelearned.logger.warn("Loot Fabricator at {} crafted with invalid output selection!", pos.toString());
            outputItem = ItemStack.EMPTY;
            return;
        }

        output.addItemToAvailableSlots(outputItem.copy());
        inputPristineMatter.voidItem();
    }

    @Override
    protected int getCraftingDuration() {
        return DMLConfig.MACHINE_SETTINGS.LOOT_FABRICATOR_PROCESSING_TIME;
    }

    @Override
    public int getCraftingEnergyCost() {
        return DMLConfig.MACHINE_SETTINGS.LOOT_FABRICATOR_RF_COST;
    }

    @Override
    protected CraftingState updateCraftingState() {
        if (!crafting && !hasPristineMatter())
            return CraftingState.IDLE;
        else if (!canContinueCrafting() || (!this.isCrafting() && !canStartCrafting()))
            return CraftingState.ERROR;

        return CraftingState.RUNNING;
    }

    private boolean isValidOutputItem() {
        MetadataDataModel pristineMatterMetadata = getPristineMatterMetadata();
        return !outputItem.isEmpty() && pristineMatterMetadata != null && pristineMatterMetadata.hasLootItem(outputItem);
    }

    @Nullable
    public MetadataDataModel getPristineMatterMetadata() {
        return inputPristineMatter.getPristineMatterMetadata();
    }

    public ItemStack getOutputItem() {
        return outputItem;
    }

    public void setOutputItem(ItemStack outputItem) {
        this.outputItem = outputItem;

        if (!isValidOutputItem())
            this.outputItem = ItemStack.EMPTY;

        if (world.isRemote)
            DMLPacketHandler.sendToServer(new MessageLootFabOutputItem(this, this.outputItem));
    }

    //
    // INVENTORY
    //

    @Override
    public ContainerTileEntity getContainer(InventoryPlayer inventoryPlayer) {
        return new ContainerLootFabricator(this, inventoryPlayer);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public GuiContainerBase getGui(EntityPlayer player, World world) {
        return new GuiLootFabricator(this, player, world);
    }

    public boolean hasPristineMatter() {
        return ItemStackHelper.isPristineMatter(inputPristineMatter.getStackInSlot(0));
    }

    public boolean hasRoomForOutput() {
        return output.hasRoomForItem(outputItem);
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
                        new CombinedInvWrapper(inputPristineMatter, output)
                );
            } else {
                if (!DMLConfig.MACHINE_SETTINGS.LEGACY_MACHINE_SIDEDNESS) {
                    return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(
                            new CombinedInvWrapper(pristineMatterWrapper, output)
                    );
                } else {
                    if (facing == EnumFacing.UP)
                        return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inputPristineMatter);
                    else
                        return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(output);
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
        ByteBufUtils.writeItemStack(buf, outputItem);
        return buf;
    }

    @Override
    public void handleUpdateData(ByteBuf buf) {
        super.handleUpdateData(buf);
        outputItem = ByteBufUtils.readItemStack(buf);
    }

    //
    // NBT WRITE/READ
    //

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        NBTTagCompound inventory = new NBTTagCompound();
        inventory.setTag(NBT_PRISTINE_INPUT, inputPristineMatter.serializeNBT());
        inventory.setTag(NBT_OUTPUT, output.serializeNBT());
        compound.setTag(NBT_INVENTORY, inventory);

        NBTTagCompound crafting = compound.getCompoundTag(NBT_CRAFTING);
        crafting.setTag(NBT_OUTPUT_ITEM, outputItem.serializeNBT());
        compound.setTag(NBT_CRAFTING, crafting);

        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        NBTTagCompound outputItemNBT;

        if (compound.hasKey(NBT_LEGACY_PRISTINE)) {
            inputPristineMatter.deserializeNBT(compound.getCompoundTag(NBT_LEGACY_PRISTINE));
            output.deserializeNBT(compound.getCompoundTag(NBT_OUTPUT));
            outputItemNBT = compound.getCompoundTag(NBT_LEGACY_OUTPUT_ITEM);
        } else {
            NBTTagCompound inventory = compound.getCompoundTag(NBT_INVENTORY);
            inputPristineMatter.deserializeNBT(inventory.getCompoundTag(NBT_PRISTINE_INPUT));
            output.deserializeNBT(inventory.getCompoundTag(NBT_OUTPUT));

            NBTTagCompound crafting = compound.getCompoundTag(NBT_CRAFTING);
            outputItemNBT = crafting.getCompoundTag(NBT_OUTPUT_ITEM);
        }

        outputItem = new ItemStack(outputItemNBT);
    }

    // NBT Tag Names
    private static final String NBT_PRISTINE_INPUT = "inputPristine";
    private static final String NBT_OUTPUT = "output";
    private static final String NBT_OUTPUT_ITEM = "outputItem";

    private static final String NBT_LEGACY_PRISTINE = "pristine";
    private static final String NBT_LEGACY_OUTPUT_ITEM = "resultingItem";
}
