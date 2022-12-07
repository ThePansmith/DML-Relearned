package mustapelto.deepmoblearning.common.trials.affix;

import mustapelto.deepmoblearning.common.entities.EntityGlitch;
import net.minecraft.entity.EntityLiving;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ITrialAffix {

    @SideOnly(Side.CLIENT)
    String getAffixName();

    // Run will run every update tick from the Trial Keystone, it's up to the implementing class to stagger this to avoid performance issues.
    default void run() {
    }

    default void cleanUp() {
    }

    default void apply(EntityLiving entity) {
    }

    default void applyToGlitch(EntityGlitch entity) {
    }
}
