package mustapelto.deepmoblearning.client;

import mustapelto.deepmoblearning.client.gui.GuiDeepLearnerOverlay;
import mustapelto.deepmoblearning.client.gui.GuiTrialOverlay;
import mustapelto.deepmoblearning.client.particles.ParticleGlitch;
import mustapelto.deepmoblearning.client.particles.ParticleScalableSmoke;
import mustapelto.deepmoblearning.client.renders.RenderEntityGlitch;
import mustapelto.deepmoblearning.client.renders.RenderEntityGlitchOrb;
import mustapelto.deepmoblearning.client.renders.TESRTrialKeystone;
import mustapelto.deepmoblearning.common.ServerProxy;
import mustapelto.deepmoblearning.common.capability.CapabilityPlayerTrialProvider;
import mustapelto.deepmoblearning.common.capability.ICapabilityPlayerTrial;
import mustapelto.deepmoblearning.common.entities.*;
import mustapelto.deepmoblearning.common.tiles.TileEntityTrialKeystone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.entity.RenderCaveSpider;
import net.minecraft.client.renderer.entity.RenderEnderman;
import net.minecraft.client.renderer.entity.RenderSlime;
import net.minecraft.client.renderer.entity.RenderSpider;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import javax.annotation.Nullable;
import java.util.concurrent.ThreadLocalRandom;

public class ClientProxy extends ServerProxy {

    @Override
    public void registerEntityRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(EntityGlitch.class, RenderEntityGlitch::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityGlitchOrb.class, RenderEntityGlitchOrb::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityTrialEnderman.class, RenderEnderman::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityTrialSpider.class, RenderSpider::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityTrialCaveSpider.class, RenderCaveSpider::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityTrialSlime.class, RenderSlime::new);
    }

    @Override
    public void registerGuiRenderers() {
        MinecraftForge.EVENT_BUS.register(GuiDeepLearnerOverlay.INSTANCE);
        MinecraftForge.EVENT_BUS.register(GuiTrialOverlay.INSTANCE);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTrialKeystone.class, new TESRTrialKeystone());
    }

    public void spawnSmokeParticle(World world, double x, double y, double z, double mx, double my, double mz, SmokeType type) {
        float scale = 1.0f;

        switch (type) {
            case CYAN:
            case MIXED:
                scale = 1.4f;
                break;
            case SMOKE:
                scale = 1.6f;
        }

        Particle particle = new ParticleScalableSmoke(world, x, y, z, mx, my, mz, scale);

        switch (type) {
            case CYAN:
                setColorCyan(particle);
                break;
            case MIXED:
                setColorMixed(particle);
                break;
            case SMOKE:
                setColorSmoke(particle);
                break;
        }

        Minecraft.getMinecraft().effectRenderer.addEffect(particle);
    }

    @Override
    public void spawnGlitchParticle(World world, double x, double y, double z, double mx, double my, double mz) {
        Particle particle = new ParticleGlitch(world, x, y, z, mx, my, mz);
        Minecraft.getMinecraft().effectRenderer.addEffect(particle);
    }

    private void setColorMixed(Particle particle) {
        boolean spawnCyan = ThreadLocalRandom.current().nextInt(0, 3) == 0; // p = 1/3

        if (spawnCyan)
            setColorCyan(particle);
        else
            setColorGray(particle);
    }

    private void setColorSmoke(Particle particle) {
        boolean spawnRed = ThreadLocalRandom.current().nextInt(0, 3) == 0; // p = 1/3
        boolean spawnBlack = ThreadLocalRandom.current().nextInt(0, 4) == 0; // p = 1/4

        if (spawnBlack)
            particle.setRBGColorF(0.02f, 0.02f, 0.02f);
        else if (spawnRed)
            particle.setRBGColorF(0.29f, 0.05f, 0.01f);
        else
            setColorGray(particle);

    }

    private void setColorCyan(Particle particle) {
        particle.setRBGColorF(0.0f, 1.0f, 0.75f);
    }

    private void setColorGray(Particle particle) {
        particle.setRBGColorF(0.09f, 0.09f, 0.09f);
    }

    @Override
    public String getLocalizedString(String key, Object... args) {
        return I18n.format(key, args);
    }

    @Nullable
    @Override
    @SuppressWarnings("ConstantConditions")
    public ICapabilityPlayerTrial getClientPlayerTrialCapability() {
        return FMLClientHandler.instance().getClientPlayerEntity().getCapability(CapabilityPlayerTrialProvider.PLAYER_TRIAL_CAP, null);
    }
}
