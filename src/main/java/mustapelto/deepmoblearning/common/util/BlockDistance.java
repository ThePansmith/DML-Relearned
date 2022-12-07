package mustapelto.deepmoblearning.common.util;

import net.minecraft.util.math.BlockPos;

public class BlockDistance {

    /* Math: http://www.meracalculator.com/math/distance-between-2-points(3-dim).php */
    public static double getBlockDistance(BlockPos pos, BlockPos pos2) {
        double x = Math.pow((pos.getX() - pos2.getX()), 2);
        double y = Math.pow((pos.getY() - pos2.getY()), 2);
        double z = Math.pow((pos.getZ() - pos2.getZ()), 2);

        return Math.sqrt(x + y + z);
    }
}
