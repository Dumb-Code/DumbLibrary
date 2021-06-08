package net.dumbcode.dumblibrary.server.dna;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.dna.datahandlers.ColouredGeneticDataHandler;
import net.dumbcode.dumblibrary.server.dna.storages.GeneticTypeLayerColorStorage;
import net.dumbcode.dumblibrary.server.dna.storages.RandomUUIDStorage;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.GeneticLayerComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.RenderAdjustmentsComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.SleepingComponent;
import net.dumbcode.dumblibrary.server.registry.RegisterGeneticTypes;
import net.dumbcode.dumblibrary.server.utils.GeneticUtils;
import net.dumbcode.dumblibrary.server.utils.InjectedUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class GeneticTypes {

    public static final DeferredRegister<GeneticType<?>> REGISTER = DeferredRegister.create(GeneticType.getWildcardType(), DumbLibrary.MODID);

    private static final Supplier<IForgeRegistry<GeneticType<?>>> REGISTRY = REGISTER.makeRegistry("genetic_type", RegistryBuilder::new);


    public static final RegistryObject<GeneticType<GeneticTypeLayerColorStorage>> LAYER_COLORS = REGISTER.register("layer_colors", () ->
        GeneticType.<GeneticTypeLayerColorStorage>builder()
            .storage(GeneticTypeLayerColorStorage::new)
            .onChange(EntityComponentTypes.GENETIC_LAYER_COLORS.get(), (value, rawValue, component, storage)
                -> component.setLayerValues(storage.getLayerName(), GeneticUtils.decode3BitColor(rawValue)))
            .dataHandler(ColouredGeneticDataHandler.INSTANCE)
            .build()
    );


    public static final RegistryObject<GeneticType<GeneticTypeOverallTintStorage>> OVERALL_TINT = REGISTER.register("overall_tint", () ->
        GeneticType.<GeneticTypeOverallTintStorage>builder()
            .storage(GeneticTypeOverallTintStorage::new)
            .onChange(EntityComponentTypes.GENETIC_LAYER_COLORS.get(), (value, rawValue, component, storage) -> {
                for (GeneticLayerComponent.GeneticLayerEntry entry : component.getEntries()) {
                    if(storage.getTintType() == GeneticTypeOverallTintStorage.TintType.DIRECT) {
                        entry.addDirectTint(storage.getRandomUUID(), GeneticUtils.decode3BitColor(rawValue));
                    } else {
                        entry.addTargetTint(storage.getRandomUUID(), GeneticUtils.decode3BitColor(rawValue));
                    }
                }
            })
            .dataHandler(ColouredGeneticDataHandler.INSTANCE)
            .build()
    );
    public static final RegistryObject<GeneticType<RandomUUIDStorage>> SPEED_MODIFIER = REGISTER.register("speed_modifier", () ->
        GeneticType.<RandomUUIDStorage>builder()
            .storage(RandomUUIDStorage::new)
            .onChange((value, rawValue, type, storage) -> {
                if (type instanceof LivingEntity) {
                    ModifiableAttributeInstance attribute = ((LivingEntity) type).getAttribute(Attributes.MOVEMENT_SPEED);
                    if (attribute != null) {
                        AttributeModifier modifier = new AttributeModifier(storage.getRandomUUID(), "speed_genetics", MathHelper.clamp(value / 4F, -1, 1), AttributeModifier.Operation.MULTIPLY_BASE);
                        if (!attribute.hasModifier(modifier)) {
                            attribute.addPermanentModifier(modifier);
                        }
                    }
                }
            })
            .build()
    );

    public static final RegistryObject<GeneticType<GeneticFieldModifierStorage>> SIZE = REGISTER.register("size", () -> GeneticType.simpleFieldModifierType(EntityComponentTypes.RENDER_ADJUSTMENTS.get(), RenderAdjustmentsComponent::getScaleModifier));
    public static final RegistryObject<GeneticType<GeneticFieldModifierStorage>> NOCTURNAL_CHANCE = REGISTER.register("nocturnal_chance", () -> GeneticType.simpleFieldModifierType(EntityComponentTypes.SLEEPING.get(), SleepingComponent::getNocturnalChance));

    public static IForgeRegistry<GeneticType<?>> registry() {
        return REGISTRY.get();
    }
}
