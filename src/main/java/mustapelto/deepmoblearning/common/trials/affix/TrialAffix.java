package mustapelto.deepmoblearning.common.trials.affix;

import mustapelto.deepmoblearning.common.entities.EntityGlitch;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TrialAffix {

    protected BlockPos pos;
    protected World world;

    public TrialAffix() {
    }

    public TrialAffix(BlockPos pos, World world) {
        this.pos = pos;
        this.world = world;
    }

    public abstract TrialAffix copy(BlockPos pos, World world);

    public abstract String getId();

    @SideOnly(Side.CLIENT)
    public abstract String getAffixName();

    // Run will run every update tick from the Trial Keystone, it's up to the implementing class to stagger this to avoid performance issues.
    public void run() {
    }

    public void cleanUp() {
    }

    public void apply(EntityLiving entity) {
    }

    public void applyToGlitch(EntityGlitch entity) {
    }
}
