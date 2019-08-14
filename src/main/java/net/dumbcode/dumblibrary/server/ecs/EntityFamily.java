package net.dumbcode.dumblibrary.server.ecs;

import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentType;

import java.lang.reflect.Array;
import java.util.function.Function;

public class EntityFamily<E> {
    private final E[] matchedEntities;

    private final Function<E, ComponentAccess> typeToAccess;

    public EntityFamily(E[] matchedEntities, Function<E, ComponentAccess> typeToAccess) {
        this.matchedEntities = matchedEntities;
        this.typeToAccess = typeToAccess;
    }

    public E[] getEntities() {
        return this.matchedEntities;
    }

    public <T extends EntityComponent> T[] populateBuffer(EntityComponentType<T, ?> type) {
        return this.populateBuffer(type, null);
    }

    @SuppressWarnings("unchecked")
    public <T extends EntityComponent> T[] populateBuffer(EntityComponentType<T, ?> type, T[] buffer) {
        if (buffer == null || buffer.length != this.matchedEntities.length) {
            buffer = (T[]) Array.newInstance(type.getType(), this.matchedEntities.length);
        }
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = this.typeToAccess.apply(this.matchedEntities[i]).getOrNull(type);
        }
        return buffer;
    }
}
