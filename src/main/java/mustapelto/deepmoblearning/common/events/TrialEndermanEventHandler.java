package mustapelto.deepmoblearning.common.events;

import mustapelto.deepmoblearning.common.entities.EntityTrialEnderman;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber
public class TrialEndermanEventHandler {

    @SubscribeEvent
    public static void onEndermanTeleport(EnderTeleportEvent event) {
        if (event.getEntityLiving() instanceof EntityTrialEnderman) {
            event.setCanceled(true);
        }
    }
}
