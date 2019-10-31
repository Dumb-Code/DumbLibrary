package net.dumbcode.dumblibrary.server.dna;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentType;
import net.minecraft.entity.Entity;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.function.BiConsumer;

@Getter
@RequiredArgsConstructor(staticName = "create")
public class GeneticType extends IForgeRegistryEntry.Impl<GeneticType> {
    private final BiConsumer<ComponentAccess, Float> onChange;

    public static <T extends EntityComponent> GeneticType createForComponent(EntityComponentType<T, ?> type, BiConsumer<T, Float> consumer) {
        return create((access, value) -> access.get(type).ifPresent(t -> consumer.accept(t, value)));
    }
}
