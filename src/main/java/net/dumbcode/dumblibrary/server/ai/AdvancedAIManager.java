package net.dumbcode.dumblibrary.server.ai;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.EntityLiving;

import java.util.Comparator;
import java.util.List;
import java.util.Queue;

@Getter
@Setter
public class AdvancedAIManager {

    private final List<AdvancedAIBase> tasks = Lists.newLinkedList();
    private final Queue<AdvancedAIBase> currentTasks = Queues.newConcurrentLinkedQueue();
    private final EntityLiving entity;
    private final int updateRate = 20;

    public AdvancedAIManager(EntityLiving entity) {
        this.entity = entity;
    }

    /**
     * Updates and sorts the tasks every second to see if a new
     * current task can be added
     */
    public void update() {
        if (this.entity.ticksExisted % updateRate == 0) {
            tasks.forEach(AdvancedAIBase::checkImportance);
            this.sortTasks();
            for (AdvancedAIBase task : tasks) {
                if (task.isUsesCooldown()) {
                    task.tickCooldown();
                }
                if (task.shouldExecute() && currentTasks.isEmpty()) {
                    currentTasks.add(task);
                    task.execute();
                } else if (task.shouldExecute() && !currentTasks.isEmpty()) {
                    if (currentTasks.peek().isConcurrent() && (task.isConcurrent() || task.getType().equals(AIType.ANIMATION))) {
                        currentTasks.add(task);
                        task.execute();
                    }
                }
            }
            if (!currentTasks.isEmpty()) {
                for (AdvancedAIBase task : currentTasks) {
                    if (task.isFinished()) {
                        currentTasks.remove(task);
                    } else {
                        if (task.shouldContinue()) {
                            task.execute();
                        }
                        if (task.isUpdatable()) {
                            task.update();
                        }
                    }
                }
            }
        }
    }

    /**
     * Sorts the tasks by importance
     */
    private void sortTasks() {
        tasks.sort(Comparator.comparing(AdvancedAIBase::getImportance).reversed());
    }
}