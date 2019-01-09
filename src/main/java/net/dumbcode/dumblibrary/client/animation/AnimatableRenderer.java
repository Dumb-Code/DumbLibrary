package net.dumbcode.dumblibrary.client.animation;

import lombok.val;
import net.dumbcode.dumblibrary.server.info.AnimationSystemInfo;
import net.ilexiconn.llibrary.server.animation.IAnimatedEntity;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * The Renderer class to use for the animations to work.
 * @param <T> The entity class
 */
public class AnimatableRenderer<T extends EntityLiving & IAnimatedEntity, N extends Enum<N>> extends RenderLiving<T> {

    private final Function<T, AnimationSystemInfo<N, T>> animationSystemInfoGetter;

    /**
     * @param renderManagerIn The RenderManager
     */
    public AnimatableRenderer(RenderManager renderManagerIn, Function<T, AnimationSystemInfo<N, T>> animationSystemInfoGetter) {
        super(renderManagerIn, null, 1f);
        this.animationSystemInfoGetter = animationSystemInfoGetter;
    }

    @Override
    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
        val info = this.animationSystemInfoGetter.apply(entity);
        this.mainModel = info.getModelContainer(entity).getModelMap().get(info.getStageFromEntity(entity));
        if(this.mainModel == null) {
            this.mainModel = ModelMissing.INSTANCE;
        }
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(T entity) {
        return this.animationSystemInfoGetter.apply(entity).getTexture(entity);
    }
}
