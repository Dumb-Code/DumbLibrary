package net.dumbcode.dumblibrary.server.registry;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.dna.GeneticType;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentType;
import net.dumbcode.dumblibrary.server.ecs.component.RegisterComponentsEvent;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

@Mod.EventBusSubscriber(modid = DumbLibrary.MODID)
public class DumbRegistryHandler {

    @SubscribeEvent
    public static void onRegisteryRegister(RegistryEvent.NewRegistry event) {
        new RegistryBuilder<GeneticType<?>>()
            .setType(GeneticType.getWildcardType())
            .setName(new ResourceLocation(DumbLibrary.MODID, "genetic_type"))
            .create();

        IForgeRegistry<EntityComponentType<?, ?>> registry = new RegistryBuilder<EntityComponentType<?, ?>>()
                .setType(EntityComponentType.getWildcardType())
                .setName(new ResourceLocation(DumbLibrary.MODID, "component"))
                .create();
        
        MinecraftForge.EVENT_BUS.post(new RegisterComponentsEvent(registry));
    }
}
