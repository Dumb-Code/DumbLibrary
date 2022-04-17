package net.dumbcode.dumblibrary.server.ai;

import lombok.var;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.EntityGoalSupplier;

import java.util.*;
import java.util.stream.Collectors;

public class GoalManager {
    private final List<EntityGoal> goals = new ArrayList<>();

    private EntityGoal selectedGoal;

    public void attachTo(ComponentAccess entity) {
        this.goals.clear();
        this.setGoal(null);

        entity.getComponentAdditionals(EntityGoalSupplier.class).addGoals(this, this.goals::add, entity);
    }

    public void tick() {
        this.searchForNewGoal();
        if(this.selectedGoal != null) {
            this.selectedGoal.tick();
        }
    }

    public void goalFinished() {
        this.setGoal(null);
        this.searchForNewGoal();
    }

    private void searchForNewGoal() {
        Map<EntityGoal, Double> currentlyAllowedValues = new HashMap<>();
        for (EntityGoal goal : this.goals) {
            goal.getImportance().ifPresent(imp -> currentlyAllowedValues.put(goal, imp));
        }
        List<Map.Entry<EntityGoal, Double>> sortedEntries = currentlyAllowedValues.entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .collect(Collectors.toList());
        Collections.reverse(sortedEntries);

        for(var entry : sortedEntries) {
            EntityGoal goal = entry.getKey();
            double importance = entry.getValue();
            if(this.selectedGoal == null || this.selectedGoal.canBeFinishedBy(importance)) {
                this.setGoal(goal);
                if(this.selectedGoal != null) {
                    break;
                }
            }
        }
    }

    private void setGoal(EntityGoal goal) {
        if(this.selectedGoal != null) {
            this.selectedGoal.onFinished();
        }

        if(goal != null && !goal.onStarted()) {
            goal = null;
        }

        this.selectedGoal = goal;
    }

}
