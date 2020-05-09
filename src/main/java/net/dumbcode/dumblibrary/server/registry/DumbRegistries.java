package net.dumbcode.dumblibrary.server.registry;

import net.dumbcode.dumblibrary.server.dna.GeneticType;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentType;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

public class DumbRegistries {
    public static final IForgeRegistry<EntityComponentType<?, ?>> COMPONENT_REGISTRY = GameRegistry.findRegistry(EntityComponentType.getWildcardType());

    public static final IForgeRegistry<GeneticType<?>> GENETIC_TYPE_REGISTRY = GameRegistry.findRegistry(GeneticType.getWildcardType());

}
