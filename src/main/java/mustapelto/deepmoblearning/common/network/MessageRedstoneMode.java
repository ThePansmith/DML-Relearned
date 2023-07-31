package mustapelto.deepmoblearning.common.network;

import io.netty.buffer.ByteBuf;
import mustapelto.deepmoblearning.common.tiles.RedstoneMode;
import mustapelto.deepmoblearning.common.tiles.TileEntityMachine;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public class MessageRedstoneMode implements IMessage {
    private BlockPos pos;
    private int dimension;
    private RedstoneMode redstoneMode;

    public MessageRedstoneMode() {}

    public MessageRedstoneMode(BlockPos pos, int dimension, RedstoneMode redstoneMode) {
        this.pos = pos;
        this.dimension = dimension;
        this.redstoneMode = redstoneMode;
    }

    public MessageRedstoneMode(TileEntityMachine target, RedstoneMode redstoneMode) {
        this(target.getPos(), target.getWorld().provider.getDimension(), redstoneMode);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
        buf.writeInt(dimension);
        buf.writeInt(redstoneMode.getIndex());
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = BlockPos.fromLong(buf.readLong());
        dimension = buf.readInt();
        redstoneMode = RedstoneMode.byIndex(buf.readInt());
    }

    public static class Handler implements IMessageHandler<MessageRedstoneMode, IMessage> {
        @Override
        @Nullable
        public IMessage onMessage(MessageRedstoneMode message, MessageContext ctx) {
            WorldServer world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(message.dimension);
            return DMLPacketHandler.handleMessageServer(ctx, () -> {
                TileEntityMachine te = (TileEntityMachine) world.getTileEntity(message.pos);
                if (te != null) {
                    te.setRedstoneMode(message.redstoneMode);
                }
            });
        }
    }
}
