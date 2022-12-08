package mustapelto.deepmoblearning.common.trials.affix;

import mustapelto.deepmoblearning.DMLConstants;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLiving;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

@SuppressWarnings("ConstantConditions")
public class SpeedAffix extends TrialAffix {

    private final PotionEffect EFFECT = new PotionEffect(Potion.REGISTRY.getObjectById(1), Integer.MAX_VALUE, 0);

    @Override
    public String getId() {
        return DMLConstants.Trials.Affix.SPEED;
    }

    @Override
    public TrialAffix copy(BlockPos pos, World world) {
        return new SpeedAffix();
    }

    @Override
    public void apply(EntityLiving entity) {
        entity.addPotionEffect(EFFECT);
    }

    @Override
    public String getAffixName() {
        return TextFormatting.AQUA + I18n.format("deepmoblearning.affix.speed.name") + TextFormatting.RESET;
    }
}
