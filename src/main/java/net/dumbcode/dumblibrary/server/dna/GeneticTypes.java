package net.dumbcode.dumblibrary.server.dna;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.attributes.ModOp;
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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber(modid = DumbLibrary.MODID)
@GameRegistry.ObjectHolder(DumbLibrary.MODID)
public class GeneticTypes {

    public static final GeneticType<GeneticTypeLayerColorStorage> LAYER_COLORS = InjectedUtils.injected();
    public static final GeneticType<GeneticTypeOverallTintStorage> OVERALL_TINT = InjectedUtils.injected();
    public static final GeneticType<RandomUUIDStorage> SPEED_MODIFIER = InjectedUtils.injected();
    public static final GeneticType<GeneticFieldModifierStorage> SIZE = InjectedUtils.injected();
    public static final GeneticType<GeneticFieldModifierStorage> NOCTURNAL_CHANCE = InjectedUtils.injected();

    @SubscribeEvent
    public static void onRegisterGenetics(RegisterGeneticTypes event) {
        event.getRegistry().registerAll(
            GeneticType.<GeneticTypeLayerColorStorage>builder()
                .storage(GeneticTypeLayerColorStorage::new)
                .onChange(EntityComponentTypes.GENETIC_LAYER_COLORS, (value, rawValue, component, storage)
                    -> component.setLayerValues(storage.getLayerName(), GeneticUtils.decode3BitColor(rawValue)))
                .dataHandler(ColouredGeneticDataHandler.INSTANCE)
                .build("layer_colors"),

            GeneticType.<GeneticTypeOverallTintStorage>builder()
                .storage(GeneticTypeOverallTintStorage::new)
                .onChange(EntityComponentTypes.GENETIC_LAYER_COLORS, (value, rawValue, component, storage) -> {
                    for (GeneticLayerComponent.GeneticLayerEntry entry : component.getEntries()) {
                        if(storage.getTintType() == GeneticTypeOverallTintStorage.TintType.DIRECT) {
                            entry.addDirectTint(storage.getRandomUUID(), GeneticUtils.decode3BitColor(rawValue));
                        } else {
                            entry.addTargetTint(storage.getRandomUUID(), GeneticUtils.decode3BitColor(rawValue));
                        }
                    }
                })
                .dataHandler(ColouredGeneticDataHandler.INSTANCE)
                .build("overall_tint"),

            GeneticType.<RandomUUIDStorage>builder()
                .storage(RandomUUIDStorage::new)
                .onChange((value, rawValue, type, storage) -> {
                    if (type instanceof EntityLivingBase) {
                        IAttributeInstance attribute = ((EntityLivingBase) type).getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
                        if (attribute != null) {
                            AttributeModifier modifier = new AttributeModifier(storage.getRandomUUID(), "speed_genetics", MathHelper.clamp(value / 4F, -1, 1), 1);
                            if (!attribute.hasModifier(modifier)) {
                                attribute.applyModifier(modifier);
                            }
                        }
                    }
                })
                .build("speed_modifier"),

            GeneticType.simpleFieldModifierType(EntityComponentTypes.RENDER_ADJUSTMENTS, RenderAdjustmentsComponent::getScaleModifier, "size"),
            GeneticType.simpleFieldModifierType(EntityComponentTypes.SLEEPING, SleepingComponent::getNocturnalChance, "nocturnal_chance")
        );
    }



}
