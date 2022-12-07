package mustapelto.deepmoblearning.common.network;

import io.netty.buffer.ByteBuf;
import mustapelto.deepmoblearning.client.gui.GuiTrialOverlay;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public class MessageTrialOverlay implements IMessage {

    private String type;

    public MessageTrialOverlay() {
    }

    public MessageTrialOverlay(String type) {
        this.type = type;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, type);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        type = ByteBufUtils.readUTF8String(buf);
    }

    public static class Handler implements IMessageHandler<MessageTrialOverlay, IMessage> {

        @Override
        @Nullable
        public IMessage onMessage(MessageTrialOverlay message, MessageContext ctx) {
            // todo scheduled task?
            GuiTrialOverlay.handleMessage(message.type);
            return null;
        }
    }
}
