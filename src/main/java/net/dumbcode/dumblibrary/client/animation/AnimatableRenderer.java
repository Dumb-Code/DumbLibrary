package net.dumbcode.dumblibrary.client.animation;

import net.dumbcode.dumblibrary.server.entity.EntityAnimatable;
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
public class AnimatableRenderer<T extends EntityLiving & EntityAnimatable> extends RenderLiving<T> {

    private final Function<T, ModelContainer> modelContainerGetter;
    private final Function<T, ResourceLocation> textureGetter;

    /**
     * @param renderManagerIn The RenderManager
     * @param modelContainerGetter A function to get the tabula model from the entity + growth stage.
     * @param textureGetter A function to get the texture that should be used to render the dinosaur with
     */
    public AnimatableRenderer(RenderManager renderManagerIn, Function<T, ModelContainer> modelContainerGetter, Function<T, ResourceLocation> textureGetter) {
        super(renderManagerIn, null, 1f);
        this.modelContainerGetter = modelContainerGetter;
        this.textureGetter = textureGetter;
    }

    @Override
    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
        this.mainModel = this.modelContainerGetter.apply(entity).getModelMap().get(entity.getGrowthStage());
        if(this.mainModel == null) {
            this.mainModel = ModelMissing.INSTANCE;
        }
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(T entity) {
        return textureGetter.apply(entity);
    }
}

