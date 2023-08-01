package mustapelto.deepmoblearning;

import com.google.common.collect.ImmutableList;
import mustapelto.deepmoblearning.common.util.Point;
import mustapelto.deepmoblearning.common.util.Rect;
import net.minecraft.util.ResourceLocation;

public class DMLConstants {
    public static final class ModInfo {
        public static final String ID = Tags.MODID;
        public static final String NAME = Tags.MODNAME;
        public static final String VERSION = Tags.VERSION;
        public static final String CONFIG_PATH = "deepmobevolution";
    }

    public static final String MINECRAFT = "minecraft";

    public static final class ModDependencies {
        public static final String PATCHOULI = "patchouli";
        public static final String DEP_STRING = "required-after:" + PATCHOULI;
    }

    public static final class Crafting {
        public static final int GLITCH_FRAGMENTS_PER_HEART = 3;
        public static final int SOOTED_REDSTONE_PER_REDSTONE = 1;
    }

    public static final class GlitchSword {
        public static final int DAMAGE_BONUS_INCREASE = 2;
        public static final int DAMAGE_BONUS_MAX = 18;
        public static final int DAMAGE_INCREASE_CHANCE = 6;
    }

    public static final class SimulationChamber {
        public static final int ENERGY_CAPACITY = 2000000;
        public static final int ENERGY_IN_MAX = 25600;
    }

    public static final class TrialKeystone {
        public static final int TRIAL_AREA_RADIUS = 7; // Block radius of area that must be solid blocks, not including keystone itself
        public static final int TRIAL_AREA_HEIGHT = 9; // Block height of area that must be air blocks, not including keystone layer
        public static final int TRIAL_ARENA_RADIUS = 21; // Geometric radius of area inside of which players are considered to be part of a trial
    }

    public static final class DefaultModels {
        public static final ResourceLocation DATA_MODEL = new ResourceLocation(DMLConstants.ModInfo.ID, "items/data_model_default");
        public static final ResourceLocation LIVING_MATTER = new ResourceLocation(DMLConstants.ModInfo.ID, "items/living_matter_default");
        public static final ResourceLocation PRISTINE_MATTER = new ResourceLocation(DMLConstants.ModInfo.ID, "items/pristine_matter_default");
    }

    public static final class Trials {
        public static final String TRIAL_KEYSTONE_POS = ModInfo.ID + ":tilepos"; // NBT key for the position of the Trial Keystone
        public static final String TRIAL_AFFIX_CONNECTION = ModInfo.ID + ":mob_type"; // NBT key for an affix on a mob

        public static final class Affix {
            public static final String SPEED = "speed";
            public static final String REGEN_PARTY = "regen_party";
            public static final String EMPOWERED_GLITCHES = "empowered_glitches";
            public static final String KNOCKBACK_IMMUNITY = "knockback_immunity";
            public static final String BLAZE_INVADERS = "blaze_invaders";
            public static final String LOOT_HOARDERS = "loot_hoarders";
            public static final String THUNDERDOME = "thunderdome";
        }

        public static final class Message {
            public static final String TRIAL_ABORT = "TrialAborted";
            public static final String TRIAL_COMPLETE = "TrialCompleted";
            public static final String WAVE_COUNTDOWN = "WaveCountdown";
            public static final String WAVE_NUMBER = "WaveNumber";
            public static final String GLITCH_NOTIF = "GlitchNotification";
        }
    }

    public static final class Sounds {
        public static final String WAVE_START = "waveStart";
        public static final String GLITCH_ALERT = "glitchAlert";
        public static final String WAVE_COUNTDOWN = "waveCountdown";
        public static final String TRIAL_WON = "trialWon";
    }

    public static final class Gui {
        public static final int ROW_SPACING = 12;

        public static final class IDs {
            public static final int DEEP_LEARNER = 0;
            public static final int TILE_ENTITY = 1;
        }

        public static final class Colors {
            public static final int AQUA = 0x62D8FF;
            public static final int WHITE = 0xFFFFFF;
            public static final int LIME = 0x00FFC0;
            public static final int BRIGHT_LIME = 0x33EFDC;
            public static final int BRIGHT_PURPLE = 0xC768DB;
        }

        public static final class DeepLearner {
            public static final Point PLAYER_INVENTORY = new Point(81, 145);
            public static final ImmutableList<Point> DATA_MODEL_SLOTS = ImmutableList.of(
                    new Point(257, 100),
                    new Point(275, 100),
                    new Point(257, 118),
                    new Point(275, 118)
            );
        }

        public static final class SimulationChamber {
            public static final Point PLAYER_INVENTORY = new Point(28, 145);
            public static final Rect DATA_MODEL_SLOT = new Rect(-14, 0, 18, 18);
            public static final Point POLYMER_SLOT = new Point(192, 7);
            public static final Point LIVING_MATTER_SLOT = new Point(182, 27);
            public static final Point PRISTINE_MATTER_SLOT = new Point(202, 27);
        }

        public static final class LootFabricator {
            public static final Point PLAYER_INVENTORY = new Point(0, 88);
            public static final Point INPUT_SLOT = new Point(79, 62);
            public static final Point OUTPUT_FIRST_SLOT = new Point(101, 7);
            public static final int OUTPUT_SLOT_SIDE_LENGTH = 18;
        }

        public static final class TrialKeystone {
            public static final Point PLAYER_INVENTORY = new Point(12, 106);
            public static final Rect TRIAL_KEY_SLOT = new Rect(-20, 0, 18, 18);
        }
    }
}
