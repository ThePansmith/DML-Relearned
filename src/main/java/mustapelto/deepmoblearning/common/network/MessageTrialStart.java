package mustapelto.deepmoblearning.common.network;

import io.netty.buffer.ByteBuf;
import mustapelto.deepmoblearning.common.tiles.TileEntityTrialKeystone;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public class MessageTrialStart implements IMessage {

    private BlockPos pos;
    private int dimension;

    public MessageTrialStart() {
    }

    public MessageTrialStart(BlockPos pos, int dimension) {
        this.pos = pos;
        this.dimension = dimension;
    }

    public MessageTrialStart(TileEntityTrialKeystone te) {
        this(te.getPos(), te.getWorld().provider.getDimension());
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = BlockPos.fromLong(buf.readLong());
        dimension = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
        buf.writeInt(dimension);
    }

    /* Handler for the message, registered in the mod class */
    public static class Handler implements IMessageHandler<MessageTrialStart, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(MessageTrialStart message, MessageContext ctx) {
            WorldServer world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(message.dimension);
            return DMLPacketHandler.handleMessageServer(ctx, () -> {
                TileEntityTrialKeystone te = (TileEntityTrialKeystone) world.getTileEntity(message.pos);
                if (te != null) {
                    te.startTrial();
                }
            });
        }
    }
}
