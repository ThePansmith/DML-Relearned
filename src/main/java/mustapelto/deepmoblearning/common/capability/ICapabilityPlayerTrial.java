package mustapelto.deepmoblearning.common.capability;

import net.minecraft.entity.player.EntityPlayerMP;

public interface ICapabilityPlayerTrial {

    void setCurrentWave(int wave);

    int getCurrentWave();

    void setLastWave(int wave);

    int getLastWave();

    void setDefeated(int count);

    int getDefated();

    void setWaveMobTotal(int total);

    int getWaveMobTotal();

    void setTilePos(long pos);

    long getTilePos();

    void setIsActive(boolean b);

    boolean isTrialActive();

    void sync(EntityPlayerMP player);
}
