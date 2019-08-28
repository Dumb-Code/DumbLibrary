package net.dumbcode.dumblibrary.server.ecs.component;

import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;

public interface FinalizableComponent {
    void finalizeComponent(ComponentAccess entity);
}
