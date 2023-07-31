package mustapelto.deepmoblearning.common.network;

import io.netty.buffer.ByteBuf;
import mustapelto.deepmoblearning.common.util.DataModelHelper;
import mustapelto.deepmoblearning.common.util.DataModelHelper.CreativeLevelUpAction;
import mustapelto.deepmoblearning.common.util.ItemStackHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public class MessageLevelUpModel implements IMessage {
    private CreativeLevelUpAction increaseTier; // false -> add one kill; true -> add one tier

    public MessageLevelUpModel() {}

    public MessageLevelUpModel(CreativeLevelUpAction increaseTier) {
        this.increaseTier = increaseTier;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        increaseTier = CreativeLevelUpAction.fromInt(buf.readInt());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(increaseTier.toInt());
    }

    public static class Handler implements IMessageHandler<MessageLevelUpModel, IMessage> {
        @Override
        @Nullable
        public IMessage onMessage(MessageLevelUpModel message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;

            if (ItemStackHelper.isCreativeModelLearner(player.getHeldItemMainhand()) || ItemStackHelper.isCreativeModelLearner(player.getHeldItemOffhand())) {
                DMLPacketHandler.handleMessageServer(ctx, () -> {
                    DataModelHelper.findAndLevelUpModels(player.inventory.mainInventory, player, message.increaseTier);
                    DataModelHelper.findAndLevelUpModels(player.inventory.offHandInventory, player, message.increaseTier);
                });
            }

            return null;
        }
    }
}
