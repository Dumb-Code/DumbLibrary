package net.dumbcode.dumblibrary.server.entity.component;

import net.dumbcode.dumblibrary.server.entity.ComponentAccess;

public interface FinalizableComponent extends EntityComponent {
    void finalizeComponent(ComponentAccess entity);
}
