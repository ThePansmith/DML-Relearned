package mustapelto.deepmoblearning.common.util;

import com.google.common.collect.ImmutableList;
import mustapelto.deepmoblearning.common.trials.affix.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

import static mustapelto.deepmoblearning.DMLConstants.Trials.Affix;

public class AffixHelper {

    public static ITrialAffix createAffix(String key, BlockPos pos, World world) {
        ITrialAffix trial;

        switch (key) {
            case Affix.REGEN_PARTY:
                trial = new RegenPartyAffix(pos, world);
                break;
            case Affix.EMPOWERED_GLITCHES:
                trial = new EmpoweredGlitchAffix();
                break;
            case Affix.KNOCKBACK_IMMUNITY:
                trial = new KnockbackImmuneAffix();
                break;
            case Affix.BLAZE_INVADERS:
                trial = new BlazeInvadersAffix(pos, world);
                break;
            case Affix.LOOT_HOARDERS:
                trial = new LootHoarderAffix(pos, world);
                break;
            case Affix.THUNDERDOME:
                trial = new ThunderDomeAffix(pos, world);
                break;
            case Affix.SPEED:
            default:
                trial = new SpeedAffix();
                break;
        }
        return trial;
    }

    @Nullable
    public static String getRandomAffixKey(ImmutableList<String> excluding) {
        String[] keyList = {
                Affix.SPEED, Affix.REGEN_PARTY, Affix.EMPOWERED_GLITCHES,
                Affix.KNOCKBACK_IMMUNITY, Affix.BLAZE_INVADERS, Affix.LOOT_HOARDERS, Affix.THUNDERDOME};

        String key = keyList[new Random().nextInt(keyList.length)];
        boolean keyIsExcluded = excluding.contains(key);
        int length = keyList.length;

        if (excluding.size() >= length) {
            return null;
        }

        while (keyIsExcluded) {
            key = keyList[new Random().nextInt(keyList.length)];
            if(!excluding.contains(key)) {
                keyIsExcluded = false;
            }
        }
        return key;
    }
}
