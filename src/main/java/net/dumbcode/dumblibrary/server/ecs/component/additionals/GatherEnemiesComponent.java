package net.dumbcode.dumblibrary.server.ecs.component.additionals;

import net.minecraft.entity.LivingEntity;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface GatherEnemiesComponent {
    void gatherEnemyPredicates(Consumer<Predicate<LivingEntity>> registry);
}
