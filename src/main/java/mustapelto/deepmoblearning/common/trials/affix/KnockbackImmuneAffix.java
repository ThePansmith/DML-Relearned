package mustapelto.deepmoblearning.common.trials.affix;

import mustapelto.deepmoblearning.DMLConstants;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class KnockbackImmuneAffix extends TrialAffix {

    @Override
    public String getId() {
        return DMLConstants.Trials.Affix.KNOCKBACK_IMMUNITY;
    }

    @Override
    public KnockbackImmuneAffix copy(BlockPos pos, World world) {
        return new KnockbackImmuneAffix();
    }

    @Override
    public void apply(EntityLiving entity) {
        IAttributeInstance knockbackResist = entity.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE);
        // todo constant?
        AttributeModifier knockbackMod = new AttributeModifier(DMLConstants.ModInfo.ID + ".ATTRIBUTE_KNOCKBACKRESIST", 1, 0);
        knockbackResist.applyModifier(knockbackMod);
    }

    @Override
    public String getAffixName() {
        return TextFormatting.DARK_GRAY + I18n.format("deepmoblearning.affix.knockback_immunity.name") + TextFormatting.RESET;
    }
}
