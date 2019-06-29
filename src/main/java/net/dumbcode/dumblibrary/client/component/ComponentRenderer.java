package net.dumbcode.dumblibrary.client.component;

import com.google.common.collect.Lists;
import net.dumbcode.dumblibrary.server.entity.ComponentAccess;
import net.dumbcode.dumblibrary.server.entity.component.EntityComponent;
import net.dumbcode.dumblibrary.server.entity.component.additionals.RenderCallbackComponent;
import net.dumbcode.dumblibrary.server.entity.component.additionals.RenderLocationComponent;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;
import java.util.WeakHashMap;

public class ComponentRenderer<E extends Entity & ComponentAccess> extends Render<E> {

    private final WeakHashMap<E, RenderComponentContext> contextMap = new WeakHashMap<>();

    public ComponentRenderer(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(E entity, double x, double y, double z, float entityYaw, float partialTicks) {
        RenderComponentContext context = this.contextMap.computeIfAbsent(entity, this::getContext);

//        GlStateManager.pushMatrix();

        for (RenderCallbackComponent.MainCallback callback : context.getRenderCallbacks()) {
            callback.invoke(context, entity, x, y, z, entityYaw, partialTicks, context.getPreRenderCallbacks(), context.getPostRenderCallback());
        }

//        GlStateManager.popMatrix();

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

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(E entity) {
        throw new UnsupportedOperationException("Not able to getEntityTexture on delegate class");
    }
}
