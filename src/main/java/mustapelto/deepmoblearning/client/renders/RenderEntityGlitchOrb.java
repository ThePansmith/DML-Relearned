package mustapelto.deepmoblearning.client.renders;

import mustapelto.deepmoblearning.common.DMLRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class RenderEntityGlitchOrb<T extends Entity> extends Render<T> {

    private final Minecraft minecraft;

    public RenderEntityGlitchOrb(RenderManager manager) {
        super(manager);
        minecraft = Minecraft.getMinecraft();
    }

    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
        renderProjectile(x, y, z, 4.0F, new ItemStack(DMLRegistry.ITEM_GLITCH_HEART));
        super.doRender(entity, x * 1.0F, y * 1.0F, z * 1.0F, entityYaw, partialTicks);
    }

    public void renderProjectile(double x, double y, double z, float scale, ItemStack renderStack) {
        GlStateManager.pushMatrix();

        GlStateManager.translate((float) x, (float) y, (float) z);
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.enableRescaleNormal();
        GlStateManager.rotate(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((float) (renderManager.options.thirdPersonView == 2 ? -1 : 1) * renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(180.0F, 0.0F, 3.0F, 0.0F);
        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        RenderHelper.enableGUIStandardItemLighting();
        minecraft.getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GROUND);

        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}
