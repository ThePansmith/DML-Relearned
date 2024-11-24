package mustapelto.deepmoblearning.client.jei;

import mezz.jei.api.gui.IAdvancedGuiHandler;
import mustapelto.deepmoblearning.client.gui.GuiMachine;
import mustapelto.deepmoblearning.client.gui.GuiTrialKeystone;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

public class GuiExclusionHandlers {
    public static class MachineGuiExclusion implements IAdvancedGuiHandler<GuiMachine> {

        @Override
        public Class<GuiMachine> getGuiContainerClass() {
            return GuiMachine.class;
        }

        @Override
        public @Nullable List<Rectangle> getGuiExtraAreas(GuiMachine guiContainer) {
            return guiContainer.getGuiExclusionAreas();
        }
    }

    public static class TrialGuiExclusion implements IAdvancedGuiHandler<GuiTrialKeystone> {

        @Override
        public Class<GuiTrialKeystone> getGuiContainerClass() {
            return GuiTrialKeystone.class;
        }

        @Override
        public @Nullable List<Rectangle> getGuiExtraAreas(GuiTrialKeystone guiContainer) {
            return guiContainer.getGuiExclusionAreas();
        }
    }
}
