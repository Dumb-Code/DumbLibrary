package net.dumbcode.dumblibrary.server.ecs.component.additionals;

import java.util.function.Consumer;

public interface RenderLayerComponent {
    void gatherLayers(Consumer<Consumer<Runnable>> registry);
}
