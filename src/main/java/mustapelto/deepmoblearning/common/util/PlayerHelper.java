package mustapelto.deepmoblearning.common.util;

import com.google.common.collect.ImmutableList;
import mustapelto.deepmoblearning.common.capability.CapabilityPlayerTrial;
import mustapelto.deepmoblearning.common.capability.CapabilityPlayerTrialProvider;
import mustapelto.deepmoblearning.common.network.DMLPacketHandler;
import mustapelto.deepmoblearning.common.network.MessageTrialOverlay;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Set;

/**
 * Player-related helper methods
 */
public class PlayerHelper {
    /**
     * Get all living players in target area (square).
     * @param world World to look in
     * @param center Center coordinates of target area
     * @param radius "Radius" (i.e. half side length; blocks) of target area
     * @param height Height (blocks) of target area
     * @param offsetY Y offset from center where height starts
     * @return List of all living players in target area
     */
    public static List<EntityPlayerMP> getLivingPlayersInArea(World world, BlockPos center, int radius, int height, int offsetY) {
        return world.getEntitiesWithinAABB(
                EntityPlayerMP.class,
                new AxisAlignedBB(
                        center.getX() - radius, center.getY() + offsetY, center.getZ() - radius,
                        center.getX() + radius, center.getY() + offsetY + height, center.getZ() + radius
                ),
                p -> !p.isDead
        );
    }

    /**
     * Get Deep Learner ItemStack held by player. Prioritizes main hand.
     * @param player Player to check
     * @return Deep Learner ItemStack
     */
    public static ItemStack getHeldDeepLearner(EntityPlayer player) {
        ItemStack mainHandStack = player.getHeldItemMainhand();
        ItemStack offHandStack = player.getHeldItemOffhand();

        if (ItemStackHelper.isDeepLearner(mainHandStack))
            return mainHandStack;
        else if (ItemStackHelper.isDeepLearner(offHandStack))
            return offHandStack;

        return ItemStack.EMPTY;
    }

    public static void sendMessageToOverlay(EntityPlayerMP player, String type) {
        DMLPacketHandler.sendToClientPlayer(new MessageTrialOverlay(type), player);
    }

    public static void updateTrialCapability(EntityPlayerMP player, int waveMobTotal, int currentWave, int mobsDefeated, int lastWave, BlockPos pos, boolean active) {
        CapabilityPlayerTrial cap = (CapabilityPlayerTrial) player.getCapability(CapabilityPlayerTrialProvider.PLAYER_TRIAL_CAP, null);
        cap.setWaveMobTotal(waveMobTotal);
        cap.setCurrentWave(currentWave);
        cap.setDefeated(mobsDefeated);
        cap.setLastWave(lastWave);
        cap.setTilePos(pos.toLong());
        cap.setIsActive(active);
        cap.sync(player);
    }

    public static void updateTrialCapability(Set<EntityPlayerMP> players, int waveMobTotal, int currentWave, int mobsDefeated, int lastWave, BlockPos pos, boolean active) {
        players.forEach(p -> updateTrialCapability(p, waveMobTotal, currentWave, mobsDefeated, lastWave, pos, active));
    }

    public static void resetTrialCapability(EntityPlayerMP player) {
        updateTrialCapability(player, 0, 0, 0, 0, BlockPos.ORIGIN, false);
    }
}
