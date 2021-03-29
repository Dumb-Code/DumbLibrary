package net.dumbcode.dumblibrary.server.ecs.component;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.ecs.blocks.components.*;
import net.dumbcode.dumblibrary.server.ecs.blocks.systems.*;
import net.dumbcode.dumblibrary.server.ecs.component.impl.*;
import net.dumbcode.dumblibrary.server.ecs.item.components.ItemEatenComponent;
import net.dumbcode.dumblibrary.server.ecs.item.components.ItemRenderModelComponent;
import net.dumbcode.dumblibrary.server.ecs.item.systems.ItemEatenSystem;
import net.dumbcode.dumblibrary.server.ecs.system.RegisterSystemsEvent;
import net.dumbcode.dumblibrary.server.ecs.system.impl.*;
import net.dumbcode.dumblibrary.server.utils.InjectedUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(DumbLibrary.MODID)
public class EntityComponentTypes {
    public static final EntityComponentType<GenderComponent,?> GENDER = InjectedUtils.injected();
    public static final EntityComponentType<HerdComponent, HerdComponent.Storage> HERD = InjectedUtils.injected();
    public static final EntityComponentType<AnimationComponent,?> ANIMATION = InjectedUtils.injected();
    public static final EntityComponentType<ModelComponent, ModelComponent.Storage> MODEL = InjectedUtils.injected();
    public static final EntityComponentType<RenderAdjustmentsComponent, RenderAdjustmentsComponent.Storage> RENDER_ADJUSTMENTS = InjectedUtils.injected();
    public static final EntityComponentType<SpeedTrackingComponent, ?> SPEED_TRACKING = InjectedUtils.injected();
    public static final EntityComponentType<GeneticComponent, GeneticComponent.Storage> GENETICS = InjectedUtils.injected();
    public static final EntityComponentType<GeneticLayerComponent, GeneticLayerComponent.Storage> GENETIC_LAYER_COLORS = InjectedUtils.injected();
    public static final EntityComponentType<FlattenedLayerComponent, FlattenedLayerComponent.Storage> FLATTENED_LAYER = InjectedUtils.injected();
    public static final EntityComponentType<EyesClosedComponent, EyesClosedComponent.Storage> EYES_CLOSED = InjectedUtils.injected();
    public static final EntityComponentType<BlinkingComponent, BlinkingComponent.Storage> BLINKING = InjectedUtils.injected();
    public static final EntityComponentType<FamilyComponent, FamilyComponent.Storage> FAMILY = InjectedUtils.injected();
    public static final EntityComponentType<BreedingComponent, BreedingComponent.Storage> BREEDING = InjectedUtils.injected();
    public static final EntityComponentType<SleepingComponent, SleepingComponent.Storage> SLEEPING = InjectedUtils.injected();
    public static final EntityComponentType<CloseProximityAngryComponent, CloseProximityAngryComponent.Storage> CLOSE_PROXIMITY_ANGRY = InjectedUtils.injected();
    public static final EntityComponentType<SoundStorageComponent, SoundStorageComponent.Storage> SOUND_STORAGE = InjectedUtils.injected();
    public static final EntityComponentType<IdleActionComponent, IdleActionComponent.Storage> IDLE_ACTION = InjectedUtils.injected();

    public static final EntityComponentType<GrowingComponent, GrowingComponent.Storage> BLOCK_GROWING = InjectedUtils.injected();
    public static final EntityComponentType<FlowerWorldgenComponent, FlowerWorldgenComponent.Storage> FLOWER_WORLDGEN = InjectedUtils.injected();
    public static final EntityComponentType<BlockTouchEffectComponent, BlockTouchEffectComponent.Storage> BLOCK_TOUCH_EFFECT = InjectedUtils.injected();
    public static final EntityComponentType<BlockDropsComponent, BlockDropsComponent.Storage> BLOCK_DROPS = InjectedUtils.injected();
    public static final EntityComponentType<BlockPlaceableComponent, ?> BLOCK_PLACEABLE = InjectedUtils.injected();

    public static final EntityComponentType<ItemRenderModelComponent, ItemRenderModelComponent.Storage> ITEM_RENDER = InjectedUtils.injected();
    public static final EntityComponentType<ItemEatenComponent, ItemEatenComponent.Storage> ITEM_EATEN = InjectedUtils.injected();

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
            SimpleComponentType.builder(AnimationComponent.class)
                .withIdentifier(new ResourceLocation(DumbLibrary.MODID, "animation"))
                .withConstructor(AnimationComponent::new)
                .build(),
            SimpleComponentType.builder(ModelComponent.class, ModelComponent.Storage.class)
                .withIdentifier(new ResourceLocation(DumbLibrary.MODID, "model"))
                .withConstructor(ModelComponent::new)
                .withStorage(ModelComponent.Storage::new)
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
            SimpleComponentType.builder(GeneticComponent.class, GeneticComponent.Storage.class)
                .withIdentifier(new ResourceLocation(DumbLibrary.MODID, "genetics"))
                .withConstructor(GeneticComponent::new)
                .withStorage(GeneticComponent.Storage::new)
                .build(),
            SimpleComponentType.builder(GeneticLayerComponent.class, GeneticLayerComponent.Storage.class)
                .withIdentifier(new ResourceLocation(DumbLibrary.MODID, "genetic_layer_colors"))
                .withConstructor(GeneticLayerComponent::new)
                .withStorage(GeneticLayerComponent.Storage::new)
                .build(),
            SimpleComponentType.builder(FlattenedLayerComponent.class, FlattenedLayerComponent.Storage.class)
                .withIdentifier(new ResourceLocation(DumbLibrary.MODID, "flattened_layer"))
                .withConstructor(FlattenedLayerComponent::new)
                .withStorage(FlattenedLayerComponent.Storage::new)
                .build(),
            SimpleComponentType.builder(EyesClosedComponent.class, EyesClosedComponent.Storage.class)
                .withIdentifier(new ResourceLocation(DumbLibrary.MODID, "eyes_closed"))
                .withConstructor(EyesClosedComponent::new)
                .withStorage(EyesClosedComponent.Storage::new)
                .build(),
            SimpleComponentType.builder(BlinkingComponent.class, BlinkingComponent.Storage.class)
                .withIdentifier(new ResourceLocation(DumbLibrary.MODID, "blinking"))
                .withConstructor(BlinkingComponent::new)
                .withStorage(BlinkingComponent.Storage::new)
                .build(),
            SimpleComponentType.builder(FamilyComponent.class, FamilyComponent.Storage.class)
                .withIdentifier(new ResourceLocation(DumbLibrary.MODID, "family"))
                .withConstructor(FamilyComponent::new)
                .withStorage(FamilyComponent.Storage::new)
                .build(),
            SimpleComponentType.builder(BreedingComponent.class, BreedingComponent.Storage.class)
                .withIdentifier(new ResourceLocation(DumbLibrary.MODID, "breeding"))
                .withConstructor(BreedingComponent::new)
                .withStorage(BreedingComponent.Storage::new)
                .build(),
            SimpleComponentType.builder(SleepingComponent.class, SleepingComponent.Storage.class)
                .withIdentifier(new ResourceLocation(DumbLibrary.MODID, "sleeping"))
                .withConstructor(SleepingComponent::new)
                .withStorage(SleepingComponent.Storage::new)
                .build(),
            SimpleComponentType.builder(CloseProximityAngryComponent.class, CloseProximityAngryComponent.Storage.class)
                .withIdentifier(new ResourceLocation(DumbLibrary.MODID, "close_proximity_angry"))
                .withConstructor(CloseProximityAngryComponent::new)
                .withStorage(CloseProximityAngryComponent.Storage::new)
                .build(),
            SimpleComponentType.builder(SoundStorageComponent.class, SoundStorageComponent.Storage.class)
                .withIdentifier(new ResourceLocation(DumbLibrary.MODID, "sound_storage"))
                .withConstructor(SoundStorageComponent::new)
                .withStorage(SoundStorageComponent.Storage::new)
                .build(),
            SimpleComponentType.builder(IdleActionComponent.class, IdleActionComponent.Storage.class)
                .withIdentifier(new ResourceLocation(DumbLibrary.MODID, "idle_action"))
                .withConstructor(IdleActionComponent::new)
                .withStorage(IdleActionComponent.Storage::new)
                .build(),

            SimpleComponentType.builder(GrowingComponent.class, GrowingComponent.Storage.class)
                .withIdentifier(new ResourceLocation(DumbLibrary.MODID, "block_growing"))
                .withConstructor(GrowingComponent::new)
                .withStorage(GrowingComponent.Storage::new)
                .build(),
            SimpleComponentType.builder(FlowerWorldgenComponent.class, FlowerWorldgenComponent.Storage.class)
                .withIdentifier(new ResourceLocation(DumbLibrary.MODID, "flower_worldgen"))
                .withConstructor(FlowerWorldgenComponent::new)
                .withStorage(FlowerWorldgenComponent.Storage::new)
                .build(),
            SimpleComponentType.builder(BlockTouchEffectComponent.class, BlockTouchEffectComponent.Storage.class)
                .withIdentifier(new ResourceLocation(DumbLibrary.MODID, "block_touch_effect"))
                .withConstructor(BlockTouchEffectComponent::new)
                .withStorage(BlockTouchEffectComponent.Storage::new)
                .build(),
            SimpleComponentType.builder(BlockDropsComponent.class, BlockDropsComponent.Storage.class)
                .withIdentifier(new ResourceLocation(DumbLibrary.MODID, "block_drops"))
                .withConstructor(BlockDropsComponent::new)
                .withStorage(BlockDropsComponent.Storage::new)
                .build(),
            SimpleComponentType.builder(BlockPlaceableComponent.class)
                .withIdentifier(new ResourceLocation(DumbLibrary.MODID, "block_placeable"))
                .withConstructor(BlockPlaceableComponent::new)
                .build(),

            SimpleComponentType.builder(ItemRenderModelComponent.class, ItemRenderModelComponent.Storage.class)
                .withIdentifier(new ResourceLocation(DumbLibrary.MODID, "item_render"))
                .withConstructor(ItemRenderModelComponent::new)
                .withStorage(ItemRenderModelComponent.Storage::new)
                .build(),
            SimpleComponentType.builder(ItemEatenComponent.class, ItemEatenComponent.Storage.class)
                .withIdentifier(new ResourceLocation(DumbLibrary.MODID, "item_eaten"))
                .withConstructor(ItemEatenComponent::new)
                .withStorage(ItemEatenComponent.Storage::new)
                .build()
        );
    }

    public static void registerSystems(RegisterSystemsEvent event) {
        event.registerSystem(new HerdSystem());
        event.registerSystem(new ItemDropSystem());
        event.registerSystem(new SpeedTrackingSystem());
        event.registerSystem(new FamilySystem());
        event.registerSystem(new BreedingSystem());
        event.registerSystem(new BlinkingSystem());
        event.registerSystem(new EyesClosedSystem());
        event.registerSystem(new SleepingSystem());
        event.registerSystem(new CloseProximityAngrySystem());
        event.registerSystem(new IdleActionSystem());

        event.registerSystem(new GrowingSystem());
        event.registerSystem(new FlowerWorldgenSystem());
        event.registerSystem(new BlockTouchEffectSystem());
        event.registerSystem(new BlockDropsSystem());
        event.registerSystem(new BlockPlacementSystem());

        event.registerSystem(new ItemEatenSystem());

    }
}
