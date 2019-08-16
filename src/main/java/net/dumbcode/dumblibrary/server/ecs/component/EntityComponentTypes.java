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
    public static final EntityComponentType<FlowerWorldgenComponent, FlowerWorldgenComponent.Storage> FLOWER_WORLDGEN = InjectedUtils.injected();
    public static final EntityComponentType<BlockTouchEffectComponent, BlockTouchEffectComponent.Storage> BLOCK_TOUCH_EFFECT = InjectedUtils.injected();
    public static final EntityComponentType<BlockDropsComponent, BlockDropsComponent.Storage> BLOCK_DROPS = InjectedUtils.injected();
    public static final EntityComponentType<BlockPlaceableComponent, ?> BLOCK_PLACEABLE = InjectedUtils.injected();

    public static final EntityComponentType<ItemRenderModelComponent, ItemRenderModelComponent.Storage> ITEM_RENDER = InjectedUtils.injected();
    public static final EntityComponentType<ItemEatenComponent, ItemEatenComponent.Storage> ITEM_EATEN = InjectedUtils.injected();

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

    @SubscribeEvent
    public static void register(RegisterSystemsEvent event) {
        event.registerSystem(MetabolismSystem.INSTANCE);
        event.registerSystem(HerdSystem.INSTANCE);
        event.registerSystem(AnimationSystem.INSTANCE);
        event.registerSystem(ItemDropSystem.INSTANCE);
        event.registerSystem(SpeedTrackingSystem.INSTANCE);

        event.registerSystem(GrowingSystem.INSTANCE);
        event.registerSystem(FlowerWorldgenSystem.INSTANCE);
        event.registerSystem(BlockTouchEffectSystem.INSTANCE);
        event.registerSystem(BlockDropsSystem.INSTANCE);
        event.registerSystem(BlockPlacementSystem.INSTANCE);

        event.registerSystem(ItemEatenSystem.INSTANCE);

    }
}
