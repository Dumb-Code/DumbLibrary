package net.dumbcode.dumblibrary.server.ecs.component.additionals;

import net.minecraft.entity.EntityLivingBase;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface GatherEnemiesComponent {
    void gatherEnemyPredicates(Consumer<Predicate<EntityLivingBase>> registry);
}
