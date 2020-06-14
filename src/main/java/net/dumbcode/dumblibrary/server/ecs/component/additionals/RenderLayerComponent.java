package net.dumbcode.dumblibrary.server.ecs.component.additionals;

import lombok.Value;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.utils.IndexedObject;
import net.minecraft.util.ResourceLocation;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface RenderLayerComponent {
    void gatherLayers(ComponentAccess entity, Consumer<IndexedObject<Supplier<Layer>>> registry);

    @Value
    class Layer {
        float red;
        float green;
        float blue;
        float alpha;
        ResourceLocation texture;
    }
}
