package mustapelto.deepmoblearning.common.network;

import io.netty.buffer.ByteBuf;
import mustapelto.deepmoblearning.common.tiles.TileEntityBase;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public class MessageUpdateTileEntity implements IMessage {
    private ByteBuf payload;

    public MessageUpdateTileEntity() {}

    public MessageUpdateTileEntity(ByteBuf payload) {
        this.payload = payload.copy();
    }

    public MessageUpdateTileEntity(TileEntityBase te) {
        this(te.getUpdateData());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBytes(payload);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        payload = buf.copy();
    }

    public static class Handler implements IMessageHandler<MessageUpdateTileEntity, IMessage> {
        @Override
        @Nullable
        public IMessage onMessage(MessageUpdateTileEntity message, MessageContext ctx) {
            return DMLPacketHandler.handleMessageClient(ctx, () -> {
                BlockPos pos = BlockPos.fromLong(message.payload.readLong());
                TileEntityBase te = (TileEntityBase) Minecraft.getMinecraft().world.getTileEntity(pos);
                if (te != null) {
                    te.handleUpdateData(message.payload);
                }
            });
        }
    }
}
