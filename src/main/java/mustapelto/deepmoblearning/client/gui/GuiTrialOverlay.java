package mustapelto.deepmoblearning.client.gui;

import mustapelto.deepmoblearning.DMLConstants;
import mustapelto.deepmoblearning.DMLRelearned;
import mustapelto.deepmoblearning.common.capability.CapabilityPlayerTrial;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import static mustapelto.deepmoblearning.DMLConstants.Gui.Colors;
import static mustapelto.deepmoblearning.DMLConstants.Trials.Message;

// TODO Convert stuff to constants and rewrite this
@EventBusSubscriber(Side.CLIENT)
public class GuiTrialOverlay extends GuiScreen {

    public static GuiTrialOverlay INSTANCE = new GuiTrialOverlay();
    private static final ResourceLocation TEXTURE = new ResourceLocation(DMLConstants.ModInfo.ID, "textures/gui/trial_overlay.png");

    private final FontRenderer fontRender;
    private final Minecraft mc;

    private static CapabilityPlayerTrial clientCapability;

    private long lastTick = 0;
    private static String lastMessage = "";
    private static int ticksToRender = 0;
    private static int ticksToRenderGlitchNotification = 0;
    private static int ticksToRenderNextWaveMessage = 0;

    private GuiTrialOverlay() {
        mc = Minecraft.getMinecraft();
        fontRender = mc.fontRenderer;
        this.itemRender = mc.getRenderItem();
        setGuiSize(89, 12);
    }

    public static void initPlayerCapability() {
        clientCapability = (CapabilityPlayerTrial) DMLRelearned.proxy.getClientPlayerTrialCapability();
    }

    public static void handleMessage(String type) {
        initPlayerCapability();

        System.out.println("Message received: " + type);

        switch (type) {
            case Message.GLITCH_NOTIF:
                ticksToRenderGlitchNotification = 80;
                break;
            case Message.TRIAL_ABORT:
                ticksToRender = 80;
                lastMessage = I18n.format("deepmoblearning.trial.message.aborted");
                break;
            case Message.TRIAL_COMPLETE:
                ticksToRender = 120;
                lastMessage = I18n.format("deepmoblearning.trial.message.completed");
                break;
            case Message.WAVE_NUMBER:
                ticksToRender = 80;
                lastMessage = I18n.format("deepmoblearning.trial.message.wave_number", clientCapability.getCurrentWave() + 1);
                break;
            // Subtract 2 from ticks so it does not overlap with WaveNumber
            case Message.WAVE_COUNTDOWN:
                ticksToRenderNextWaveMessage = 118;
                break;
        }
    }

    /* Needed on 1.12 to render tooltips */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @SubscribeEvent(priority=EventPriority.NORMAL)
    public void renderOverlay(RenderGameOverlayEvent.Post event) {
        doTickChecks();

        if (!mc.inGameHasFocus || event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE) {
            return;
        }

        if (ticksToRender > 0) renderMessage();
        if (ticksToRenderGlitchNotification > 0) renderGlitchNotification();
        if (ticksToRenderNextWaveMessage > 0) renderWaveCountdown();

        if (clientCapability == null) {
            initPlayerCapability();
        }

        if (clientCapability.isTrialActive()) {
            renderTrialScoreboard();
        }
    }

    private void doTickChecks() {
        if (lastTick != mc.world.getTotalWorldTime()) {
            lastTick = mc.world.getTotalWorldTime();

            if (ticksToRender > 0) ticksToRender--;
            if (ticksToRenderNextWaveMessage > 0) ticksToRenderNextWaveMessage--;
            if (ticksToRenderGlitchNotification > 0) ticksToRenderGlitchNotification--;
        }
    }

    private void renderWaveCountdown() {
        float scale = 1.6F;

        if ((ticksToRenderNextWaveMessage / 20) > 0) {
            String nextWave = I18n.format("deepmoblearning.trial.message.next_wave", ticksToRenderNextWaveMessage / 20);
            int x = getScreenCenterX(scale) - getHalfLineWidth(nextWave, scale);
            renderScaledString(scale, x, 80, nextWave, Colors.WHITE);
        }
    }

    private void renderMessage() {
        float scale = lastMessage.length() < 8 ? 2.2F : 1.6F;
        int x = getScreenCenterX(scale) - getHalfLineWidth(lastMessage, scale);

        renderScaledString(scale, x, 80, lastMessage, Colors.WHITE);
    }

    private void renderGlitchNotification() {
        float scale1 = 1.9F;
        float scale2 = 1.2F;

        String notif1 = I18n.format("deepmoblearning.trial.message.glitch_1");
        String notif2 = I18n.format("deepmoblearning.trial.message.glitch_2");
        int x1 = getScreenCenterX(scale1) - getHalfLineWidth(notif1, scale1);
        int x2 = getScreenCenterX(scale2) - getHalfLineWidth(notif2, scale2);
        int width = fontRender.getStringWidth(notif1);

        // Draw the Glitch faces
        mc.getTextureManager().bindTexture(TEXTURE);
        drawTexturedModalRect(getScreenCenterX() - width - 22, 108, 0, 0, 17, 17);
        drawTexturedModalRect(getScreenCenterX() + width + 4, 108, 0, 0, 17, 17);

        renderScaledString(scale1, x1, 110, notif1, Colors.BRIGHT_PURPLE);
        renderScaledString(scale2, x2, 130, notif2, Colors.WHITE);
    }

    private void renderTrialScoreboard() {
        int x = getRightCornerX() - 50;
        float scale = 1.3F;
        int scaledX = (int) (x / scale);
        int y = 145;

        String wave = I18n.format("deepmoblearning.trial.message.wave");
        String opponents = I18n.format("deepmoblearning.trial.message.opponents");
        String waveCleared = I18n.format("deepmoblearning.trial.message.wave_cleared");
        int toKill = clientCapability.getWaveMobTotal() - clientCapability.getDefated();
        String enemiesLeft = I18n.format("deepmoblearning.trial.message.enemies_left", toKill);

        drawItemStack(x - 21, y + 4, new ItemStack(Items.CLOCK));
        renderScaledString(scale, scaledX, y + 2, wave,  Colors.BRIGHT_LIME);
        drawString(fontRender, (clientCapability.getCurrentWave() + 1) + "/" + clientCapability.getLastWave(), x + 1, y + 16, Colors.WHITE);

        ItemStack skull = new ItemStack(Items.SKULL);
        skull.setItemDamage(0);

        drawItemStack(x - 21, y + 36, skull);
        renderScaledString(scale, scaledX, y + 32, opponents, Colors.BRIGHT_LIME);


        if (toKill <= 0) {
            drawString(fontRender, waveCleared, x + 1, y + 46, Colors.WHITE);
        } else {
            drawString(fontRender, enemiesLeft, x + 1, y + 46, Colors.WHITE);
        }
    }


    private void renderScaledString(float scale, int x, int y, String text, int color) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        drawString(fontRender, text, x, (int) (y / scale), color);
        GlStateManager.scale(1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    private int getHalfLineWidth(String text, float glScale) {
        return (int) (((fontRender.getStringWidth(text) / 2) * glScale) / glScale);
    }

    private int getScreenCenterX() {
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        return scaledResolution.getScaledWidth() / 2;
    }

    private int getScreenCenterX(float glScale) {
        return (int) (getScreenCenterX() / glScale);
    }

    private int getRightCornerX() {
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        return scaledResolution.getScaledWidth() - width - 5;
    }

    private void drawItemStack(int x, int y, ItemStack stack) {
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.translate(0.0F, 0.0F, 32.0F);
        this.zLevel = 200.0F;
        itemRender.zLevel = 200.0F;
        itemRender.renderItemAndEffectIntoGUI(stack, x, y);
        itemRender.renderItemOverlayIntoGUI(fontRender, stack, x - 1, y - 1, "");
        this.zLevel = 0.0F;
        itemRender.zLevel = 0.0F;
        RenderHelper.disableStandardItemLighting();
    }
}
