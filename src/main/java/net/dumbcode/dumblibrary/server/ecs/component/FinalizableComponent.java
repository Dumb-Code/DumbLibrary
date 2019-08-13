package net.dumbcode.dumblibrary.server.ecs.component;

import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;

public interface FinalizableComponent extends EntityComponent {
    void finalizeComponent(ComponentAccess entity);
}
