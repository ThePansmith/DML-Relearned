package mustapelto.deepmoblearning.common.capability;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nullable;

@SuppressWarnings("ConstantConditions")
public class CapabilityPlayerTrialProvider implements ICapabilitySerializable<NBTTagCompound> {

    @CapabilityInject(ICapabilityPlayerTrial.class)
    public static final Capability<ICapabilityPlayerTrial> PLAYER_TRIAL_CAP = null;

    private ICapabilityPlayerTrial instance;

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing enumFacing) {
        return capability == PLAYER_TRIAL_CAP;
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing enumFacing) {
        return capability == PLAYER_TRIAL_CAP ? PLAYER_TRIAL_CAP.cast(getInstance()) : null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return (NBTTagCompound) PLAYER_TRIAL_CAP.getStorage().writeNBT(PLAYER_TRIAL_CAP, getInstance(), null);
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        PLAYER_TRIAL_CAP.getStorage().readNBT(PLAYER_TRIAL_CAP, getInstance(), null, compound);
    }

    private ICapabilityPlayerTrial getInstance() {
        if (instance != null) {
            return instance;
        }
        if (PLAYER_TRIAL_CAP != null) {
            instance = PLAYER_TRIAL_CAP.getDefaultInstance();
        }
        return instance;
    }
}
