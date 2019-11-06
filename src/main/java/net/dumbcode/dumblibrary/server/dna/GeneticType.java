package net.dumbcode.dumblibrary.server.dna;

import lombok.Builder;
import lombok.Getter;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentType;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@Getter
public class GeneticType<T extends GeneticFactoryStorage> extends IForgeRegistryEntry.Impl<GeneticType<?>> {
    private final GeneticValueApplier<T, ComponentAccess> onChange;
    @Nullable private final Supplier<T> storage;

    GeneticType(GeneticValueApplier<T, ComponentAccess> onChange, Supplier<T> storage) {
        this.onChange = onChange;
        this.storage = storage;
    }

    @SuppressWarnings("unchecked")
    public static Class<GeneticType<?>> getWildcardType() {
        return (Class<GeneticType<?>>) (Class<?>) GeneticType.class;
    }

    public static <T extends GeneticFactoryStorage> GeneticTypeBuilder<T> builder() {
        return new GeneticTypeBuilder<>();
    }

    public static class GeneticTypeBuilder<T extends GeneticFactoryStorage> {
        private GeneticValueApplier<T, ComponentAccess> onChange;
        private Supplier<T> storageCreator;

        private GeneticTypeBuilder() { }

        public GeneticTypeBuilder<T> onChange(GeneticValueApplier<T, ComponentAccess> onChange) {
            this.onChange = onChange;
            return this;
        }

        public <E extends EntityComponent> GeneticTypeBuilder<T> onChange(EntityComponentType<E, ?> type, GeneticValueApplier<T, E> onChange) {
            this.onChange = (value, rawValue, access, storage) -> access.get(type).ifPresent(c -> onChange.apply(value, rawValue, c, storage));
            return this;
        }

        public GeneticTypeBuilder<T> storage(Supplier<T> storageCreator) {
            this.storageCreator = storageCreator;
            return this;
        }

        public GeneticType<T> build() {
            return new GeneticType<>(this.onChange, this.storageCreator);
        }
    }
}
