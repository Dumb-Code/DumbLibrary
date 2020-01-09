package net.dumbcode.dumblibrary.server.ecs.component.additionals;

import net.dumbcode.dumblibrary.server.dna.GeneticEntry;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;

import java.util.function.Consumer;

public interface GatherGeneticsComponent {
    void gatherGenetics(ComponentAccess entity, Consumer<GeneticEntry> registry);
}
