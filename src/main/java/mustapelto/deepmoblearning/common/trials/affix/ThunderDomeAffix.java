package mustapelto.deepmoblearning.common.trials.affix;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;

import java.util.concurrent.ThreadLocalRandom;

public class ThunderDomeAffix implements ITrialAffix {

    private final BlockPos pos;
    private final World world;
    private int ticks = 0;
    private final boolean wasRaining;
    private final boolean wasThundering;

    public ThunderDomeAffix(BlockPos pos, World world) {
        this.pos = pos;
        this.world = world;
        this.wasRaining = world.getWorldInfo().isRaining();
        this.wasThundering = world.getWorldInfo().isThundering();
    }

    @Override
    public void run() {
        // Do once every cycle, enable weather effects
        if(ticks == 0) {
            WorldInfo info = world.getWorldInfo();
            info.setCleanWeatherTime(0);
            info.setRaining(true);
            info.setThundering(true);
            info.setThunderTime(400); // 20 seconds
            info.setRainTime(400); // 20 seconds
        }

        ticks++;

        // Once every 15 seconds
        if(ticks % 300 == 0) {
            // 22% chance
            ThreadLocalRandom rand = ThreadLocalRandom.current();
            if (rand.nextInt(1, 100) < 22) {
                int randomX = pos.getX() + rand.nextInt(-5, 5);
                int randomY = pos.getY() + rand.nextInt(0, 1);
                int randomZ = pos.getZ() + rand.nextInt(-5, 5);

                if (rand.nextInt(1, 100) < 33) {
                    EntityCreeper creeper = new EntityCreeper(world);
                    creeper.setLocationAndAngles(randomX, randomY, randomZ, 0, 0);

                    NBTTagCompound tag = new NBTTagCompound();
                    tag = creeper.writeToNBT(tag);
                    tag.setBoolean("powered", true);
                    creeper.readEntityFromNBT(tag);

                    world.spawnEntity(creeper);
                } else {
                    EntityWitch witch = new EntityWitch(world);
                    witch.setLocationAndAngles(randomX, randomY, randomZ, 0, 0);
                    world.spawnEntity(witch);
                }
            }
            ticks = 0;
        }
    }

    @Override
    public void cleanUp() {
        /* Go back to the initial weather state */
        WorldInfo info = world.getWorldInfo();
        info.setCleanWeatherTime(0);
        info.setRaining(wasRaining);
        info.setThundering(wasThundering);
    }

    @Override
    public String getAffixName() {
        return TextFormatting.BLUE + I18n.format("deepmoblearning.affix.thunderdome.name") + TextFormatting.RESET;
    }
}
