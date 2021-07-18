package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.dumbcode.dumblibrary.client.component.RenderComponentContext;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.FinalizableComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderCallbackComponent;

import java.util.List;

public class ComponentRenderContext extends EntityComponent implements FinalizableComponent {

    @Getter
    private RenderComponentContext context;

    @Override
    public void finalizeComponent(ComponentAccess entity) {
        List<RenderCallbackComponent.SubCallback> pre = Lists.newArrayList();
        List<RenderCallbackComponent.MainCallback> now = Lists.newArrayList();
        List<RenderCallbackComponent.SubCallback> post = Lists.newArrayList();

        for (EntityComponent component : entity.getAllComponents()) {
            if(component instanceof RenderCallbackComponent) {
                ((RenderCallbackComponent) component).addCallbacks(pre, now, post);
            }
        }

        this.context = new RenderComponentContext(pre, now, post);
    }
}
