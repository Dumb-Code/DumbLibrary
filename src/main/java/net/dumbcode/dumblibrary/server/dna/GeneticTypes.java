package net.dumbcode.dumblibrary.server.dna;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.dna.datahandlers.ColouredGeneticDataHandler;
import net.dumbcode.dumblibrary.server.dna.storages.GeneticFieldModifierStorage;
import net.dumbcode.dumblibrary.server.dna.storages.GeneticTypeLayerColorStorage;
import net.dumbcode.dumblibrary.server.dna.storages.GeneticTypeOverallTintStorage;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.RenderAdjustmentsComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.SleepingComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.data.GeneticLayerEntry;
import net.dumbcode.dumblibrary.server.registry.EarlyDeferredRegister;
import net.dumbcode.dumblibrary.server.utils.GeneticUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class GeneticTypes {

    public static final EarlyDeferredRegister<GeneticType<?>> REGISTER = EarlyDeferredRegister.create(GeneticType.getWildcardType(), DumbLibrary.MODID);

    private static final Supplier<IForgeRegistry<GeneticType<?>>> REGISTRY = REGISTER.makeRegistry("genetic_type", RegistryBuilder::new);


    public static final RegistryObject<GeneticType<GeneticTypeLayerColorStorage>> LAYER_COLORS = REGISTER.register("layer_colors", () ->
        GeneticType.<GeneticTypeLayerColorStorage>builder()
            .storage(GeneticTypeLayerColorStorage::new)
            .onChange(EntityComponentTypes.GENETIC_LAYER_COLORS.get(), (value, rawValue, component, storage)
                -> component.setLayerValues(storage.getRandomUUID(), storage.getLayerName(), GeneticUtils.decodeFloatColor(rawValue)))
            .dataHandler(ColouredGeneticDataHandler.INSTANCE)
            .build()
    );


    public static final RegistryObject<GeneticType<GeneticTypeOverallTintStorage>> OVERALL_TINT = REGISTER.register("overall_tint", () ->
        GeneticType.<GeneticTypeOverallTintStorage>builder()
            .storage(GeneticTypeOverallTintStorage::new)
            .onChange(EntityComponentTypes.GENETIC_LAYER_COLORS.get(), (value, rawValue, component, storage) -> {
                for (GeneticLayerEntry entry : component.getEntries()) {
                    if(storage.getTintType() == GeneticTypeOverallTintStorage.TintType.DIRECT) {
                        entry.addDirectTint(storage.getRandomUUID(), GeneticUtils.decodeFloatColor(rawValue));
                    } else {
                        entry.addTargetTint(storage.getRandomUUID(), GeneticUtils.decodeFloatColor(rawValue));
                    }
                }
            })
            .dataHandler(ColouredGeneticDataHandler.INSTANCE)
            .build()
    );
    public static final RegistryObject<GeneticType<GeneticFieldModifierStorage>> SPEED_MODIFIER = REGISTER.register("speed_modifier", () -> GeneticType.simpleVanillaModifierType(Attributes.MOVEMENT_SPEED, "speed_genetics"));
    public static final RegistryObject<GeneticType<GeneticFieldModifierStorage>> HEALTH_MODIFIER = REGISTER.register("health_modifier", () -> GeneticType.simpleVanillaModifierType(Attributes.MAX_HEALTH, "health_genetics"));
    public static final RegistryObject<GeneticType<GeneticFieldModifierStorage>> JUMP_STRENGTH = REGISTER.register("jump_strength", () -> GeneticType.simpleVanillaModifierType(Attributes.JUMP_STRENGTH, "jump_genetics"));


    public static final RegistryObject<GeneticType<GeneticFieldModifierStorage>> SIZE = REGISTER.register("size", () -> GeneticType.simpleFieldModifierType(EntityComponentTypes.RENDER_ADJUSTMENTS.get(), RenderAdjustmentsComponent::getScaleModifier));
    public static final RegistryObject<GeneticType<GeneticFieldModifierStorage>> NOCTURNAL_CHANCE = REGISTER.register("nocturnal_chance", () -> GeneticType.simpleFieldModifierType(EntityComponentTypes.SLEEPING.get(), SleepingComponent::getNocturnalChance));
    public static final RegistryObject<GeneticType<?>> REPRODUCTIVE_CAPABILITY = REGISTER.register("reproductive_capability", GeneticType::unfinished);
    public static final RegistryObject<GeneticType<?>> IMMUNITY = REGISTER.register("immunity", GeneticType::unfinished);
    public static final RegistryObject<GeneticType<?>> UNDERWATER_CAPACITY = REGISTER.register("underwater_capacity", GeneticType::unfinished);
    public static final RegistryObject<GeneticType<?>> INTELLIGENCE = REGISTER.register("intelligence", GeneticType::unfinished);
    public static final RegistryObject<GeneticType<?>> TAMING_CHANCE = REGISTER.register("taming_chance", GeneticType::unfinished);
    public static final RegistryObject<GeneticType<?>> HEAT_RESISTANCE = REGISTER.register("heat_resistance", GeneticType::unfinished);
    public static final RegistryObject<GeneticType<?>> HEALTH_REGEN_SPEED = REGISTER.register("health_regen_speed", GeneticType::unfinished);

    public static IForgeRegistry<GeneticType<?>> registry() {
        return REGISTRY.get();
    }
}
