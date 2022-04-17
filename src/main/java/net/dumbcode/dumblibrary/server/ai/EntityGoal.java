package net.dumbcode.dumblibrary.server.ai;

import java.util.OptionalDouble;
import java.util.Random;

public abstract class EntityGoal {

    protected static final Random RANDOM = new Random();

    private final GoalManager manager;

    public EntityGoal(GoalManager manager) {
        this.manager = manager;
    }

    protected abstract void tick();

    protected abstract OptionalDouble getImportance();

    protected final void finish() {
        this.manager.goalFinished();
    }

    public boolean canBeFinishedBy(double otherImportance) {
        return false;
    }

    public boolean onStarted() {
        return true;
    }

    public void onFinished() {

    }
}
