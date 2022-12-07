package mustapelto.deepmoblearning.common;

import mustapelto.deepmoblearning.DMLConstants;
import mustapelto.deepmoblearning.client.gui.GuiDeepLearner;
import mustapelto.deepmoblearning.client.gui.GuiLootFabricator;
import mustapelto.deepmoblearning.client.gui.GuiSimulationChamber;
import mustapelto.deepmoblearning.client.gui.GuiTrialKeystone;
import mustapelto.deepmoblearning.common.inventory.ContainerDeepLearner;
import mustapelto.deepmoblearning.common.inventory.ContainerLootFabricator;
import mustapelto.deepmoblearning.common.inventory.ContainerSimulationChamber;
import mustapelto.deepmoblearning.common.inventory.ContainerTrialKeystone;
import mustapelto.deepmoblearning.common.tiles.TileEntityLootFabricator;
import mustapelto.deepmoblearning.common.tiles.TileEntitySimulationChamber;
import mustapelto.deepmoblearning.common.tiles.TileEntityTrialKeystone;
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
                if (tileEntity instanceof TileEntitySimulationChamber)
                    return new ContainerSimulationChamber((TileEntitySimulationChamber) tileEntity, player.inventory);
                else if (tileEntity instanceof TileEntityLootFabricator)
                    return new ContainerLootFabricator((TileEntityLootFabricator) tileEntity, player.inventory);
                else if (tileEntity instanceof TileEntityTrialKeystone)
                    return new ContainerTrialKeystone((TileEntityTrialKeystone) tileEntity, player.inventory);
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
                if (tileEntity instanceof TileEntitySimulationChamber)
                    return new GuiSimulationChamber((TileEntitySimulationChamber) tileEntity, player, world);
                else if (tileEntity instanceof TileEntityLootFabricator)
                    return new GuiLootFabricator((TileEntityLootFabricator) tileEntity, player, world);
                else if (tileEntity instanceof TileEntityTrialKeystone)
                    return new GuiTrialKeystone((TileEntityTrialKeystone) tileEntity, player, world);
            default:
                return null;
        }
    }
}
