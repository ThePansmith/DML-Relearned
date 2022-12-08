package mustapelto.deepmoblearning.common.trials.affix;

import mustapelto.deepmoblearning.DMLConstants;
import mustapelto.deepmoblearning.common.entities.EntityGlitch;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class EmpoweredGlitchAffix extends TrialAffix {

    @Override
    public String getId() {
        return DMLConstants.Trials.Affix.EMPOWERED_GLITCHES;
    }

    @Override
    public EmpoweredGlitchAffix copy(BlockPos pos, World world) {
        return new EmpoweredGlitchAffix();
    }

    @Override
    public void applyToGlitch(EntityGlitch entity) {
        entity.setEmpowered(true);
        IAttributeInstance health = entity.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);

        // TODO Constant?
        AttributeModifier healthMod = new AttributeModifier(DMLConstants.ModInfo.ID + ".ATTRIBUTE_MAX_HEALTH", 10, 0);
        health.applyModifier(healthMod);
        entity.setHealth(entity.getMaxHealth());
    }

    @Override
    public String getAffixName() {
        return TextFormatting.DARK_AQUA + I18n.format("deepmoblearning.affix.empowered_glitches.name") + TextFormatting.RESET;
    }
}
