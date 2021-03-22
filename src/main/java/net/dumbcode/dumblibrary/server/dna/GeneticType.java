package net.dumbcode.dumblibrary.server.dna;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.server.attributes.ModOp;
import net.dumbcode.dumblibrary.server.attributes.ModifiableField;
import net.dumbcode.dumblibrary.server.dna.datahandlers.FloatGeneticDataHandler;
import net.dumbcode.dumblibrary.server.dna.datahandlers.GeneticDataHandler;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentType;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class GeneticType<T extends GeneticFactoryStorage> implements IForgeRegistryEntry<GeneticType<?>> {
    private final GeneticValueApplier<T, ComponentAccess> onChange;
    private final GeneticDataHandler dataHandler;
    private final Supplier<T> storage;

    @SuppressWarnings("unchecked")
    public static Class<GeneticType<?>> getWildcardType() {
        return (Class<GeneticType<?>>) (Class<?>) GeneticType.class;
    }

    public static <E extends EntityComponent> GeneticType<GeneticFieldModifierStorage> simpleFieldModifierType(EntityComponentType<E, ?> componentType, Function<E, ModifiableField> func, String name) {
        return GeneticType.<GeneticFieldModifierStorage>builder()
            .storage(GeneticFieldModifierStorage::new)
            .onChange(componentType, func, (value, rawValue, field, storage) -> field.addModifer(storage.getRandomUUID(), storage.getOperation(), value*storage.getModifier()))
            .build(name);
    }

    public static <T extends GeneticFactoryStorage> GeneticTypeBuilder<T> builder() {
        return new GeneticTypeBuilder<>();
    }

    public static class GeneticTypeBuilder<T extends GeneticFactoryStorage> {
        private GeneticValueApplier<T, ComponentAccess> onChange = (value, rawValue, type, storage1) -> {};
        private GeneticDataHandler dataHandler = FloatGeneticDataHandler.INSTANCE;
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

        public <E extends EntityComponent> GeneticTypeBuilder<T> onChange(EntityComponentType<E, ?> type, Function<E, ModifiableField> func, GeneticValueApplier<T, ModifiableField> onChange) {
            this.onChange = (value, rawValue, access, storage) -> access.get(type).map(func).ifPresent(c -> onChange.apply(value, rawValue, c, storage));
            return this;
        }

        public GeneticTypeBuilder<T> storage(Supplier<T> storageCreator) {
            this.storageCreator = storageCreator;
            return this;
        }

        public GeneticTypeBuilder<T> dataHandler(GeneticDataHandler dataHandler) {
            this.dataHandler = dataHandler;
            return this;
        }

        public GeneticType<T> build(String registryName) {
            GeneticType<T> type = new GeneticType<>(this.onChange, this.dataHandler, this.storageCreator);
            type.setRegistryName(registryName);
            return type;
        }
    }
}
