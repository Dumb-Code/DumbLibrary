package net.dumbcode.dumblibrary.server.dna;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.dna.storages.GeneticTypeLayerColorStorage;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.utils.InjectedUtils;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber(modid = DumbLibrary.MODID)
@GameRegistry.ObjectHolder(DumbLibrary.MODID)
public class GeneticTypes {

    public static final GeneticType<GeneticTypeLayerColorStorage> LAYER_COLORS = InjectedUtils.injected();

    @SubscribeEvent
    public static void onRegisterGenetics(RegistryEvent.Register event) {
        if(event.getGenericType() == GeneticType.class) {
            event.getRegistry().registerAll(
                GeneticType.<GeneticTypeLayerColorStorage>builder()
                    .storage(GeneticTypeLayerColorStorage::new)
                    .onChange(EntityComponentTypes.GENETIC_LAYER_COLORS, (value, rawValue, component, storage) -> component.setLayerValues(storage.getLayerName(), rawValue))
                    .build().setRegistryName("layer_colors")//TODO:make the reg name part of the builder
            );
        }
    }

}
