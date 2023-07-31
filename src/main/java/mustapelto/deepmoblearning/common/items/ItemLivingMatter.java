package mustapelto.deepmoblearning.common.items;

import mustapelto.deepmoblearning.DMLRelearned;
import mustapelto.deepmoblearning.client.util.KeyboardHelper;
import mustapelto.deepmoblearning.common.metadata.MetadataLivingMatter;
import mustapelto.deepmoblearning.common.network.DMLPacketHandler;
import mustapelto.deepmoblearning.common.network.MessageLivingMatterConsume;
import mustapelto.deepmoblearning.common.util.DMLRHelper;
import mustapelto.deepmoblearning.common.util.StringHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemLivingMatter extends ItemBase {
    private final MetadataLivingMatter metadata;

    public ItemLivingMatter(MetadataLivingMatter metadata) {
        super(metadata.getRegistryID(), 64, DMLRHelper.isModLoaded(metadata.getModID()));
        this.metadata = metadata;
    }

    public MetadataLivingMatter getLivingMatterData() {
        return metadata;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        String useString = StringHelper.getFormattedString(TextFormatting.ITALIC, KeyboardHelper.getUseKeyName(), TextFormatting.GRAY);
        String sneakString = StringHelper.getFormattedString(TextFormatting.ITALIC, KeyboardHelper.getSneakKeyName(), TextFormatting.GRAY);
        tooltip.add(I18n.format("deepmoblearning.living_matter.consume_for_xp", useString));
        tooltip.add(I18n.format("deepmoblearning.living_matter.consume_stack", sneakString));
        tooltip.add(I18n.format("deepmoblearning.living_matter.xp", metadata.getXpValue()));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        if (worldIn.isRemote) {
            if (KeyboardHelper.isHoldingSneakKey()) {
                DMLPacketHandler.network.sendToServer(new MessageLivingMatterConsume(true));
            } else {
                DMLPacketHandler.network.sendToServer(new MessageLivingMatterConsume(false));
            }
        }

        return new ActionResult<>(EnumActionResult.PASS, playerIn.getHeldItem(handIn));
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return DMLRelearned.proxy.getLocalizedString("deepmoblearning.living_matter.display_name", metadata.getDisplayNameFormatted());
    }
}
