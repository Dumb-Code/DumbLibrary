package net.dumbcode.dumblibrary.server.utils;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.Validate;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

@Mod.EventBusSubscriber(modid = DumbLibrary.MODID)
public class TaskScheduler {
    private static final Queue<Scheduled> tasksToAdd = new ArrayDeque<>();
    private static final List<Scheduled> scheduledTasks = new ArrayList<>();

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void clientUpdate(TickEvent.ClientTickEvent event) {
        update(Minecraft.getMinecraft().world);
    }

    @SubscribeEvent
    public static void worldUpdate(TickEvent.WorldTickEvent event) {
        update(event.world);
    }

    public static void update(World world) {
        if(world == null || (!world.isRemote && SidedExecutor.CLIENT)) {
            return;
        }
        synchronized (tasksToAdd) {
            while (!tasksToAdd.isEmpty()) {
                scheduledTasks.add(tasksToAdd.poll());
            }
        }
        scheduledTasks.removeIf(t -> {
            if(t.ticksLeft-- == 0) {
                Util.runTask(t.task, DumbLibrary.getLogger());
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
