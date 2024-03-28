package mustapelto.deepmoblearning.client.gui;

import mustapelto.deepmoblearning.DMLConstants;
import mustapelto.deepmoblearning.DMLConstants.Gui.Colors;
import mustapelto.deepmoblearning.client.util.StringAnimator;
import mustapelto.deepmoblearning.common.tiles.TileEntitySimulationChamber;
import mustapelto.deepmoblearning.common.util.DataModelHelper;
import mustapelto.deepmoblearning.common.util.Point;
import mustapelto.deepmoblearning.common.util.Rect;
import mustapelto.deepmoblearning.common.util.StringHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

import static mustapelto.deepmoblearning.DMLConstants.Gui.ROW_SPACING;
import static mustapelto.deepmoblearning.DMLConstants.Gui.SimulationChamber.*;

public class GuiSimulationChamber extends GuiMachine {
    // TEXTURES
    private static final ResourceLocation TEXTURE = new ResourceLocation(DMLConstants.ModInfo.ID, "textures/gui/simulation_chamber.png");
    private static final class TextureCoords {
        private static final Point MAIN_GUI = new Point(0, 0);
        private static final Point DATA_BAR = new Point(18, 141);
        private static final Point ENERGY_BAR = new Point(25, 141);
        private static final Point DATA_MODEL_SLOT = new Point(0, 141);
    }

    // DIMENSIONS
    private static final int WIDTH = 232;
    private static final int HEIGHT = 230;
    private static final Rect MAIN_GUI = new Rect(8, 0, 216, 141);

    // STATUS DISPLAY
    private static final Point INFO_BOX = new Point(18, 9);
    private static final Point CONSOLE = new Point(29, 51);
    private static final int REDSTONE_DEACTIVATED_LINE_LENGTH = 28;
    private static final int BLINKING_CURSOR_SPEED = 16;

    // XP / ENERGY BAR LOCATIONS
    private static final Rect DATA_BAR = new Rect(14, 47, 7,87);
    private static final Rect ENERGY_BAR = new Rect(211, 47, 7, 87);

    // BUTTON LOCATIONS
    private static final Point REDSTONE_BUTTON = new Point(-14, 24);

    // ANIMATORS (for animated strings)
    private final StringAnimator progressAnimator = new StringAnimator(); // Used to display simulation progress
    private final StringAnimator emptyDisplayAnimator = new StringAnimator(); // Used to display empty screen ("blinking cursor")
    private final StringAnimator dataModelErrorAnimator = new StringAnimator(); // Used to display error messages relating to data model
    private final StringAnimator simulationErrorAnimator= new StringAnimator(); // Used to display other errors (no polymer/energy, output full)

    // STATE VARIABLES
    private final TileEntitySimulationChamber simulationChamber;
    private ItemStack dataModel; // Data Model currently inside Simulation Chamber

    private DataModelError dataModelError = DataModelError.NONE; // Error with model (missing/faulty)?
    private SimulationError simulationError = SimulationError.NONE; // Other error (missing polymer/low energy/output full)?
    private boolean redstoneDeactivated = false; // Is simulation chamber deactivated by redstone signal?
    private int currentIteration; // Saves data model's current iteration so we don't update display if iteration hasn't changed
    private boolean currentPristineSuccess; // Saves data model's current pristine success state so we don't update display if iteration hasn't changed

    //
    // INIT
    //

    public GuiSimulationChamber(TileEntitySimulationChamber tileEntity, EntityPlayer player, World world) {
        super(tileEntity, player, world, WIDTH, HEIGHT, REDSTONE_BUTTON);
        simulationChamber = tileEntity;
        dataModel = tileEntity.getDataModel();
        prepareStringAnimators();
    }

    //
    // UPDATE
    //

    @Override
    public void updateScreen() {
        super.updateScreen();
        dataModel = simulationChamber.getDataModel(); // Update data model

        //
        // Check for Data Model errors and update animator
        //
        if (!simulationChamber.hasDataModel()) {
            if (dataModelError == DataModelError.MISSING)
                return;

            dataModelErrorAnimator.setString(AnimatedString.ERROR_DATA_MODEL_TEXT_1, I18n.format("deepmoblearning.simulation_chamber.error_text.no_data_model_1"));
            dataModelErrorAnimator.setString(AnimatedString.ERROR_DATA_MODEL_TEXT_2, I18n.format("deepmoblearning.simulation_chamber.error_text.no_data_model_2"));
            dataModelErrorAnimator.reset();
            dataModelError = DataModelError.MISSING;
            return;
        }

        if (!simulationChamber.canDataModelSimulate()) {
            if (dataModelError == DataModelError.FAULTY)
                return;

            dataModelErrorAnimator.setString(AnimatedString.ERROR_DATA_MODEL_TEXT_1, I18n.format("deepmoblearning.simulation_chamber.error_text.model_cannot_simulate_1"));
            dataModelErrorAnimator.setString(AnimatedString.ERROR_DATA_MODEL_TEXT_2, I18n.format("deepmoblearning.simulation_chamber.error_text.model_cannot_simulate_2"));
            dataModelErrorAnimator.reset();
            dataModelError = DataModelError.FAULTY;
            return;
        }

        // No Data Model errors found
        dataModelError = DataModelError.NONE;
        emptyDisplayAnimator.reset();

        // Check redstone state
        redstoneDeactivated = !tileEntity.isRedstoneActive();
        if (redstoneDeactivated)
            return;

        //
        // Check for simulation errors and update animator
        //
        if (!simulationChamber.hasPolymerClay() && !tileEntity.isCrafting()) {
            // Polymer error only shown if simulation hasn't started already
            // (remaining polymer can be removed while simulation is running, which should not cause an error display)
            if (simulationError == SimulationError.POLYMER)
                return;

            simulationErrorAnimator.setString(AnimatedString.ERROR_SIMULATION_TEXT, I18n.format("deepmoblearning.simulation_chamber.error_text.no_polymer"));
            simulationErrorAnimator.reset();
            simulationError = SimulationError.POLYMER;
            return;
        }

        if (!tileEntity.hasEnergyForCrafting() || !tileEntity.isCrafting() && !tileEntity.canStartCrafting()) {
            if (simulationError == SimulationError.ENERGY)
                return;

            simulationErrorAnimator.setString(AnimatedString.ERROR_SIMULATION_TEXT, I18n.format("deepmoblearning.simulation_chamber.error_text.no_energy"));
            simulationErrorAnimator.reset();
            simulationError = SimulationError.ENERGY;
            return;
        }

        if ((simulationChamber.isPristineMatterOutputFull() || simulationChamber.isLivingMatterOutputFull())) {
            if (simulationError == SimulationError.OUTPUT)
                return;

            simulationErrorAnimator.setString(AnimatedString.ERROR_SIMULATION_TEXT, I18n.format("deepmoblearning.simulation_chamber.error_text.output_full"));
            simulationErrorAnimator.reset();
            simulationError = SimulationError.OUTPUT;
            return;
        }

        // No simulation errors found
        simulationError = SimulationError.NONE;

        // Update data for current iteration
        int iteration = DataModelHelper.getTotalSimulationCount(dataModel) + 1;
        boolean pristineSuccess = simulationChamber.isPristineSuccess();

        if ((iteration == currentIteration) && (pristineSuccess == currentPristineSuccess))
            return; // Already updated, no need to do it again

        currentIteration = iteration;
        currentPristineSuccess = pristineSuccess;

        String iterationString = I18n.format("deepmoblearning.simulation_chamber.simulation_text.iteration", iteration);
        progressAnimator.setString(AnimatedString.SIMULATION_ITERATION, iterationString);


        String pristineString = I18n.format("deepmoblearning.simulation_chamber.simulation_text.pristine");
        String successString = TextFormatting.GREEN + I18n.format("deepmoblearning.simulation_chamber.simulation_text.pristine_success") + TextFormatting.RESET;
        String failureString = TextFormatting.RED + I18n.format("deepmoblearning.simulation_chamber.simulation_text.pristine_failure") + TextFormatting.RESET;
        pristineString += " " + (pristineSuccess ? successString : failureString);
        progressAnimator.setString(AnimatedString.SIMULATION_PRISTINE, pristineString);
        progressAnimator.reset();
    }

    //
    // DRAWING
    //

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        final int x = mouseX - guiLeft;
        final int y = mouseY - guiTop;

        List<String> tooltip = new ArrayList<>();

        if (DATA_BAR.isInside(x, y)) {
            // Draw Data Bar Tooltip
            if (simulationChamber.hasDataModel()) {
                if (!DataModelHelper.isMaxTier(dataModel)) {
                    String currentData = String.valueOf(DataModelHelper.getCurrentTierDataCount(dataModel));
                    String maxData = String.valueOf(DataModelHelper.getTierRequiredData(dataModel));
                    tooltip.add(I18n.format("deepmoblearning.simulation_chamber.tooltip.model_data", currentData + "/" + maxData));
                } else {
                    tooltip.add(I18n.format("deepmoblearning.simulation_chamber.tooltip.model_max"));
                }
                if (!DataModelHelper.canSimulate(dataModel)) {
                    tooltip.add(TextFormatting.RED + I18n.format("deepmoblearning.simulation_chamber.tooltip.model_cannot_simulate") + TextFormatting.RESET);
                }
            } else {
                tooltip.add(I18n.format("deepmoblearning.simulation_chamber.tooltip.model_missing"));
            }
            drawHoveringText(tooltip, x, y);
        } else if (ENERGY_BAR.isInside(x, y)) {
            // Draw Energy Bar Tooltip
            String currentEnergy = String.valueOf(tileEntity.getEnergy());
            String maxEnergy = String.valueOf(tileEntity.getMaxEnergy());
            tooltip.add(currentEnergy + "/" + maxEnergy + " RF");
            if (simulationChamber.hasDataModel()) {
                int energyDrain = tileEntity.getCraftingEnergyCost();
                tooltip.add(I18n.format("deepmoblearning.simulation_chamber.tooltip.sim_cost", energyDrain));
            }
            drawHoveringText(tooltip, x, y);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        textureManager.bindTexture(TEXTURE);
        GlStateManager.color(1f, 1f, 1f, 1f);

        // Main GUI
        drawTexturedModalRect(
                guiLeft + MAIN_GUI.LEFT,
                guiTop + MAIN_GUI.TOP,
                TextureCoords.MAIN_GUI.X,
                TextureCoords.MAIN_GUI.Y,
                MAIN_GUI.WIDTH,
                MAIN_GUI.HEIGHT
        );

        // Data Model Slot
        drawTexturedModalRect(
                guiLeft + DATA_MODEL_SLOT.LEFT,
                guiTop + DATA_MODEL_SLOT.TOP,
                TextureCoords.DATA_MODEL_SLOT.X,
                TextureCoords.DATA_MODEL_SLOT.Y,
                DATA_MODEL_SLOT.WIDTH,
                DATA_MODEL_SLOT.HEIGHT
        );

        // Data Model Experience Bar
        if (dataModelError == DataModelError.NONE) {
            int dataBarHeight;
            if (DataModelHelper.isMaxTier(dataModel)) {
                dataBarHeight = DATA_BAR.HEIGHT;
            } else {
                int currentData = DataModelHelper.getCurrentTierDataCount(dataModel);
                int tierMaxData = DataModelHelper.getTierRequiredData(dataModel);
                dataBarHeight = (int) (((float) currentData / tierMaxData) * DATA_BAR.HEIGHT);
            }
            int dataBarOffset = DATA_BAR.HEIGHT - dataBarHeight;
            drawTexturedModalRect(
                    guiLeft + DATA_BAR.LEFT,
                    guiTop + DATA_BAR.TOP + dataBarOffset,
                    TextureCoords.DATA_BAR.X,
                    TextureCoords.DATA_BAR.Y,
                    DATA_BAR.WIDTH,
                    dataBarHeight
            );
        }

        drawEnergyBar(ENERGY_BAR, TextureCoords.ENERGY_BAR);

        drawPlayerInventory(guiLeft + PLAYER_INVENTORY.X, guiTop + PLAYER_INVENTORY.Y);

        drawInfoboxText(deltaTime, guiLeft + INFO_BOX.X, guiTop + INFO_BOX.Y);
        drawConsoleText(deltaTime, guiLeft + CONSOLE.X, guiTop + CONSOLE.Y);
    }

    private void drawInfoboxText(float advanceAmount, int left, int top) {
        List<String> strings = new ArrayList<>();

        if (dataModelError == DataModelError.NONE) {
            String tier = I18n.format("deepmoblearning.simulation_chamber.data_model_info.tier");
            String iterations = I18n.format("deepmoblearning.simulation_chamber.data_model_info.iterations");
            String pristine = I18n.format("deepmoblearning.simulation_chamber.data_model_info.pristine");
            strings.add(tier + ": " + DataModelHelper.getTierDisplayNameFormatted(dataModel));
            strings.add(iterations + ": " + DataModelHelper.getTotalSimulationCount(dataModel));
            strings.add(pristine + ": " + DataModelHelper.getPristineChance(dataModel));
        } else {
            dataModelErrorAnimator.advance(advanceAmount);
            strings = dataModelErrorAnimator.getCurrentStrings();
        }

        drawStrings(strings, left, top);
    }

    private void drawConsoleText(float advanceAmount, int left, int top) {
        List<String> strings;

        if (dataModelError != DataModelError.NONE) {
            emptyDisplayAnimator.advance(advanceAmount);
            strings = emptyDisplayAnimator.getCurrentStrings();
        } else if (redstoneDeactivated) {
            strings = new ArrayList<>();
            strings.add(TextFormatting.RED + StringHelper.getDashedLine(REDSTONE_DEACTIVATED_LINE_LENGTH) + TextFormatting.RESET);
            strings.add(TextFormatting.RED + StringHelper.pad(I18n.format("deepmoblearning.simulation_chamber.redstone_deactivated_1"), REDSTONE_DEACTIVATED_LINE_LENGTH) + TextFormatting.RESET);
            strings.add(TextFormatting.RED + StringHelper.pad(I18n.format("deepmoblearning.simulation_chamber.redstone_deactivated_2"), REDSTONE_DEACTIVATED_LINE_LENGTH) + TextFormatting.RESET);
            strings.add(TextFormatting.RED + StringHelper.getDashedLine(REDSTONE_DEACTIVATED_LINE_LENGTH) + TextFormatting.RESET);
        } else if (simulationError != SimulationError.NONE) {
            simulationErrorAnimator.advance(advanceAmount);
            strings = simulationErrorAnimator.getCurrentStrings();
        } else {
            float relativeProgress = tileEntity.getRelativeCraftingProgress();
            progressAnimator.goToRelativePosition(relativeProgress);
            strings = progressAnimator.getCurrentStrings();
        }

        drawStrings(strings, left, top);
    }

    private void drawStrings(List<String> strings, int left, int top) {
        for (int i = 0; i < strings.size(); i++) {
            drawString(fontRenderer, strings.get(i), left, top + i * ROW_SPACING, Colors.WHITE);
        }
    }

    //
    // STRING ANIMATORS
    //

    private void prepareStringAnimators() {
        String blinkingCursor = " _"; // Space so this looks like it's blinking

        String simulationLaunching = I18n.format("deepmoblearning.simulation_chamber.simulation_text.launching");
        String simulationLoading = I18n.format("deepmoblearning.simulation_chamber.simulation_text.loading");
        String simulationAssessing = I18n.format("deepmoblearning.simulation_chamber.simulation_text.assessing");
        String simulationEngaged = I18n.format("deepmoblearning.simulation_chamber.simulation_text.engaged");
        String simulationProcessing = I18n.format("deepmoblearning.simulation_chamber.simulation_text.processing") + " . . . . ."; // Padding so this line is displayed a little longer
        String error = I18n.format("deepmoblearning.simulation_chamber.error_text.error");

        progressAnimator.addString(AnimatedString.SIMULATION_LAUNCHING, simulationLaunching);
        progressAnimator.addString(AnimatedString.SIMULATION_ITERATION, ""); // gets set in update method
        progressAnimator.addString(AnimatedString.SIMULATION_LOADING, simulationLoading);
        progressAnimator.addString(AnimatedString.SIMULATION_ASSESSING, simulationAssessing);
        progressAnimator.addString(AnimatedString.SIMULATION_ENGAGED, simulationEngaged);
        progressAnimator.addString(AnimatedString.SIMULATION_PRISTINE, ""); // gets set in update method
        progressAnimator.addString(AnimatedString.SIMULATION_PROCESSING, simulationProcessing);

        emptyDisplayAnimator.addString(AnimatedString.UNDERLINE, blinkingCursor, BLINKING_CURSOR_SPEED, true);

        dataModelErrorAnimator.addString(AnimatedString.ERROR_DATA_MODEL_HEADING, error);
        dataModelErrorAnimator.addString(AnimatedString.ERROR_DATA_MODEL_TEXT_1, ""); // gets set in update method
        dataModelErrorAnimator.addString(AnimatedString.ERROR_DATA_MODEL_TEXT_2, ""); // gets set in update method

        simulationErrorAnimator.addString(AnimatedString.ERROR_SIMULATION_HEADING, error);
        simulationErrorAnimator.addString(AnimatedString.ERROR_SIMULATION_TEXT, ""); // gets set in update method
        simulationErrorAnimator.addString(AnimatedString.UNDERLINE, blinkingCursor, BLINKING_CURSOR_SPEED, true);
    }

    public enum AnimatedString {
        UNDERLINE,
        SIMULATION_LAUNCHING,
        SIMULATION_ITERATION,
        SIMULATION_LOADING,
        SIMULATION_ASSESSING,
        SIMULATION_ENGAGED,
        SIMULATION_PRISTINE,
        SIMULATION_PROCESSING,
        ERROR_DATA_MODEL_HEADING,
        ERROR_DATA_MODEL_TEXT_1,
        ERROR_DATA_MODEL_TEXT_2,
        ERROR_SIMULATION_HEADING,
        ERROR_SIMULATION_TEXT
    }

    private enum DataModelError {
        NONE,
        MISSING,
        FAULTY
    }

    private enum SimulationError {
        NONE,
        ENERGY,
        POLYMER,
        OUTPUT
    }
}
