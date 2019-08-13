package net.dumbcode.dumblibrary.server.ecs;

import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentMap;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

/**
 * This is a default implementation of {@link ComponentWriteAccess} and {@link ComponentAccess} that all uses a getter to
 * a {@link EntityComponentMap} ({@link #getComponentMap()})
 * @author Wyn Price
 */
public interface ComponentMapWriteAccess extends ComponentWriteAccess {

    @Override
    default <T extends EntityComponent> void attachComponent(EntityComponentType<T, ?> type, T component) {
        if(component == null) {
            throw new NullPointerException("Component on type " + type.getIdentifier() + " is null.");
        }
        this.getComponentMap().put(type, component);
    }

    @Nullable
    @Override
    default <T extends EntityComponent, S extends EntityComponentStorage<T>> T getOrNull(EntityComponentType<T, S> type) {
        return this.getComponentMap().getNullable(type);
    }

    @Nonnull
    @Override
    default Collection<EntityComponent> getAllComponents() {
        return this.getComponentMap().values();
    }

    @Override
    default boolean contains(EntityComponentType<?, ?> type) {
        return this.getComponentMap().containsKey(type);
    }

    /**
     * Get's the component map for this object. This should be persistent throughout this objects life.
     * @return the unchanging component map.
     */
    EntityComponentMap getComponentMap();

}
