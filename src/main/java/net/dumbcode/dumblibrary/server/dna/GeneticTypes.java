package net.dumbcode.dumblibrary.server.dna;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.dna.data.ColouredGeneticDataHandler;
import net.dumbcode.dumblibrary.server.dna.data.GeneticTint;
import net.dumbcode.dumblibrary.server.dna.storages.GeneticFieldModifierStorage;
import net.dumbcode.dumblibrary.server.dna.storages.GeneticLayerColorStorage;
import net.dumbcode.dumblibrary.server.dna.storages.GeneticTypeOverallTintStorage;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.RenderAdjustmentsComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.SleepingComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.data.GeneticLayerEntry;
import net.dumbcode.dumblibrary.server.registry.EarlyDeferredRegister;
import net.dumbcode.dumblibrary.server.utils.GeneticUtils;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class GeneticTypes {

    public static final EarlyDeferredRegister<GeneticType<?, ?>> REGISTER = EarlyDeferredRegister.create(GeneticType.getWildcardType(), DumbLibrary.MODID);

    private static final Supplier<IForgeRegistry<GeneticType<?, ?>>> REGISTRY = REGISTER.makeRegistry("genetic_type", RegistryBuilder::new);


    public static final RegistryObject<GeneticType<GeneticLayerColorStorage, GeneticTint>> LAYER_COLORS = REGISTER.register("layer_colors", () ->
        GeneticType.<GeneticLayerColorStorage, GeneticTint>builder(ColouredGeneticDataHandler.INSTANCE)
            .storage(GeneticLayerColorStorage::new)
            .onChange(EntityComponentTypes.GENETIC_LAYER_COLORS.get(), (value, component, storage)
                -> component.setLayerValues(storage.getRandomUUID(), storage.getLayerName(), value.getPrimary()))
            .onRemove(EntityComponentTypes.GENETIC_LAYER_COLORS.get(), (value, component, storage)
                -> component.removeLayerValues(storage.getRandomUUID(), storage.getLayerName()))
            .build()
    );


    public static final RegistryObject<GeneticType<GeneticTypeOverallTintStorage, GeneticTint>> OVERALL_TINT = REGISTER.register("overall_tint", () ->
        GeneticType.<GeneticTypeOverallTintStorage, GeneticTint>builder(ColouredGeneticDataHandler.INSTANCE)
            .storage(GeneticTypeOverallTintStorage::new)
            .onChange(EntityComponentTypes.GENETIC_LAYER_COLORS.get(), (value, component, storage) -> {
                for (GeneticLayerEntry entry : component.getEntries()) {
                    GeneticTint.Part part = entry.isPrimary() ? value.getPrimary() : value.getSecondary();
                    if(storage.getTintType() == GeneticTypeOverallTintStorage.TintType.DIRECT) {
                        entry.addDirectTint(storage.getRandomUUID(), part);
                    } else {
                        entry.addTargetTint(storage.getRandomUUID(), part);
                    }
                }
            })
            .onRemove(EntityComponentTypes.GENETIC_LAYER_COLORS.get(), (value, component, storage) -> {
                for (GeneticLayerEntry entry : component.getEntries()) {
                    if(storage.getTintType() == GeneticTypeOverallTintStorage.TintType.DIRECT) {
                        entry.removeDirectTint(storage.getRandomUUID());
                    } else {
                        entry.removeTargetTint(storage.getRandomUUID());
                    }
                }
            })
            .build()
    );
    public static final RegistryObject<GeneticType<GeneticFieldModifierStorage, Float>> SPEED_MODIFIER = REGISTER.register("speed_modifier", () -> GeneticType.simpleVanillaModifierType(Attributes.MOVEMENT_SPEED, "speed_genetics"));
    public static final RegistryObject<GeneticType<GeneticFieldModifierStorage, Float>> HEALTH_MODIFIER = REGISTER.register("health_modifier", () -> GeneticType.simpleVanillaModifierType(Attributes.MAX_HEALTH, "health_genetics"));
    public static final RegistryObject<GeneticType<GeneticFieldModifierStorage, Float>> JUMP_STRENGTH = REGISTER.register("jump_strength", () -> GeneticType.simpleVanillaModifierType(Attributes.JUMP_STRENGTH, "jump_genetics"));


    public static final RegistryObject<GeneticType<GeneticFieldModifierStorage, Float>> SIZE = REGISTER.register("size", () -> GeneticType.simpleFieldModifierType(EntityComponentTypes.RENDER_ADJUSTMENTS.get(), RenderAdjustmentsComponent::getScaleModifier));
    public static final RegistryObject<GeneticType<GeneticFieldModifierStorage, Float>> NOCTURNAL_CHANCE = REGISTER.register("nocturnal_chance", () -> GeneticType.simpleFieldModifierType(EntityComponentTypes.SLEEPING.get(), SleepingComponent::getNocturnalChance));
    public static final RegistryObject<GeneticType<GeneticFieldModifierStorage, Float>> REPRODUCTIVE_CAPABILITY = REGISTER.register("reproductive_capability", GeneticType::unfinished);
    public static final RegistryObject<GeneticType<GeneticFieldModifierStorage, Float>> IMMUNITY = REGISTER.register("immunity", GeneticType::unfinished);
    public static final RegistryObject<GeneticType<GeneticFieldModifierStorage, Float>> UNDERWATER_CAPACITY = REGISTER.register("underwater_capacity", GeneticType::unfinished);
    public static final RegistryObject<GeneticType<GeneticFieldModifierStorage, Float>> INTELLIGENCE = REGISTER.register("intelligence", GeneticType::unfinished);
    public static final RegistryObject<GeneticType<GeneticFieldModifierStorage, Float>> TAMING_CHANCE = REGISTER.register("taming_chance", GeneticType::unfinished);
    public static final RegistryObject<GeneticType<GeneticFieldModifierStorage, Float>> HEAT_RESISTANCE = REGISTER.register("heat_resistance", GeneticType::unfinished);
    public static final RegistryObject<GeneticType<GeneticFieldModifierStorage, Float>> HEALTH_REGEN_SPEED = REGISTER.register("health_regen_speed", GeneticType::unfinished);

    public static IForgeRegistry<GeneticType<?, ?>> registry() {
        return REGISTRY.get();
    }
}
