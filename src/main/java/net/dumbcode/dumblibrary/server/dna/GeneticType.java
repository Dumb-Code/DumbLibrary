package net.dumbcode.dumblibrary.server.dna;

import com.sun.java.accessibility.util.java.awt.TextComponentTranslator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.server.attributes.ModifiableField;
import net.dumbcode.dumblibrary.server.dna.datahandlers.FloatGeneticDataHandler;
import net.dumbcode.dumblibrary.server.dna.datahandlers.GeneticDataHandler;
import net.dumbcode.dumblibrary.server.dna.storages.GeneticFieldModifierStorage;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.Function;
import java.util.function.Supplier;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class GeneticType<T extends GeneticFactoryStorage> extends ForgeRegistryEntry<GeneticType<?>> {
    private final GeneticValueApplier<T, ComponentAccess> onChange;
    private final GeneticDataHandler dataHandler;
    private final Supplier<T> storage;

    @SuppressWarnings("unchecked")
    public static Class<GeneticType<?>> getWildcardType() {
        return (Class<GeneticType<?>>) (Class<?>) GeneticType.class;
    }

    public static <E extends EntityComponent> GeneticType<GeneticFieldModifierStorage> simpleFieldModifierType(EntityComponentType<E, ?> componentType, Function<E, ModifiableField> func) {
        return GeneticType.<GeneticFieldModifierStorage>builder()
            .storage(GeneticFieldModifierStorage::new)
            .onChange(componentType, func, (value, rawValue, field, storage) -> field.addModifer(storage.getRandomUUID(), storage.getOperation(), value*storage.getModifier()))
            .build();
    }

    public static <E extends EntityComponent> GeneticType<GeneticFieldModifierStorage> simpleVanillaModifierType(Attribute attributeType, String modifierName) {
        return GeneticType.<GeneticFieldModifierStorage>builder()
            .storage(GeneticFieldModifierStorage::new)
            .onChange((value, rawValue, type, storage) -> {
                if (type instanceof LivingEntity) {
                    ModifiableAttributeInstance attribute = ((LivingEntity) type).getAttribute(attributeType);
                    if (attribute != null) {
                        AttributeModifier modifier = new AttributeModifier(storage.getRandomUUID(), modifierName, storage.getModifier()*value, storage.getOperation().getVanilla());
                        if (!attribute.hasModifier(modifier)) {
                            attribute.addPermanentModifier(modifier);
                        }
                    }
                }
            })
            .build();
    }

    @Deprecated
    public static <E extends EntityComponent> GeneticType<?> unfinished() {
        return GeneticType.builder().build();
    }

    public TranslationTextComponent getTranslationComponent() {
        ResourceLocation name = this.getRegistryName();
        return new TranslationTextComponent(name.getNamespace() + ".genetic_type" + name.getPath() + ".name");
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

        public GeneticType<T> build() {
            return new GeneticType<>(this.onChange, this.dataHandler, this.storageCreator);
        }
    }
}
