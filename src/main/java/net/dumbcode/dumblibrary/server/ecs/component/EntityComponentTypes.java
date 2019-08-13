package net.dumbcode.dumblibrary.server.ecs.component;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.ecs.blocks.components.GrowingComponent;
import net.dumbcode.dumblibrary.server.ecs.blocks.systems.GrowingSystem;
import net.dumbcode.dumblibrary.server.ecs.component.impl.*;
import net.dumbcode.dumblibrary.server.ecs.system.RegisterSystemsEvent;
import net.dumbcode.dumblibrary.server.ecs.system.impl.*;
import net.dumbcode.dumblibrary.server.utils.InjectedUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber(modid = DumbLibrary.MODID)
@GameRegistry.ObjectHolder(DumbLibrary.MODID)
public class EntityComponentTypes {
    public static final EntityComponentType<GenderComponent,?> GENDER = InjectedUtils.injected();
    public static final EntityComponentType<HerdComponent, HerdComponent.Storage> HERD = InjectedUtils.injected();
    public static final EntityComponentType<MetabolismComponent, MetabolismComponent.Storage> METABOLISM = InjectedUtils.injected();
    public static final EntityComponentType<AnimationComponent,?> ANIMATION = InjectedUtils.injected();
    public static final EntityComponentType<ModelComponent, ?> MODEL = InjectedUtils.injected();
    public static final EntityComponentType<RenderAdjustmentsComponent, RenderAdjustmentsComponent.Storage> RENDER_ADJUSTMENTS = InjectedUtils.injected();
    public static final EntityComponentType<SpeedTrackingComponent, ?> SPEED_TRACKING = InjectedUtils.injected();

    public static final EntityComponentType<GrowingComponent, GrowingComponent.Storage> BLOCK_GROWING = InjectedUtils.injected();

    @SubscribeEvent
    public static void onRegisterComponents(RegisterComponentsEvent event) {
        event.getRegistry().registerAll(
                SimpleComponentType.builder(GenderComponent.class)
                        .withIdentifier(new ResourceLocation(DumbLibrary.MODID, "gender"))
                        .withConstructor(GenderComponent::new)
                        .build(),
                SimpleComponentType.builder(HerdComponent.class, HerdComponent.Storage.class)
                        .withIdentifier(new ResourceLocation(DumbLibrary.MODID, "herd"))
                        .withStorage(HerdComponent.Storage::new)
                        .withConstructor(HerdComponent::new)
                        .build(),
                SimpleComponentType.builder(MetabolismComponent.class, MetabolismComponent.Storage.class)
                        .withIdentifier(new ResourceLocation(DumbLibrary.MODID, "metabolism"))
                        .withStorage(MetabolismComponent.Storage::new)
                        .withConstructor(MetabolismComponent::new)
                        .build(),
                SimpleComponentType.builder(AnimationComponent.class)
                        .withIdentifier(new ResourceLocation(DumbLibrary.MODID, "animation"))
                        .withConstructor(AnimationComponent::new)
                        .build(),
                SimpleComponentType.builder(ModelComponent.class)
                        .withIdentifier(new ResourceLocation(DumbLibrary.MODID, "model"))
                        .withConstructor(ModelComponent::new)
                        .build(),
                SimpleComponentType.builder(RenderAdjustmentsComponent.class, RenderAdjustmentsComponent.Storage.class)
                        .withIdentifier(new ResourceLocation(DumbLibrary.MODID, "render_adjustments"))
                        .withConstructor(RenderAdjustmentsComponent::new)
                        .withStorage(RenderAdjustmentsComponent.Storage::new)
                        .build(),
                SimpleComponentType.builder(SpeedTrackingComponent.class)
                        .withIdentifier(new ResourceLocation(DumbLibrary.MODID, "speed_tracking"))
                        .withConstructor(SpeedTrackingComponent::new)
                        .build(),

                SimpleComponentType.builder(GrowingComponent.class, GrowingComponent.Storage.class)
                        .withIdentifier(new ResourceLocation(DumbLibrary.MODID, "block_growing"))
                        .withConstructor(GrowingComponent::new)
                        .withStorage(GrowingComponent.Storage::new)
                        .build()
        );
    }

    @SubscribeEvent
    public static void register(RegisterSystemsEvent event) {
        event.registerSystem(MetabolismSystem.INSTANCE);
        event.registerSystem(HerdSystem.INSTANCE);
        event.registerSystem(AnimationSystem.INSTANCE);
        event.registerSystem(ItemDropSystem.INSTANCE);
        event.registerSystem(SpeedTrackingSystem.INSTANCE);

        event.registerSystem(GrowingSystem.INSTANCE);
    }
}
