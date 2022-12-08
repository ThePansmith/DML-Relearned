package mustapelto.deepmoblearning.client.gui;

import mustapelto.deepmoblearning.common.tiles.TileEntityTickable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public abstract class GuiContainerTickable extends GuiContainerBase {

    protected final TileEntityTickable tileEntity;

    //
    // INIT
    //

    public GuiContainerTickable(TileEntityTickable tileEntity,
                                EntityPlayer player,
                                World world,
                                int width,
                                int height) {
        super(player, world, tileEntity.getContainer(player.inventory), width, height);
        this.tileEntity = tileEntity;
    }

    @Override
    public void initGui() {
        super.initGui();
        tileEntity.setGuiOpen(true);
    }

    @Override
    public void onGuiClosed() {
        tileEntity.setGuiOpen(false);
        super.onGuiClosed();
    }
}
