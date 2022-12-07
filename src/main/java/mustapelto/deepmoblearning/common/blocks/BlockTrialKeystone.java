package mustapelto.deepmoblearning.common.blocks;

import mustapelto.deepmoblearning.common.tiles.TileEntityTrialKeystone;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockTrialKeystone extends BlockTileEntity {
    private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0, 0, 0, 1d, 0.5d, 1d);

    public BlockTrialKeystone() {
        super("trial_keystone", Material.ROCK);
        setHardness(4f);
        setResistance(3000f);
        fullBlock = false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOUNDING_BOX;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityTrialKeystone();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityTrialKeystone) {
            TileEntityTrialKeystone keystone = (TileEntityTrialKeystone) tile;
            keystone.stopTrial(true, true);
            // todo drop key?
        }
        super.breakBlock(world, pos, state);
    }
}
