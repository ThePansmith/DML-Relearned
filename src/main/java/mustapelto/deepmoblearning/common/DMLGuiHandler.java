package mustapelto.deepmoblearning.common;

import mustapelto.deepmoblearning.DMLConstants;
import mustapelto.deepmoblearning.client.gui.GuiDeepLearner;
import mustapelto.deepmoblearning.common.inventory.ContainerDeepLearner;
import mustapelto.deepmoblearning.common.tiles.TileEntityContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

public class DMLGuiHandler implements IGuiHandler {
    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        switch (ID) {
            case DMLConstants.Gui.IDs.DEEP_LEARNER:
                return new ContainerDeepLearner(player);
            case DMLConstants.Gui.IDs.TILE_ENTITY:
                TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
                if (tileEntity instanceof TileEntityContainer)
                    return ((TileEntityContainer) tileEntity).getContainer(player.inventory);
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        switch (ID) {
            case DMLConstants.Gui.IDs.DEEP_LEARNER:
                return new GuiDeepLearner(player, world);
            case DMLConstants.Gui.IDs.TILE_ENTITY:
                TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
                if (tileEntity instanceof TileEntityContainer)
                    return ((TileEntityContainer) tileEntity).getGui(player, world);
            default:
                return null;
        }
    }
}
