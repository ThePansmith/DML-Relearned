package mustapelto.deepmoblearning.common.trials.affix;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.concurrent.ThreadLocalRandom;

public class BlazeInvadersAffix implements ITrialAffix {

    private final BlockPos pos;
    private final World world;
    private int ticks = 0;

    public BlazeInvadersAffix(BlockPos pos, World world) {
        this.pos = pos;
        this.world = world;
    }

    @Override
    public String getAffixName() {
        return TextFormatting.RED + I18n.format("deepmoblearning.affix.blaze_invaders.name") + TextFormatting.RESET;
    }

    @Override
    public void run() {
        ticks++;
        // Once every 15 seconds 34% chance
        if (ticks % 300 == 0) {
            ThreadLocalRandom rand = ThreadLocalRandom.current();
            if (rand.nextInt(1, 100) > 66) {
                EntityBlaze blaze = new EntityBlaze(world);

                int randomX = pos.getX() + rand.nextInt(-5, 5);
                int randomY = pos.getY() + rand.nextInt(0, 1);
                int randomZ = pos.getZ() + rand.nextInt(-5, 5);
                blaze.setLocationAndAngles(randomX, randomY, randomZ, 0, 0);

                world.spawnEntity(blaze);
            }
            ticks = 0;
        }
    }
}
