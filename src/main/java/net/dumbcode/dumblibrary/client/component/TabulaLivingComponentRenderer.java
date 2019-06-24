package net.dumbcode.dumblibrary.client.component;

import net.dumbcode.dumblibrary.client.model.ModelMissing;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class TabulaLivingComponentRenderer <E extends EntityLiving> extends RenderLiving<E> {

    public TabulaLivingComponentRenderer(RenderManager renderManagerIn) {
        super(renderManagerIn, ModelMissing.INSTANCE, 1f);
    }

    @Override
    public void doRender(E entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.disableAlpha();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(E entity) {
        return null;
    }
}
