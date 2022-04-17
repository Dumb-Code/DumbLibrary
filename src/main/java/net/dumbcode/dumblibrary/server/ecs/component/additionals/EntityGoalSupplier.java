package net.dumbcode.dumblibrary.server.ecs.component.additionals;

import net.dumbcode.dumblibrary.server.ai.EntityGoal;
import net.dumbcode.dumblibrary.server.ai.GoalManager;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;

import java.util.function.Consumer;

public interface EntityGoalSupplier {
    void addGoals(GoalManager manager, Consumer<EntityGoal> consumer, ComponentAccess access);
}
