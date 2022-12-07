package mustapelto.deepmoblearning.client.renders;

import mustapelto.deepmoblearning.DMLConstants;
import mustapelto.deepmoblearning.client.models.ModelGlitch;
import mustapelto.deepmoblearning.common.entities.EntityGlitch;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class RenderEntityGlitch extends RenderLiving<EntityGlitch> {

    private final ResourceLocation TEXTURE = new ResourceLocation(DMLConstants.ModInfo.ID, "textures/entity/glitch.png");

    public RenderEntityGlitch(RenderManager manager) {
        super(manager, new ModelGlitch(), 0.5F);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityGlitch glitch) {
        return TEXTURE;
    }

    // override for type-coercion
    @Override
    public void doRender(EntityGlitch entity, double x, double y, double z, float entityYaw, float partialTicks) {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }
}
