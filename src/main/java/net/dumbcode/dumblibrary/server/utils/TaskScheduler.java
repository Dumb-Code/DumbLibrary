package net.dumbcode.dumblibrary.server.utils;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.Validate;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

@Mod.EventBusSubscriber(modid = DumbLibrary.MODID)
public class TaskScheduler {
    private static final Queue<Scheduled> tasksToAdd = new ArrayDeque<>();
    private static final List<Scheduled> scheduledTasks = new ArrayList<>();

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void clientUpdate(TickEvent.ClientTickEvent event) {
        if(event.phase == TickEvent.Phase.START) {
            update(Minecraft.getInstance().level);
        }
    }

    @SubscribeEvent
    public static void worldUpdate(TickEvent.WorldTickEvent event) {
        if(event.phase == TickEvent.Phase.START) {
            update(event.world);
        }
    }

    public static void update(World world) {
        if(world == null || (!world.isClientSide && SidedExecutor.CLIENT)) {
            return;
        }
        synchronized (tasksToAdd) {
            while (!tasksToAdd.isEmpty()) {
                scheduledTasks.add(tasksToAdd.poll());
            }
        }
        scheduledTasks.removeIf(t -> {
            if(t.ticksLeft-- == 0) {
                try {
                    t.task.run();
                    t.task.get();
                } catch (InterruptedException | ExecutionException e) {
                    DumbLibrary.getLogger().fatal("Error executing task", e);
                }
                return true;
            }
            return false;
        });
    }

    public static <V> ListenableFuture<V> addTask(Callable<V> task, int ticks) {
        Validate.notNull(task);
        ListenableFutureTask<V> future = ListenableFutureTask.create(task);
        synchronized (tasksToAdd) {
            tasksToAdd.add(new Scheduled(future, ticks));
            return future;
        }
    }

    public static ListenableFuture<?> addTask(Runnable task, int ticks) {
        Validate.notNull(task);
        return addTask(Executors.callable(task), ticks);
    }


    @Data
    @AllArgsConstructor
    private static class Scheduled {
        private final FutureTask<?> task;
        private int ticksLeft;
    }
}
