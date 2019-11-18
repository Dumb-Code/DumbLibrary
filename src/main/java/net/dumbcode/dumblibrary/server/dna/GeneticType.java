package net.dumbcode.dumblibrary.server.dna;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.function.BinaryOperator;
import java.util.function.Supplier;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class GeneticType<T extends GeneticFactoryStorage> extends IForgeRegistryEntry.Impl<GeneticType<?>> {
    private final GeneticValueApplier<T, ComponentAccess> onChange;
    private final BinaryOperator<Float> combiner;
    private final Supplier<T> storage;

    @SuppressWarnings("unchecked")
    public static Class<GeneticType<?>> getWildcardType() {
        return (Class<GeneticType<?>>) (Class<?>) GeneticType.class;
    }

    public static <T extends GeneticFactoryStorage> GeneticTypeBuilder<T> builder() {
        return new GeneticTypeBuilder<>();
    }

    public static class GeneticTypeBuilder<T extends GeneticFactoryStorage> {
        private GeneticValueApplier<T, ComponentAccess> onChange = (value, rawValue, type, storage1) -> {};
        private BinaryOperator<Float> combiner = (a, b) -> (a + b) / 2;
        private Supplier<T> storageCreator = () -> null;

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

        public GeneticTypeBuilder<T> onCombined(BinaryOperator<Float> combiner) {
            this.combiner = combiner;
            return this;
        }

        public GeneticType<T> build(String registryName) {
            GeneticType<T> type = new GeneticType<>(this.onChange, this.combiner, this.storageCreator);
            type.setRegistryName(registryName);
            return type;
        }
    }
}
