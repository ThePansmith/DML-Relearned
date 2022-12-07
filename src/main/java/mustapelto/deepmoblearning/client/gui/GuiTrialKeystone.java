package mustapelto.deepmoblearning.client.gui;

import mustapelto.deepmoblearning.DMLConstants;
import mustapelto.deepmoblearning.client.gui.buttons.ButtonBase;
import mustapelto.deepmoblearning.client.gui.buttons.ButtonItemDisplay;
import mustapelto.deepmoblearning.client.gui.buttons.ButtonStartTrial;
import mustapelto.deepmoblearning.common.network.DMLPacketHandler;
import mustapelto.deepmoblearning.common.network.MessageRequestUpdateTileEntity;
import mustapelto.deepmoblearning.common.tiles.TileEntityTrialKeystone;
import mustapelto.deepmoblearning.common.trials.AttunementData;
import mustapelto.deepmoblearning.common.util.Point;
import mustapelto.deepmoblearning.common.util.Rect;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static mustapelto.deepmoblearning.DMLConstants.Gui.ROW_SPACING;
import static mustapelto.deepmoblearning.DMLConstants.Gui.TrialKeystone.*;
import static mustapelto.deepmoblearning.DMLConstants.Gui.Colors;

public class GuiTrialKeystone extends GuiContainerBase {

    // TEXTURE
    private static final ResourceLocation TEXTURE = new ResourceLocation(DMLConstants.ModInfo.ID, "textures/gui/trial_keystone.png");
    private static final class TextureCoords {
        private static final Point MAIN_GUI = new Point(0, 0);
        private static final Point TRIAL_INFO_OVERLAY = new Point(18, 100);
        private static final Point TRIAL_KEY_SLOT = new Point(0, 100);
    }

    // DIMENSIONS
    private static final int WIDTH = 200;
    private static final int HEIGHT = 178;
    private static final Rect MAIN_GUI = new Rect(0, 0, 200, 100);

    // TRIAL INFO OVERLAY
    private static final Rect TRIAL_INFO_OVERLAY = new Rect(109, 0, 91, 100);

    // BUTTONS
    private static final Point START_TRIAL_BUTTON = new Point(114, 76);
    private static final Point REWARD_BUTTON_LIST = new Point(115, 48);
    private static final int REWARD_BUTTON_SIZE = 18;
    private static final int REWARD_BUTTON_ID_OFFSET = 20;

    // TEXT
    private static final Point ERROR_TEXT_CENTER = new Point(100, 50);
    private static final Point TRIAL_INFO_TEXT_LEFT = new Point(6, 6);
    private static final Point TRIAL_INFO_TEXT_RIGHT = new Point(115, 6);
    private static final int TIER_NAME_OFFSET_X = 25;
    private static final int WAVES_OFFSET_X = 35;

    // STATE VARIABLES
    private ButtonStartTrial startTrialButton;
    private final TileEntityTrialKeystone trialKeystone;
    @Nullable
    private AttunementData attunementData;
    private final List<ButtonItemDisplay> rewardItemDisplay = new ArrayList<>();
    private final List<String> errorStrings = new ArrayList<>();
    private boolean hasError;

    //
    // INIT
    //

    public GuiTrialKeystone(TileEntityTrialKeystone tileEntity, EntityPlayer player, World world) {
        super(player, world, tileEntity.getContainer(player.inventory), WIDTH, HEIGHT);
        this.trialKeystone = tileEntity;
    }

    @Override
    public void initGui() {
        super.initGui();
        DMLPacketHandler.sendToServer(new MessageRequestUpdateTileEntity(trialKeystone));
        attunementData = trialKeystone.getTrialData();
    }

    //
    // BUTTONS
    //

    @Override
    protected void initButtons() {
        super.initButtons();
        startTrialButton = new ButtonStartTrial(0, guiLeft + START_TRIAL_BUTTON.X, guiTop + START_TRIAL_BUTTON.Y);
    }

    @Override
    protected void rebuildButtonList() {
        super.rebuildButtonList();
        buttonList.add(startTrialButton);
        rebuildRewardButtons();
        buttonList.addAll(rewardItemDisplay);
    }

    @Override
    protected void handleButtonPress(ButtonBase button, int mouseButton) {
        if (button instanceof ButtonStartTrial) {
            trialKeystone.startTrial();
            DMLPacketHandler.sendToServer(new MessageRequestUpdateTileEntity(trialKeystone));
        }
    }

    private void rebuildRewardButtons() {
        rewardItemDisplay.clear();

        if (hasError || attunementData == null)
            return;

        List<ItemStack> rewards = attunementData.getRewards();

        for (int i = 0; i < rewards.size(); i++) {
            ItemStack reward = rewards.get(i);
            rewardItemDisplay.add(new ButtonItemDisplay(
                    REWARD_BUTTON_ID_OFFSET + i,
                    guiLeft + REWARD_BUTTON_LIST.X + i * REWARD_BUTTON_SIZE,
                    guiTop + REWARD_BUTTON_LIST.Y,
                    reward
            ));
        }
    }

    //
    // UPDATE
    //

    @Override
    public void updateScreen() {
        AttunementData oldAttunementData = attunementData;
        attunementData = trialKeystone.getTrialData();

        if (attunementData != oldAttunementData)
            buttonListNeedsRebuild = true;

        super.updateScreen();

        errorStrings.clear();
        hasError = true;
        if (trialKeystone.isTrialActive()) {
            errorStrings.add(I18n.format("deepmoblearning.trial_keystone.trial_active_1"));
            errorStrings.add(I18n.format("deepmoblearning.trial_keystone.trial_active_2"));
            errorStrings.add(I18n.format("deepmoblearning.trial_keystone.trial_active_3", trialKeystone.getCurrentWave() + 1, trialKeystone.getLastWave()));
        } else if (!trialKeystone.isTrialAreaClear()) {
            errorStrings.add(I18n.format("deepmoblearning.trial_keystone.area_blocked_1"));
            errorStrings.add(I18n.format("deepmoblearning.trial_keystone.area_blocked_2"));
            errorStrings.add(I18n.format("deepmoblearning.trial_keystone.area_blocked_3"));
            errorStrings.add(I18n.format("deepmoblearning.trial_keystone.area_blocked_4"));
            errorStrings.add(I18n.format("deepmoblearning.trial_keystone.area_blocked_5"));
        } else if (!trialKeystone.hasTrialKey()) {
            errorStrings.add(I18n.format("deepmoblearning.trial_keystone.no_trial_key_1"));
            errorStrings.add(I18n.format("deepmoblearning.trial_keystone.no_trial_key_2"));
        } else
            hasError = false;

        if (attunementData == null || hasError) {
            startTrialButton.visible = false;
            startTrialButton.enabled = false;
        } else {
            startTrialButton.visible = true;
            startTrialButton.enabled = true;
        }
    }

    //
    // DRAWING
    //


    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // Draw item stacks on buttons (can't be done in button draw method due to z order problems)
        RenderHelper.enableGUIStandardItemLighting();
        rewardItemDisplay.forEach(item -> drawItemStackWithOverlay(item.getStack(), item.x - guiLeft, item.y - guiTop));
        RenderHelper.disableStandardItemLighting();

        // Draw button tooltips (after items to ensure correct z order)
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        // Main GUI
        textureManager.bindTexture(TEXTURE);
        GlStateManager.color(1, 1, 1, 1);
        drawTexturedModalRect(
                guiLeft + MAIN_GUI.LEFT,
                guiTop + MAIN_GUI.TOP,
                TextureCoords.MAIN_GUI.X,
                TextureCoords.MAIN_GUI.Y,
                MAIN_GUI.WIDTH,
                MAIN_GUI.HEIGHT
        );

        // Trial Key Slot
        drawTexturedModalRect(
                guiLeft + TRIAL_KEY_SLOT.LEFT,
                guiTop + TRIAL_KEY_SLOT.TOP,
                TextureCoords.TRIAL_KEY_SLOT.X,
                TextureCoords.TRIAL_KEY_SLOT.Y,
                TRIAL_KEY_SLOT.WIDTH,
                TRIAL_KEY_SLOT.HEIGHT
        );

        drawPlayerInventory(guiLeft + PLAYER_INVENTORY.X, guiTop + PLAYER_INVENTORY.Y);

        // Error text (if any)
        if (hasError) {
            int lines = errorStrings.size();
            int y = guiTop + ERROR_TEXT_CENTER.Y - (lines * ROW_SPACING / 2);
            for (String errorString : errorStrings) {
                drawCenteredString(
                        fontRenderer,
                        errorString,
                        guiLeft + ERROR_TEXT_CENTER.X,
                        y,
                        Colors.WHITE
                );

                y += ROW_SPACING;
            }
            return;
        }

        if (attunementData == null)
            return;

        // Trial Info Overlay
        drawTexturedModalRect(
                guiLeft + TRIAL_INFO_OVERLAY.LEFT,
                guiTop + TRIAL_INFO_OVERLAY.TOP,
                TextureCoords.TRIAL_INFO_OVERLAY.X,
                TextureCoords.TRIAL_INFO_OVERLAY.Y,
                TRIAL_INFO_OVERLAY.WIDTH,
                TRIAL_INFO_OVERLAY.HEIGHT
        );

        // Trial Info Text
        drawString(
                fontRenderer,
                I18n.format("deepmoblearning.trial_keystone.trial_type"),
                guiLeft + TRIAL_INFO_TEXT_LEFT.X,
                guiTop + TRIAL_INFO_TEXT_LEFT.Y,
                Colors.AQUA
        );

        drawString(
                fontRenderer,
                attunementData.getMobDisplayName(),
                guiLeft + TRIAL_INFO_TEXT_LEFT.X,
                guiTop + TRIAL_INFO_TEXT_LEFT.Y + ROW_SPACING,
                Colors.WHITE
        );

        //TODO: Affixes

        drawString(
                fontRenderer,
                I18n.format("deepmoblearning.trial_keystone.tier"),
                guiLeft + TRIAL_INFO_TEXT_RIGHT.X,
                guiTop + TRIAL_INFO_TEXT_RIGHT.Y,
                Colors.AQUA
        );

        drawString(
                fontRenderer,
                attunementData.getTierDisplayNameFormatted(),
                guiLeft + TRIAL_INFO_TEXT_RIGHT.X + TIER_NAME_OFFSET_X,
                guiTop + TRIAL_INFO_TEXT_RIGHT.Y,
                Colors.WHITE
        );

        drawString(
                fontRenderer,
                I18n.format("deepmoblearning.trial_keystone.waves"),
                guiLeft + TRIAL_INFO_TEXT_RIGHT.X,
                guiTop + TRIAL_INFO_TEXT_RIGHT.Y + ROW_SPACING,
                Colors.AQUA
        );

        drawString(
                fontRenderer,
                String.valueOf(attunementData.getMaxWave()),
                guiLeft + TRIAL_INFO_TEXT_RIGHT.X + WAVES_OFFSET_X,
                guiTop + TRIAL_INFO_TEXT_RIGHT.Y + ROW_SPACING,
                Colors.WHITE
        );

        drawString(
                fontRenderer,
                I18n.format("deepmoblearning.trial_keystone.rewards"),
                guiLeft + TRIAL_INFO_TEXT_RIGHT.X,
                guiTop + TRIAL_INFO_TEXT_RIGHT.Y + (int)(2.5 * ROW_SPACING),
                Colors.AQUA
        );
    }
}
