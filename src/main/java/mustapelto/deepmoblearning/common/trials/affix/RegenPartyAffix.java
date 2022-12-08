package mustapelto.deepmoblearning.common.trials.affix;

import mustapelto.deepmoblearning.DMLConstants;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.concurrent.ThreadLocalRandom;

public class RegenPartyAffix extends TrialAffix {

    private int ticks = 0;

    public RegenPartyAffix() {
        super();
    }

    public RegenPartyAffix(BlockPos pos, World world) {
        super(pos, world);
    }

    @Override
    public String getId() {
        return DMLConstants.Trials.Affix.REGEN_PARTY;
    }

    @Override
    public RegenPartyAffix copy(BlockPos pos, World world) {
        return new RegenPartyAffix(pos, world);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void run() {
        ticks++;
        // Run once every 16 seconds
        if (ticks % 220 == 0) {
            EntityPotion regenPot = new EntityPotion(world);
            ItemStack lingeringPotion = new ItemStack(Items.LINGERING_POTION);
            PotionUtils.addPotionToItemStack(lingeringPotion, PotionType.getPotionTypeForName("strong_regeneration"));
            regenPot.setItem(lingeringPotion);

            ThreadLocalRandom rand = ThreadLocalRandom.current();
            int randomX = pos.getX() + rand.nextInt(-5, 5);
            int randomY = pos.getY() + rand.nextInt(2, 9);
            int randomZ = pos.getZ() + rand.nextInt(-5, 5);
            regenPot.setLocationAndAngles(randomX, randomY, randomZ, 0 ,0);

            world.spawnEntity(regenPot);
            ticks = 0;
        }
    }

    @Override
    public String getAffixName() {
        return TextFormatting.LIGHT_PURPLE + I18n.format("deepmoblearning.affix.regen_party.name") + TextFormatting.RESET;
    }
}
