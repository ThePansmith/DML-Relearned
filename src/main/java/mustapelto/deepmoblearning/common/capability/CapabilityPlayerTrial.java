package mustapelto.deepmoblearning.common.capability;

import mustapelto.deepmoblearning.common.network.DMLPacketHandler;
import mustapelto.deepmoblearning.common.network.MessageUpdateTrialCapability;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

public class CapabilityPlayerTrial implements ICapabilityPlayerTrial, Capability.IStorage<ICapabilityPlayerTrial> {

    private int currentWave = 0;
    private int lastWave = 0;
    private int mobsDefeated = 0;
    private int waveMobTotal = 0;
    private boolean isActive = false;
    private long tilePos;

    public static void init() {
        // Enable field injection for capabilities
        CapabilityManager.INSTANCE.register(ICapabilityPlayerTrial.class, new CapabilityPlayerTrial(), CapabilityPlayerTrial::new);
    }

    public CapabilityPlayerTrial() {
    }

    public CapabilityPlayerTrial(int currentWave, int lastWave, int mobsDefeated, int waveMobTotal, long pos, boolean isActive) {
        this.currentWave = currentWave;
        this.lastWave = lastWave;
        this.mobsDefeated = mobsDefeated;
        this.waveMobTotal = waveMobTotal;
        this.tilePos = pos;
        this.isActive = isActive;
    }


    @Nullable
    @Override
    public NBTTagCompound writeNBT(Capability<ICapabilityPlayerTrial> capability, ICapabilityPlayerTrial instance, EnumFacing enumFacing) {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("currentWave", instance.getCurrentWave());
        compound.setInteger("lastWave", instance.getLastWave());
        compound.setInteger("mobsDefeated", instance.getDefated());
        compound.setInteger("waveMobTotal", instance.getWaveMobTotal());
        compound.setBoolean("isActive", instance.isTrialActive());
        compound.setLong("tilePos", instance.getTilePos());
        return compound;
    }

    @Override
    public void readNBT(Capability<ICapabilityPlayerTrial> capability, ICapabilityPlayerTrial instance, EnumFacing enumFacing, NBTBase nbt) {
        instance.setCurrentWave(((NBTTagCompound) nbt).getInteger("currentWave"));
        instance.setLastWave(((NBTTagCompound) nbt).getInteger("lastWave"));
        instance.setDefeated(((NBTTagCompound) nbt).getInteger("mobsDefeated"));
        instance.setWaveMobTotal(((NBTTagCompound) nbt).getInteger("waveMobTotal"));
        instance.setIsActive(((NBTTagCompound) nbt).getBoolean("isActive"));
        instance.setTilePos(((NBTTagCompound) nbt).getLong("tilePos"));
    }


    @Override
    @SuppressWarnings("ConstantConditions")
    public void sync(EntityPlayerMP player) {
        DMLPacketHandler.network.sendTo(new MessageUpdateTrialCapability((CapabilityPlayerTrial) player.getCapability(CapabilityPlayerTrialProvider.PLAYER_TRIAL_CAP, null)), player);
    }

    @Override
    public void setCurrentWave(int wave) {
        currentWave = wave;
    }

    @Override
    public int getCurrentWave() {
        return currentWave;
    }

    @Override
    public void setLastWave(int wave) {
        lastWave = wave;
    }

    @Override
    public int getLastWave() {
        return lastWave;
    }

    @Override
    public void setDefeated(int count) {
        mobsDefeated = count;
    }

    @Override
    public int getDefated() {
        return mobsDefeated;
    }

    @Override
    public void setWaveMobTotal(int total) {
        waveMobTotal = total;
    }

    @Override
    public int getWaveMobTotal() {
        return waveMobTotal;
    }

    @Override
    public void setTilePos(long pos) {
        tilePos = pos;
    }

    @Override
    public long getTilePos() {
        return tilePos;
    }

    @Override
    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public boolean isTrialActive() {
        return isActive;
    }
}
