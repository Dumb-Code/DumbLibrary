package net.dumbcode.dumblibrary.server.ecs.component.additionals;

import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;

public interface CanBreedComponent {
    boolean canBreedWith(ComponentAccess otherEntity);
}
