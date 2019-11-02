package net.dumbcode.dumblibrary.server.ecs.component.additionals;

import net.dumbcode.dumblibrary.server.dna.GeneticEntry;

import java.util.function.Consumer;

public interface GatherGeneticsComponent {
    void gatherGenetics(Consumer<GeneticEntry> registry);
}
