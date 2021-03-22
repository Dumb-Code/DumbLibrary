package net.dumbcode.dumblibrary.client.component;

import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderCallbackComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLocationComponent;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Map;

public class ComponentRenderer<E extends Entity & ComponentAccess> extends EntityRenderer<E> {

    private final Map<E, RenderComponentContext> contextMap = new MapMaker().weakKeys().makeMap(); //We use MapMaker so we can have weak keys, with identity checks rather than #hashCode checks

    protected ComponentRenderer(EntityRendererManager manager) {
        super(manager);
    }

    @Override
    public void render(E entity, float entityYaw, float partialTicks, MatrixStack stack, IRenderTypeBuffer buffer, int light) {
        RenderComponentContext context = this.contextMap.computeIfAbsent(entity, this::getContext);

        for (RenderCallbackComponent.MainCallback callback : context.getRenderCallbacks()) {
            callback.invoke(context, entity, entityYaw, partialTicks, stack, buffer, light, context.getPreRenderCallbacks(), context.getPostRenderCallback());
        }

    }

    private RenderComponentContext getContext(E entity) {
        List<RenderCallbackComponent.SubCallback> pre = Lists.newArrayList();
        List<RenderCallbackComponent.MainCallback> now = Lists.newArrayList();
        List<RenderCallbackComponent.SubCallback> post = Lists.newArrayList();

        RenderLocationComponent.ConfigurableLocation textureLocation = new RenderLocationComponent.ConfigurableLocation(".png");
        RenderLocationComponent.ConfigurableLocation fileLocation = new RenderLocationComponent.ConfigurableLocation();

        for (EntityComponent component : entity.getAllComponents()) {
            if(component instanceof RenderCallbackComponent) {
                ((RenderCallbackComponent) component).addCallbacks(pre, now, post);
            }

            if(component instanceof RenderLocationComponent) {
                ((RenderLocationComponent) component).editLocations(textureLocation, fileLocation);
            }
        }

        return new RenderComponentContext(pre, now, post, textureLocation, fileLocation);
    }

    @Override
    public ResourceLocation getTextureLocation(E p_110775_1_) {
        throw new UnsupportedOperationException("Not able to getEntityTexture on delegate class");
    }
}
