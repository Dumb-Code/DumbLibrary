package net.dumbcode.dumblibrary.server.ecs.component.additionals;

import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;

import java.util.function.Consumer;

public interface RenderLayerComponent {
    void gatherLayers(ComponentAccess entity, Consumer<Consumer<Runnable>> registry);
}
