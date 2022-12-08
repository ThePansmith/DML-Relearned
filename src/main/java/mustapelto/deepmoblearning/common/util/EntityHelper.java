package mustapelto.deepmoblearning.common.util;

import mustapelto.deepmoblearning.DMLConstants;
import mustapelto.deepmoblearning.common.trials.affix.TrialAffix;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class EntityHelper {

    public static void spawnTrialMob(@Nullable EntityLiving entity, World world, BlockPos pos, List<TrialAffix> affixes) {
        if (entity == null) return;
        ThreadLocalRandom rand = ThreadLocalRandom.current();

        int randomX = pos.getX() + rand.nextInt(-5, 5);
        int randomY = pos.getY() + rand.nextInt(0, 1);
        int randomZ = pos.getZ() + rand.nextInt(-5, 5);

        entity.setLocationAndAngles(randomX, randomY, randomZ, 0 ,0);
        entity.getEntityData().setLong(DMLConstants.Trials.TRIAL_KEYSTONE_POS, pos.toLong());
        entity.enablePersistence();

        EntityPlayer target = entity.world.getNearestAttackablePlayer(entity.getPosition(), 32, 5);
        if(target != null && target.isEntityAlive()) {
            entity.setAttackTarget(target);
        }

        affixes.forEach(affix -> affix.apply(entity));

        // Do not spawn them all at once (once every 2 sec atm)
        world.spawnEntity(entity);
    }
}
