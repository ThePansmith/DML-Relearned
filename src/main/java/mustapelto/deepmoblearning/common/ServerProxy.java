package mustapelto.deepmoblearning.common;

import mustapelto.deepmoblearning.common.capability.ICapabilityPlayerTrial;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ServerProxy {
    public enum SmokeType { SMOKE, MIXED, CYAN }

    // Client-only methods
    public void registerGuiRenderers() {}
    public void registerEntityRenderers() {}
    public void spawnSmokeParticle(World world, double x, double y, double z, double mx, double my, double mz, SmokeType type) {}
    public void spawnGlitchParticle(World world, double x, double y, double z, double mx, double my, double mz) {}

    @SuppressWarnings("deprecation")
    public String getLocalizedString(String key, Object... args) {
        return I18n.translateToLocalFormatted(key, args);
    }

    @Nullable
    public ICapabilityPlayerTrial getClientPlayerTrialCapability() {
        return null;
    }
}
