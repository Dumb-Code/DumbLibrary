package net.dumbcode.dumblibrary.server.ecs;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class GroupedComponentAccess implements ComponentAccess {

    private final ComponentAccess[] accesses;

    public GroupedComponentAccess(ComponentAccess[] accesses) {
        this.accesses = accesses;
    }

    @Nullable
    @Override
    public <T extends EntityComponent, S extends EntityComponentStorage<T>> T getOrNull(EntityComponentType<T, S> type) {
        List<T> collected = Arrays.stream(this.accesses)
                .map(componentAccess -> componentAccess.getOrNull(type))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if(collected.size() > 1) {
            DumbLibrary.getLogger().info("When requesting component type {}, {} entries were found. {}",
                    type.getIdentifier(), collected.size(), collected.toString());
        }
        return collected.isEmpty() ? null : collected.get(0);
    }

    @Nonnull
    @Override
    public Collection<EntityComponent> getAllComponents() {
        return Arrays.stream(this.accesses)
                .map(ComponentAccess::getAllComponents)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
    }
}
