package mustapelto.deepmoblearning.common;

import mustapelto.deepmoblearning.DMLConstants;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.*;

import java.util.HashMap;
import java.util.Map;

@Config(modid = DMLConstants.ModInfo.ID, name = DMLConstants.ModInfo.CONFIG_PATH + "/deepmobevolution", category = "")
@EventBusSubscriber
public class DMLConfig {
    @Name("Misc Settings")
    public static MiscSettings MISC_SETTINGS = new MiscSettings();

    public static class MiscSettings {
        @Name("Show Data Model tier in item name?")
        public boolean SHOW_TIER_IN_NAME = true;

        @Name("Is Soot-Covered Redstone Crafting Enabled?")
        public boolean SOOT_COVERED_REDSTONE_CRAFTING_ENABLED = true;
    }

    @Name("Machine Settings")
    public static MachineSettings MACHINE_SETTINGS = new MachineSettings();

    public static class MachineSettings {
        @Name("Simulation Chamber Processing Time")
        @Comment("Time it takes for the Simulation Chamber to run one iteration (in ticks)")
        @RangeInt(min = 1)
        public int SIMULATION_CHAMBER_PROCESSING_TIME = 301;

        @Name("Loot Fabricator RF Cost")
        @Comment("Energy cost of Loot Fabricator in RF/t")
        @RangeInt(min = 0)
        public int LOOT_FABRICATOR_RF_COST = 256;

        @Name("Loot Fabricator Processing Time")
        @Comment("Time it takes for the Loot Fabricator to process one item (in ticks)")
        @RangeInt(min = 1)
        public int LOOT_FABRICATOR_PROCESSING_TIME = 51;

        @Name("Legacy Machine Sidedness")
        @Comment("Use legacy sidedness (insert from top, output to all other sides)?")
        public boolean LEGACY_MACHINE_SIDEDNESS = false;
    }

    @Name("Glitch Armor Settings")
    public static GlitchArmorSettings GLITCH_ARMOR_SETTINGS = new GlitchArmorSettings();

    public static class GlitchArmorSettings {
        @Name("Glitch Armor Pristine Chance")
        @Comment("Chance to drop Pristine Matter on Data Model mob kill with full Glitch Armor equipped")
        @RangeInt(min = 0, max = 100)
        public int GLITCH_ARMOR_PRISTINE_CHANCE = 18;

        @Name("Glitch Armor Pristine Count")
        @Comment("Number of Pristine Matter to drop on Data Model mob kill with full Glitch Armor equipped")
        @RangeInt(min = 0, max = 64)
        public int GLITCH_ARMOR_PRISTINE_COUNT = 2;

        @Name("Is Glitch Armor Creative Flight Enabled?")
        public boolean GLITCH_CREATIVE_FLIGHT_ENABLED = true;
    }

    @Name("Deep Learner GUI Overlay Settings")
    public static DeepLearnerGuiOverlaySettings DEEP_LEARNER_GUI_OVERLAY_SETTINGS = new DeepLearnerGuiOverlaySettings();

    public static class DeepLearnerGuiOverlaySettings {
        @Name("Position")
        @Comment("Overlay screen position. Valid values: topleft / topright / bottomright / bottomleft")
        public String POSITION = "topleft";

        @Name("Horizontal Padding")
        @Comment("Horizontal padding from selected corner")
        public int PADDING_HORIZONTAL = 0;

        @Name("Vertical Padding")
        @Comment("Vertical padding from selected corner")
        public int PADDING_VERTICAL = 0;

        public enum GuiPosition {
            TOP_LEFT("topleft"),
            TOP_RIGHT("topright"),
            BOTTOM_RIGHT("bottomright"),
            BOTTOM_LEFT("bottomleft");

            private final String value;
            private static final Map<String, GuiPosition> map = new HashMap<>();

            GuiPosition(String value) {
                this.value = value;
            }

            static {
                for (GuiPosition position : values())
                    map.put(position.value, position);
            }

            public static GuiPosition fromString(String positionString) {
                return map.getOrDefault(positionString, GuiPosition.TOP_LEFT);
            }
        }
        public GuiPosition getGuiPosition() {
            return GuiPosition.fromString(POSITION);
        }
    }

    @SubscribeEvent
    public static void onConfigChanged(OnConfigChangedEvent event) {
        if (event.getModID().equals(DMLConstants.ModInfo.ID)) {
            ConfigManager.sync(DMLConstants.ModInfo.ID, Config.Type.INSTANCE);
        }
    }
}
