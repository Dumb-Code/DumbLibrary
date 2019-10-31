package net.dumbcode.dumblibrary.server.registry;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.animation.objects.Animation;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationFactor;
import net.dumbcode.dumblibrary.server.dna.GeneticType;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentType;
import net.dumbcode.dumblibrary.server.ecs.component.RegisterComponentsEvent;
import net.dumbcode.dumblibrary.server.ecs.component.RegisterStoragesEvent;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolderRegistry;
import net.minecraftforge.registries.RegistryBuilder;

@Mod.EventBusSubscriber(modid = DumbLibrary.MODID)
public class DumbRegistryHandler {

    @SubscribeEvent
    public static void onRegisteryRegister(RegistryEvent.NewRegistry event) {
        new RegistryBuilder<Animation>()
                .setType(Animation.class)
                .setName(new ResourceLocation(DumbLibrary.MODID, "animation"))
                .setDefaultKey(new ResourceLocation(DumbLibrary.MODID, "none"))
                .set((key, isNetwork) -> Animation.NONE)
                .create();

        new RegistryBuilder<AnimationFactor>()
                .setType(AnimationFactor.class)
                .setName(new ResourceLocation(DumbLibrary.MODID, "animation_factor"))
                .setDefaultKey(new ResourceLocation(DumbLibrary.MODID, "default"))
                .set((key, isNetwork) -> AnimationFactor.DEFAULT)
                .create();

        new RegistryBuilder<GeneticType>()
            .setType(GeneticType.class)
            .setName(new ResourceLocation(DumbLibrary.MODID, "genetic_type"))
            .create();

        IForgeRegistry<EntityComponentType<?, ?>> registry = new RegistryBuilder<EntityComponentType<?, ?>>()
                .setType(EntityComponentType.getWildcardType())
                .setName(new ResourceLocation(DumbLibrary.MODID, "component"))
                .create();



        MinecraftForge.EVENT_BUS.post(new RegisterComponentsEvent(registry));
    }

    @SubscribeEvent
    public static void onAnimationRegister(RegistryEvent.Register<Animation> event) {
        event.getRegistry().register(new Animation().setRegistryName("none"));
    }

    @SubscribeEvent
    public static void onFloatSupplierRegistry(RegistryEvent.Register<AnimationFactor> event) {
        event.getRegistry().register(AnimationFactor.DEFAULT);
    }
}
