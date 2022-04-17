package net.dumbcode.dumblibrary.server.ecs.component.impl;

import net.dumbcode.dumblibrary.server.ai.GoalManager;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.FinalizableComponent;

public class GoalManagerComponent extends EntityComponent implements FinalizableComponent {

    public final GoalManager goalManager = new GoalManager();

    @Override
    public void finalizeComponent(ComponentAccess entity) {
        this.goalManager.attachTo(entity);
    }
}
