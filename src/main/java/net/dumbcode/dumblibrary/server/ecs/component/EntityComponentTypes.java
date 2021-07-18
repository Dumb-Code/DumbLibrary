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
import net.dumbcode.dumblibrary.server.registry.EarlyDeferredRegister;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class EntityComponentTypes {

    public static final EarlyDeferredRegister<EntityComponentType<?, ?>> REGISTER = EarlyDeferredRegister.create(EntityComponentType.getWildcardType(), DumbLibrary.MODID);

    private static Supplier<IForgeRegistry<EntityComponentType<?, ?>>> REGISTRY = REGISTER.makeRegistry("component", RegistryBuilder::new);

    public static final RegistryObject<EntityComponentType<GenderComponent,?>> GENDER = REGISTER.register("gender", () -> SimpleComponentType.of(GenderComponent.class, GenderComponent::new));
    public static final RegistryObject<EntityComponentType<HerdComponent, HerdComponent.Storage>> HERD = REGISTER.register("herd", () -> SimpleComponentType.of(HerdComponent.class, HerdComponent::new, HerdComponent.Storage::new));
    public static final RegistryObject<EntityComponentType<AnimationComponent,?>> ANIMATION = REGISTER.register("animation", () -> SimpleComponentType.of(AnimationComponent.class, AnimationComponent::new));
    public static final RegistryObject<EntityComponentType<ModelComponent, ModelComponent.Storage>> MODEL = REGISTER.register("model", () -> SimpleComponentType.of(ModelComponent.class, ModelComponent::new, ModelComponent.Storage::new));
    public static final RegistryObject<EntityComponentType<RenderAdjustmentsComponent, RenderAdjustmentsComponent.Storage>> RENDER_ADJUSTMENTS = REGISTER.register("render_adjustments", () -> SimpleComponentType.of(RenderAdjustmentsComponent.class, RenderAdjustmentsComponent::new, RenderAdjustmentsComponent.Storage::new));
    public static final RegistryObject<EntityComponentType<SpeedTrackingComponent, ?>> SPEED_TRACKING = REGISTER.register("speed_tracking", () -> SimpleComponentType.of(SpeedTrackingComponent.class, SpeedTrackingComponent::new));
    public static final RegistryObject<EntityComponentType<GeneticComponent, GeneticComponent.Storage>> GENETICS = REGISTER.register("genetics", () -> SimpleComponentType.of(GeneticComponent.class, GeneticComponent::new, GeneticComponent.Storage::new));
    public static final RegistryObject<EntityComponentType<GeneticLayerComponent, GeneticLayerComponent.Storage>> GENETIC_LAYER_COLORS = REGISTER.register("genetic_layer_colors", () -> SimpleComponentType.of(GeneticLayerComponent.class, GeneticLayerComponent::new, GeneticLayerComponent.Storage::new));
    public static final RegistryObject<EntityComponentType<FlattenedLayerComponent, FlattenedLayerComponent.Storage>> FLATTENED_LAYER = REGISTER.register("flattened_layer", () -> SimpleComponentType.of(FlattenedLayerComponent.class, FlattenedLayerComponent::new, FlattenedLayerComponent.Storage::new));
    public static final RegistryObject<EntityComponentType<EyesClosedComponent, EyesClosedComponent.Storage>> EYES_CLOSED = REGISTER.register("eyes_closed", () -> SimpleComponentType.of(EyesClosedComponent.class, EyesClosedComponent::new, EyesClosedComponent.Storage::new));
    public static final RegistryObject<EntityComponentType<BlinkingComponent, BlinkingComponent.Storage>> BLINKING = REGISTER.register("blinking", () -> SimpleComponentType.of(BlinkingComponent.class, BlinkingComponent::new, BlinkingComponent.Storage::new));
    public static final RegistryObject<EntityComponentType<FamilyComponent, FamilyComponent.Storage>> FAMILY = REGISTER.register("family", () -> SimpleComponentType.of(FamilyComponent.class, FamilyComponent::new, FamilyComponent.Storage::new));
    public static final RegistryObject<EntityComponentType<BreedingComponent, BreedingComponent.Storage>> BREEDING = REGISTER.register("breeding", () -> SimpleComponentType.of(BreedingComponent.class, BreedingComponent::new, BreedingComponent.Storage::new));
    public static final RegistryObject<EntityComponentType<SleepingComponent, SleepingComponent.Storage>> SLEEPING = REGISTER.register("sleeping", () -> SimpleComponentType.of(SleepingComponent.class, SleepingComponent::new, SleepingComponent.Storage::new));
    public static final RegistryObject<EntityComponentType<CloseProximityAngryComponent, CloseProximityAngryComponent.Storage>> CLOSE_PROXIMITY_ANGRY = REGISTER.register("close_proximity_angry", () -> SimpleComponentType.of(CloseProximityAngryComponent.class, CloseProximityAngryComponent::new, CloseProximityAngryComponent.Storage::new));
    public static final RegistryObject<EntityComponentType<SoundStorageComponent, SoundStorageComponent.Storage>> SOUND_STORAGE = REGISTER.register("sound_storage", () -> SimpleComponentType.of(SoundStorageComponent.class, SoundStorageComponent::new, SoundStorageComponent.Storage::new));
    public static final RegistryObject<EntityComponentType<IdleActionComponent, IdleActionComponent.Storage>> IDLE_ACTION = REGISTER.register("idle_action", () -> SimpleComponentType.of(IdleActionComponent.class, IdleActionComponent::new, IdleActionComponent.Storage::new));
    public static final RegistryObject<EntityComponentType<CullSizeComponent, CullSizeComponent.Storage>> CULL_SIZE = REGISTER.register("cull_size", () -> SimpleComponentType.of(CullSizeComponent.class, CullSizeComponent::new, CullSizeComponent.Storage::new));
    public static final RegistryObject<EntityComponentType<ComponentRenderContext, ?>> RENDER_CONTEXT = REGISTER.register("component_render_context", () -> SimpleComponentType.of(ComponentRenderContext.class, ComponentRenderContext::new));

    public static final RegistryObject<EntityComponentType<GrowingComponent, GrowingComponent.Storage>> BLOCK_GROWING = REGISTER.register("block_growing", () -> SimpleComponentType.of(GrowingComponent.class, GrowingComponent::new, GrowingComponent.Storage::new));
    public static final RegistryObject<EntityComponentType<FlowerWorldgenComponent, FlowerWorldgenComponent.Storage>> FLOWER_WORLDGEN = REGISTER.register("flower_worldgen", () -> SimpleComponentType.of(FlowerWorldgenComponent.class, FlowerWorldgenComponent::new, FlowerWorldgenComponent.Storage::new ));
    public static final RegistryObject<EntityComponentType<BlockTouchEffectComponent, BlockTouchEffectComponent.Storage>> BLOCK_TOUCH_EFFECT = REGISTER.register("block_touch_effect", () -> SimpleComponentType.of(BlockTouchEffectComponent.class, BlockTouchEffectComponent::new, BlockTouchEffectComponent.Storage::new));
    public static final RegistryObject<EntityComponentType<BlockDropsComponent, BlockDropsComponent.Storage>> BLOCK_DROPS = REGISTER.register("block_drops", () -> SimpleComponentType.of(BlockDropsComponent.class, BlockDropsComponent::new, BlockDropsComponent.Storage::new));
    public static final RegistryObject<EntityComponentType<BlockPlaceableComponent, ?>> BLOCK_PLACEABLE = REGISTER.register("block_placeable", () -> SimpleComponentType.of(BlockPlaceableComponent.class, BlockPlaceableComponent::new));

    public static final RegistryObject<EntityComponentType<ItemRenderModelComponent, ItemRenderModelComponent.Storage>> ITEM_RENDER = REGISTER.register("item_render", () -> SimpleComponentType.of(ItemRenderModelComponent.class, ItemRenderModelComponent::new, ItemRenderModelComponent.Storage::new));
    public static final RegistryObject<EntityComponentType<ItemEatenComponent, ItemEatenComponent.Storage>> ITEM_EATEN = REGISTER.register("item_eaten", () -> SimpleComponentType.of(ItemEatenComponent.class, ItemEatenComponent::new, ItemEatenComponent.Storage::new));

    public static IForgeRegistry<EntityComponentType<?, ?>> getRegistry() {
        return REGISTRY.get();
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
