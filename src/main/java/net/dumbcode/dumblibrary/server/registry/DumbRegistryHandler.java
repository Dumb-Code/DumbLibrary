package net.dumbcode.dumblibrary.server.registry;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.animation.objects.Animation;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationFactor;
import net.dumbcode.dumblibrary.server.dna.GeneticType;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentType;
import net.dumbcode.dumblibrary.server.ecs.component.RegisterComponentsEvent;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

@Mod.EventBusSubscriber(modid = DumbLibrary.MODID)
public class DumbRegistryHandler {

    @SubscribeEvent
    public static void onRegisteryRegister(RegistryEvent.NewRegistry event) {
        new RegistryBuilder<AnimationFactor>()
                .setType(AnimationFactor.class)
                .setName(new ResourceLocation(DumbLibrary.MODID, "animation_factor"))
                .setDefaultKey(new ResourceLocation(DumbLibrary.MODID, "default"))
                .set((key, isNetwork) -> AnimationFactor.DEFAULT)
                .create();

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

    @SubscribeEvent
    public static void onFloatSupplierRegistry(RegistryEvent.Register<AnimationFactor> event) {
        event.getRegistry().register(AnimationFactor.DEFAULT);
    }
}
