package mustapelto.deepmoblearning.common.util;

import com.google.common.collect.ImmutableList;
import mustapelto.deepmoblearning.common.trials.affix.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static mustapelto.deepmoblearning.DMLConstants.Trials.Affix;

public class AffixHelper {

    private static final Map<String, TrialAffix> AFFIXES = new HashMap<>();

    public static void registerAffixes() {
        registerAffix(Affix.BLAZE_INVADERS, new BlazeInvadersAffix());
        registerAffix(Affix.EMPOWERED_GLITCHES, new EmpoweredGlitchAffix());
        registerAffix(Affix.KNOCKBACK_IMMUNITY, new KnockbackImmuneAffix());
        registerAffix(Affix.LOOT_HOARDERS, new LootHoarderAffix());
        registerAffix(Affix.REGEN_PARTY, new RegenPartyAffix());
        registerAffix(Affix.SPEED, new SpeedAffix());
        registerAffix(Affix.THUNDERDOME, new ThunderDomeAffix());
    }

    public static void registerAffix(String key, TrialAffix affix) {
        AFFIXES.put(key, affix);
    }

    @SuppressWarnings("unchecked")
    public static <T extends TrialAffix> T createAffix(String key, BlockPos pos, World world) {
        TrialAffix affixTemplate = AFFIXES.get(key);
        if (affixTemplate == null) return (T) new SpeedAffix();
        return (T) affixTemplate.copy(pos, world);
    }

    @Nullable
    public static String getRandomAffixKey(ImmutableList<String> excluding) {
        String[] keyList = AFFIXES.keySet().toArray(new String[0]);

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
