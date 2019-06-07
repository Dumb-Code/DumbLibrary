package net.dumbcode.dumblibrary.client.animation;

import lombok.val;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.client.model.ModelMissing;
import net.dumbcode.dumblibrary.server.info.AnimationSystemInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * The Renderer class to use for the animations to work.
 * @param <E> The entity class
 */
public class AnimatableRenderer<E extends EntityLiving, N extends IStringSerializable> extends RenderLiving<E> {

    private final Function<E, AnimationSystemInfo<N, E>> animationSystemInfoGetter;

    /**
     * @param renderManagerIn The RenderManager
     */
    public AnimatableRenderer(RenderManager renderManagerIn, Function<E, AnimationSystemInfo<N, E>> animationSystemInfoGetter) {
        super(renderManagerIn, ModelMissing.INSTANCE, 1f);
        this.animationSystemInfoGetter = animationSystemInfoGetter;
    }

    @Override
    public void doRender(E entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.disableAlpha();
        val info = this.animationSystemInfoGetter.apply(entity);
        val map = info.getModelContainer(entity).getModelMap();
        TabulaModel model = map.getOrDefault(info.getStageFromEntity(entity), map.get(info.defaultStage()));
        this.mainModel = model;
        if(this.mainModel == null) {
            this.mainModel = ModelMissing.INSTANCE;
        }
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(E entity) {
        return this.animationSystemInfoGetter.apply(entity).getTexture(entity);
    }
}
