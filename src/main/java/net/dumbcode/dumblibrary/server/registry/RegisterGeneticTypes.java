package net.dumbcode.dumblibrary.server.registry;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.dumbcode.dumblibrary.server.dna.GeneticType;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.registries.IForgeRegistry;

@Value
@EqualsAndHashCode(callSuper = false)
public class RegisterGeneticTypes extends Event {
    private final IForgeRegistry<GeneticType<?>> registry;
}
