package net.dumbcode.dumblibrary.server.dna;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.dna.storages.GeneticTypeLayerColorStorage;
import net.dumbcode.dumblibrary.server.dna.storages.RandomUUIDStorage;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.registry.RegisterGeneticTypes;
import net.dumbcode.dumblibrary.server.utils.InjectedUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber(modid = DumbLibrary.MODID)
@GameRegistry.ObjectHolder(DumbLibrary.MODID)
public class GeneticTypes {

    public static final GeneticType<GeneticTypeLayerColorStorage> LAYER_COLORS = InjectedUtils.injected();
    public static final GeneticType<RandomUUIDStorage> SPEED_MODIFIER = InjectedUtils.injected();

    @SubscribeEvent
    public static void onRegisterGenetics(RegisterGeneticTypes event) {
        event.getRegistry().registerAll(
            GeneticType.<GeneticTypeLayerColorStorage>builder()
                .storage(GeneticTypeLayerColorStorage::new)
                .onChange(EntityComponentTypes.GENETIC_LAYER_COLORS, (value, rawValue, component, storage) -> component.setLayerValues(storage.getLayerName(), rawValue))
                .build().setRegistryName("layer_colors"),//TODO:make the reg name part of the builder
            GeneticType.<RandomUUIDStorage>builder()
                .storage(RandomUUIDStorage::new)
                .onChange((value, rawValue, type, storage) -> {
                    if(type instanceof EntityLivingBase) {
                        IAttributeInstance attribute = ((EntityLivingBase) type).getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
                        if(attribute != null) {
                            AttributeModifier modifier = new AttributeModifier(storage.getRandomUUID(), "speed_genetics", MathHelper.clamp(value/4F, -1, 1), 1);
                            if(!attribute.hasModifier(modifier)) {
                                attribute.applyModifier(modifier);
                            }
                        }
                    }
                })
                .build().setRegistryName("speed_modifier")
        );
    }

}
