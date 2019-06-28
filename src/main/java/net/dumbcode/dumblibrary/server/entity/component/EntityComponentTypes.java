package net.dumbcode.dumblibrary.server.entity.component;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.entity.component.impl.*;
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
    public static final EntityComponentType<AnimationComponent, AnimationComponent.Storage> ANIMATION = InjectedUtils.injected();
    public static final EntityComponentType<ModelComponent, ?> MODEL = InjectedUtils.injected();
    public static final EntityComponentType<RenderAdjustmentsComponent, RenderAdjustmentsComponent.Storage> RENDER_ADJUSTMENTS = InjectedUtils.injected();

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
                SimpleComponentType.builder(AnimationComponent.class, AnimationComponent.Storage.class)
                        .withIdentifier(new ResourceLocation(DumbLibrary.MODID, "animation"))
                        .withConstructor(AnimationComponent::new)
                        .withStorage(AnimationComponent.Storage::new)
                        .build(),
                SimpleComponentType.builder(ModelComponent.class)
                        .withIdentifier(new ResourceLocation(DumbLibrary.MODID, "model"))
                        .withConstructor(ModelComponent::new)
                        .build(),
                SimpleComponentType.builder(RenderAdjustmentsComponent.class, RenderAdjustmentsComponent.Storage.class)
                        .withIdentifier(new ResourceLocation(DumbLibrary.MODID, "render_adjustments"))
                        .withConstructor(RenderAdjustmentsComponent::new)
                        .withStorage(RenderAdjustmentsComponent.Storage::new)
                        .build()
        );
    }
}
