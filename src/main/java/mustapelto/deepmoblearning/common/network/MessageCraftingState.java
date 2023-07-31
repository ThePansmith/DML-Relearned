package mustapelto.deepmoblearning.common.network;

import io.netty.buffer.ByteBuf;
import mustapelto.deepmoblearning.common.tiles.CraftingState;
import mustapelto.deepmoblearning.common.tiles.TileEntityMachine;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public class MessageCraftingState implements IMessage {
    private BlockPos pos;
    private CraftingState craftingState;

    public MessageCraftingState() {
        craftingState = CraftingState.IDLE;
    }

    public MessageCraftingState(BlockPos pos, CraftingState craftingState) {
        this.pos = pos;
        this.craftingState = craftingState;
    }

    public MessageCraftingState(TileEntityMachine te) {
        this(te.getPos(), te.getCraftingState());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
        buf.writeInt(craftingState.getIndex());
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = BlockPos.fromLong(buf.readLong());
        craftingState = CraftingState.byIndex(buf.readInt());
    }

    public static class Handler implements IMessageHandler<MessageCraftingState, IMessage> {
        @Override
        @Nullable
        public IMessage onMessage(MessageCraftingState message, MessageContext ctx) {
            return DMLPacketHandler.handleMessageClient(ctx, () -> {
                TileEntityMachine te = (TileEntityMachine) Minecraft.getMinecraft().world.getTileEntity(message.pos);
                if (te != null) {
                    te.setCraftingState(message.craftingState);
                }
            });
        }
    }
}
