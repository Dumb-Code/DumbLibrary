package net.dumbcode.dumblibrary.server.entity.component;

import net.minecraft.entity.Entity;

public interface FinalizableComponent extends EntityComponent {
    void finalizeComponent(Entity entity);
}
