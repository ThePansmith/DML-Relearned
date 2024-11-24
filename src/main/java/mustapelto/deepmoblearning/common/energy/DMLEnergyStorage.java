package mustapelto.deepmoblearning.common.energy;

import io.netty.buffer.ByteBuf;
import mustapelto.deepmoblearning.common.util.MathHelper;
import mustapelto.deepmoblearning.common.util.NBTHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.energy.EnergyStorage;

public class DMLEnergyStorage extends EnergyStorage {
    public DMLEnergyStorage(int capacity, int maxReceive) {
        super(capacity, maxReceive, 0);
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!simulate) {
            onEnergyChanged();
        }
        return super.receiveEnergy(maxReceive, simulate);
    }

    public void voidEnergy(int energy) {
        this.energy -= Math.min(this.energy, energy);
        onEnergyChanged();
    }

    public void writeToNBT(NBTTagCompound compound) {
        compound.setInteger("energy", energy);
    }

    public void readFromNBT(NBTTagCompound compound) {
        setEnergy(NBTHelper.getInteger(compound, "energy", 0));
    }

    public void writeToBuffer(ByteBuf buf) {
        buf.writeInt(energy);
    }

    public void readFromBuffer(ByteBuf buf) {
        setEnergy(buf.readInt());
    }

    private void setEnergy(int energy) {
        this.energy = MathHelper.clamp(energy, 0, getMaxEnergyStored());
        onEnergyChanged();
    }

    protected void onEnergyChanged() {}
}
