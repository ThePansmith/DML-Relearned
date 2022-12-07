package mustapelto.deepmoblearning.common.events;

import mustapelto.deepmoblearning.DMLConstants;
import mustapelto.deepmoblearning.client.gui.GuiTrialOverlay;
import mustapelto.deepmoblearning.common.capability.CapabilityPlayerTrial;
import mustapelto.deepmoblearning.common.capability.CapabilityPlayerTrialProvider;
import mustapelto.deepmoblearning.common.capability.ICapabilityPlayerTrial;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings("ConstantConditions")
@EventBusSubscriber
public class CapabilityEventHandler {

    public static final ResourceLocation PLAYER_TRIAL_CAP = new ResourceLocation(DMLConstants.ModInfo.ID, "player_trial");

    @SubscribeEvent
    public static void attachCapability(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            event.addCapability(PLAYER_TRIAL_CAP, new CapabilityPlayerTrialProvider());
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        EntityPlayer player = event.getEntityPlayer();

        if (!player.world.isRemote) {
            ICapabilityPlayerTrial cap = player.getCapability(CapabilityPlayerTrialProvider.PLAYER_TRIAL_CAP, null);
            ICapabilityPlayerTrial oldCap = event.getOriginal().getCapability(CapabilityPlayerTrialProvider.PLAYER_TRIAL_CAP, null);

            cap.setDefeated(oldCap.getDefated());
            cap.setCurrentWave(oldCap.getCurrentWave());
            cap.setLastWave(oldCap.getLastWave());
            cap.setWaveMobTotal(oldCap.getWaveMobTotal());
            cap.setTilePos(oldCap.getTilePos());
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onPlayerJoinedWorld(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityPlayerSP) {
            GuiTrialOverlay.initPlayerCapability();
        }
    }
}
