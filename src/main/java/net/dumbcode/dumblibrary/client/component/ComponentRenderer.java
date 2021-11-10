package net.dumbcode.dumblibrary.client.component;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderCallbackComponent;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class ComponentRenderer<E extends Entity & ComponentAccess> extends EntityRenderer<E> {

    public ComponentRenderer(EntityRendererManager manager) {
        super(manager);
    }

    @Override
    public void render(E entity, float entityYaw, float partialTicks, MatrixStack stack, IRenderTypeBuffer buffer, int light) {
        entity.get(EntityComponentTypes.RENDER_CONTEXT.get()).ifPresent(componentRenderContext -> {
            RenderComponentContext context = componentRenderContext.getContext();
            for (RenderCallbackComponent.MainCallback callback : context.getRenderCallbacks()) {
                callback.invoke(context, entity, entityYaw, partialTicks, stack, buffer, light, context.getPreRenderCallbacks(), context.getPostRenderCallback());
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(E p_110775_1_) {
        throw new UnsupportedOperationException("Not able to getEntityTexture on delegate class");
    }
}
