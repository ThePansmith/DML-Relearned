package mustapelto.deepmoblearning.common.network;

import io.netty.buffer.ByteBuf;
import mustapelto.deepmoblearning.common.items.ItemLivingMatter;
import mustapelto.deepmoblearning.common.util.ItemStackHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public class MessageLivingMatterConsume implements IMessage {
    private boolean consumeStack;

    public MessageLivingMatterConsume() {
        consumeStack = false;
    }

    public MessageLivingMatterConsume(boolean consumeStack) {
        this.consumeStack = consumeStack;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        consumeStack = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(consumeStack);
    }

    public static class Handler implements IMessageHandler<MessageLivingMatterConsume, IMessage> {
        @Override
        @Nullable
        public IMessage onMessage(MessageLivingMatterConsume message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            ItemStack mainHand = player.getHeldItemMainhand();
            ItemStack offHand = player.getHeldItemOffhand();

            if (ItemStackHelper.isLivingMatter(mainHand)) {
                DMLPacketHandler.handleMessageServer(ctx, () -> consumeMatter(mainHand, message.consumeStack, player));
            } else if (ItemStackHelper.isLivingMatter(offHand)) {
                DMLPacketHandler.handleMessageServer(ctx, () -> consumeMatter(offHand, message.consumeStack, player));
            }

            return null;
        }

        private void consumeMatter(ItemStack matterStack, boolean consumeStack, EntityPlayerMP player) {
            ItemLivingMatter matterItem = (ItemLivingMatter) matterStack.getItem();
            int xp = matterItem.getLivingMatterData().getXpValue();

            if (consumeStack) {
                int size = matterStack.getCount();
                matterStack.shrink(size);
                player.addExperience(xp * size);
            } else {
                matterStack.shrink(1);
                player.addExperience(xp);
            }
        }
    }
}
