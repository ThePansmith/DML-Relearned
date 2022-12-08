package mustapelto.deepmoblearning.common.tiles;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ITickable;

public abstract class TileEntityTickable extends TileEntityContainer implements ITickable {

    // Ticking
    private long timer;

    // UI
    private boolean guiOpen;

    public TileEntityTickable() {
        super();
    }

    //
    // ITickable
    //

    @Override
    public void update() {
        timer++;
        if (world.isRemote) {
            if (guiOpen) {
                requestUpdatePacketFromServer();
            }
        }
    }

    public long getTimer() {
        return timer;
    }

    //
    // GUI
    //

    public void setGuiOpen(boolean open) {
        this.guiOpen = open;
    }

    protected void sendBlockUpdate() {
        IBlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state, 3);
    }
}
