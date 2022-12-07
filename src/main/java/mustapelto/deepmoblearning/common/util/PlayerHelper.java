package mustapelto.deepmoblearning.common.util;

import mustapelto.deepmoblearning.common.network.DMLPacketHandler;
import mustapelto.deepmoblearning.common.network.MessageTrialOverlay;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

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
}
