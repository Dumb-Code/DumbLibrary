package net.dumbcode.dumblibrary.server.dna;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.server.attributes.ModifiableField;
import net.dumbcode.dumblibrary.server.dna.data.ColouredGeneticDataHandler;
import net.dumbcode.dumblibrary.server.dna.data.FloatGeneticDataHandler;
import net.dumbcode.dumblibrary.server.dna.data.GeneticDataHandler;
import net.dumbcode.dumblibrary.server.dna.data.GeneticTint;
import net.dumbcode.dumblibrary.server.dna.storages.GeneticFieldModifierStorage;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class GeneticType<T extends GeneticFactoryStorage<O>, O> extends ForgeRegistryEntry<GeneticType<? ,?>> {
    private final GeneticValueApplier<T, ComponentAccess, O> onChange;
    private final GeneticValueApplier<T, ComponentAccess, O> onRemove;
    private final GeneticDataHandler<O> dataHandler;
    private final Supplier<T> storage;

    @SuppressWarnings("unchecked")
    public static Class<GeneticType<?, ?>> getWildcardType() {
        return (Class<GeneticType<?, ?>>) (Class<?>) GeneticType.class;
    }

    public static <E extends EntityComponent> GeneticType<GeneticFieldModifierStorage, Float> simpleFieldModifierType(EntityComponentType<E, ?> componentType, Function<E, ModifiableField> func) {
        return GeneticType.<GeneticFieldModifierStorage>builder()
            .storage(GeneticFieldModifierStorage::new)
            .onChange(componentType, func, (value, field, storage) -> field.addModifier(storage.getRandomUUID(), value))
            .onRemove(componentType, func, (value, field, storage) -> field.removeModifier(storage.getRandomUUID()))
            .build();
    }

    public static <E extends EntityComponent> GeneticType<GeneticFieldModifierStorage, Float> simpleVanillaModifierType(Attribute attributeType, String modifierName) {
        return GeneticType.<GeneticFieldModifierStorage>builder()
            .storage(GeneticFieldModifierStorage::new)
            .onChange((value, type, storage) -> {
                if (type instanceof LivingEntity) {
                    ModifiableAttributeInstance attribute = ((LivingEntity) type).getAttribute(attributeType);
                    if (attribute != null) {
                        AttributeModifier modifier = new AttributeModifier(storage.getRandomUUID(), modifierName, value, AttributeModifier.Operation.MULTIPLY_BASE);
                        if (!attribute.hasModifier(modifier)) {
                            attribute.addPermanentModifier(modifier);
                        }
                    }
                }
            })
            .onRemove((value, type, storage) -> {
                if (type instanceof LivingEntity) {
                    ModifiableAttributeInstance attribute = ((LivingEntity) type).getAttribute(attributeType);
                    if (attribute != null) {
                        attribute.removeModifier(storage.getRandomUUID());
                    }
                }
            })
            .build();
    }

    @Deprecated
    public static <E extends EntityComponent> GeneticType<GeneticFieldModifierStorage, Float> unfinished() {
        return GeneticType.<GeneticFieldModifierStorage>builder().storage(GeneticFieldModifierStorage::new).build();
    }

    public MutableComponent getTranslationComponent() {
        ResourceLocation name = this.getRegistryName();
        return new TranslationTextComponent(name.getNamespace() + ".genetic_type." + name.getPath());
    }

    public static <T extends GeneticFactoryStorage<Float>> GeneticTypeBuilder<T, Float> builder() {
        return builder(FloatGeneticDataHandler.INSTANCE);
    }

    public static <T extends GeneticFactoryStorage<O>, O> GeneticTypeBuilder<T, O> builder(GeneticDataHandler<O> dataHandler) {
        return new GeneticTypeBuilder<>(dataHandler);
    }

    public static class GeneticTypeBuilder<T extends GeneticFactoryStorage<O>, O> {
        private final GeneticDataHandler<O> dataHandler;
        private GeneticValueApplier<T, ComponentAccess, O> onChange = (value, type, storage1) -> {};
        private GeneticValueApplier<T, ComponentAccess, O> onRemove = (value, type, storage1) -> {};
        private Supplier<T> storageCreator = () -> null;

        private GeneticTypeBuilder(GeneticDataHandler<O> dataHandler) {
            this.dataHandler = dataHandler;
        }

        public GeneticTypeBuilder<T, O> onChange(GeneticValueApplier<T, ComponentAccess, O> onChange) {
            this.onChange = onChange;
            return this;
        }

        public <E extends EntityComponent> GeneticTypeBuilder<T, O> onChange(EntityComponentType<E, ?> type, GeneticValueApplier<T, E, O> onChange) {
            this.onChange = this.convert(type, onChange);
            return this;
        }

        public <E extends EntityComponent> GeneticTypeBuilder<T, O> onChange(EntityComponentType<E, ?> type, Function<E, ModifiableField> func, GeneticValueApplier<T, ModifiableField, O> onChange) {
            this.onChange = this.convert(type, func, onChange);
            return this;
        }

        public GeneticTypeBuilder<T, O> onRemove(GeneticValueApplier<T, ComponentAccess, O> onChange) {
            this.onRemove = onChange;
            return this;
        }

        public <E extends EntityComponent> GeneticTypeBuilder<T, O> onRemove(EntityComponentType<E, ?> type, GeneticValueApplier<T, E, O> onChange) {
            this.onRemove = this.convert(type, onChange);
            return this;
        }

        public <E extends EntityComponent> GeneticTypeBuilder<T, O> onRemove(EntityComponentType<E, ?> type, Function<E, ModifiableField> func, GeneticValueApplier<T, ModifiableField, O> onChange) {
            this.onRemove = this.convert(type, func, onChange);
            return this;
        }

        private <E extends EntityComponent> GeneticValueApplier<T, ComponentAccess, O> convert(EntityComponentType<E, ?> type, GeneticValueApplier<T, E, O> onChange) {
            return (value, access, storage) -> access.get(type).ifPresent(c -> onChange.apply(value, c, storage));
        }

        private <E extends EntityComponent> GeneticValueApplier<T, ComponentAccess, O> convert(EntityComponentType<E, ?> type, Function<E, ModifiableField> func, GeneticValueApplier<T, ModifiableField, O> onChange) {
            return (value, access, storage) -> access.get(type).map(func).ifPresent(c -> onChange.apply(value, c, storage));
        }

        public GeneticTypeBuilder<T, O> storage(Supplier<T> storageCreator) {
            this.storageCreator = storageCreator;
            return this;
        }

        public GeneticType<T, O> build() {
            return new GeneticType<>(this.onChange, this.onRemove, this.dataHandler, this.storageCreator);
        }
    }
}
