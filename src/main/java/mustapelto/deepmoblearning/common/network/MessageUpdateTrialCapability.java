package mustapelto.deepmoblearning.common.network;

import io.netty.buffer.ByteBuf;
import mustapelto.deepmoblearning.DMLRelearned;
import mustapelto.deepmoblearning.common.capability.CapabilityPlayerTrial;
import mustapelto.deepmoblearning.common.capability.CapabilityPlayerTrialProvider;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public class MessageUpdateTrialCapability implements IMessage {

    private NBTTagCompound compound;

    public MessageUpdateTrialCapability() {
    }

    public MessageUpdateTrialCapability(CapabilityPlayerTrial instance) {
        compound = instance.writeNBT(CapabilityPlayerTrialProvider.PLAYER_TRIAL_CAP, instance, null);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        compound = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, compound);
    }

    public static class Handler implements IMessageHandler<MessageUpdateTrialCapability, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(MessageUpdateTrialCapability message, MessageContext ctx) {
            CapabilityPlayerTrial capability = (CapabilityPlayerTrial) DMLRelearned.proxy.getClientPlayerTrialCapability();
            capability.readNBT(CapabilityPlayerTrialProvider.PLAYER_TRIAL_CAP, capability, null, message.compound);
            return null;
        }
    }
}
