package mustapelto.deepmoblearning.common.tiles;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import mustapelto.deepmoblearning.common.network.DMLPacketHandler;
import mustapelto.deepmoblearning.common.network.MessageRequestUpdateTileEntity;
import mustapelto.deepmoblearning.common.network.MessageUpdateTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public abstract class TileEntityBase extends TileEntity {
    @Override
    public void onLoad() {
        // Update client TileEntity on chunk load to properly set CraftingState
        if (world.isRemote)
            requestUpdatePacketFromServer();
    }

    protected void requestUpdatePacketFromServer() {
        DMLPacketHandler.sendToServer(new MessageRequestUpdateTileEntity(this));
    }

    protected void sendUpdatePacketToClient() {
        DMLPacketHandler.sendToClient(new MessageUpdateTileEntity(this), world, pos);
    }

    public ByteBuf getUpdateData() {
        ByteBuf buf = Unpooled.buffer();

        buf.writeLong(pos.toLong());

        return buf;
    }

    public void handleUpdateData(ByteBuf buf) {}

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return (oldState.getBlock() != newState.getBlock());
    }

    //
    // Chunk / Block Load
    //

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 1, writeToNBT(new NBTTagCompound()));
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        if (pkt.getPos() != pos)
            return;

        readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void markDirty() {
        if (hasWorld() && pos != null) {
            getWorld().markChunkDirty(pos, this);
        }
    }
}
