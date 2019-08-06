package net.dumbcode.dumblibrary.server.registry;

import net.dumbcode.dumblibrary.server.animation.Animation;
import net.dumbcode.dumblibrary.server.animation.data.AnimationFactor;
import net.dumbcode.dumblibrary.server.entity.component.EntityComponentType;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

public class DumbRegistries {
    public static final IForgeRegistry<Animation> ANIMATION_REGISTRY = GameRegistry.findRegistry(Animation.class);
    public static final IForgeRegistry<EntityComponentType<?, ?>> COMPONENT_REGISTRY = GameRegistry.findRegistry(EntityComponentType.getWildcardType());
    public static final IForgeRegistry<AnimationFactor> FLOAT_SUPPLIER_REGISTRY = GameRegistry.findRegistry(AnimationFactor.class);

}
