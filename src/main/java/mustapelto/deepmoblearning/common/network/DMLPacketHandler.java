package mustapelto.deepmoblearning.common.network;

import mustapelto.deepmoblearning.DMLConstants;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber
public class DMLPacketHandler {
    public static final SimpleNetworkWrapper network = NetworkRegistry.INSTANCE.newSimpleChannel(DMLConstants.ModInfo.ID);
    private static int id = 0;

    public static void registerPackets() {
        network.registerMessage(MessageLivingMatterConsume.Handler.class, MessageLivingMatterConsume.class, id++, Side.SERVER);
        network.registerMessage(MessageLevelUpModel.Handler.class, MessageLevelUpModel.class, id++, Side.SERVER);
        network.registerMessage(MessageUpdateTileEntity.Handler.class, MessageUpdateTileEntity.class, id++, Side.CLIENT);
        network.registerMessage(MessageRequestUpdateTileEntity.Handler.class, MessageRequestUpdateTileEntity.class, id++, Side.SERVER);
        network.registerMessage(MessageRedstoneMode.Handler.class, MessageRedstoneMode.class, id++, Side.SERVER);
        network.registerMessage(MessageLootFabOutputItem.Handler.class, MessageLootFabOutputItem.class, id++, Side.SERVER);
        network.registerMessage(MessageCraftingState.Handler.class, MessageCraftingState.class, id++, Side.CLIENT);
        network.registerMessage(MessageTrialStart.Handler.class, MessageTrialStart.class, id++, Side.SERVER);
        network.registerMessage(MessageUpdateTrialCapability.Handler.class, MessageUpdateTrialCapability.class, id++, Side.CLIENT);
        network.registerMessage(MessageTrialOverlay.Handler.class, MessageTrialOverlay.class, id++, Side.CLIENT);
    }

    public static void sendToClient(IMessage message, World world, BlockPos pos) {
        network.sendToAllTracking(message, new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 0));
    }

    public static void sendToServer(IMessage message) {
        network.sendToServer(message);
    }

    public static void sendToClientPlayer(IMessage message, EntityPlayerMP player) {
        network.sendTo(message, player);
    }
}
