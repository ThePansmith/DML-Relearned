package mustapelto.deepmoblearning.common.tiles;

import mustapelto.deepmoblearning.client.gui.GuiContainerBase;
import mustapelto.deepmoblearning.common.inventory.ContainerTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TileEntityContainer extends TileEntityBase {

    protected static final String NBT_INVENTORY = "inventory"; // Inventory contents subtag (only used by subclasses)

    //
    // Inventory
    //

    public abstract ContainerTileEntity getContainer(InventoryPlayer inventoryPlayer);

    @SideOnly(Side.CLIENT)
    public abstract GuiContainerBase getGui(EntityPlayer player, World world);
}
