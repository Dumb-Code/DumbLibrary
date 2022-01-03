package net.dumbcode.dumblibrary.server.utils;

import net.minecraft.client.renderer.chunk.ChunkRenderCache;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.BiPredicate;

public class BlockStateWorker {
    public static final BlockStateWorker INSTANCE = new BlockStateWorker(1000, 10);

    private static int counter;

    private final Thread workerThread;
    private final int iterationsPerRun;
    private final int minIterations;
    private final Queue<Task> newTasks = new ArrayDeque<>();
    private final List<Task> tasks = new ArrayList<>();

    public BlockStateWorker(int iterationsPerRun, int minItertations) {
        this.workerThread = new Thread(this::run, "BlockStateWorker-" + counter++);
        this.workerThread.setDaemon(true);
        this.workerThread.start();
        this.iterationsPerRun = iterationsPerRun;
        this.minIterations = minItertations;
    }

    private void run() {
        while(!this.workerThread.isInterrupted()) {
            synchronized (this.newTasks) {
                while (!this.newTasks.isEmpty()) {
                    this.tasks.add(this.newTasks.poll());
                }
            }
            synchronized (this.tasks) {
                if(!this.tasks.isEmpty()) {
                    int iterationsPerTask = Math.max(this.iterationsPerRun / this.tasks.size(), this.minIterations);
                    this.tasks.removeIf(t -> {
                        t.runTask(iterationsPerTask);
                        if(t.finished) {
                            t.completableFuture.complete(t.result);
                            return true;
                        }
                        return false;
                    });
                }
            }
        }
    }

    public Future<List<BlockPos>> runTask(World world, BlockPos center, int radii, BiPredicate<IBlockDisplayReader, BlockPos> predicate) {
        return this.runTask(world, center, radii, radii, radii, predicate);
    }

    public Future<List<BlockPos>> runTask(World world, BlockPos center, int xRadii, int yRadii, int zRadii, BiPredicate<IBlockDisplayReader, BlockPos> predicate) {
        CompletableFuture<List<BlockPos>> future = new CompletableFuture<>();
        synchronized (this.newTasks) {
            this.newTasks.add(new Task(world, center, xRadii, yRadii, zRadii, predicate, future));
        }
        return future;
    }


    private static class Task {
        private final ChunkRenderCache world;
        private final BiPredicate<IBlockDisplayReader, BlockPos> predicate;
        private final CompletableFuture<List<BlockPos>> completableFuture;
        private final BlockPos min;
        private final BlockPos max;
        private final List<BlockPos> result = new ArrayList<>();

        private boolean finished;
        private int x;
        private int y;
        private int z;

        private Task(World world, BlockPos center, int xRadii, int yRadii, int zRadii, BiPredicate<IBlockDisplayReader, BlockPos> predicate, CompletableFuture<List<BlockPos>> completableFuture) {
            this.predicate = predicate;
            this.completableFuture = completableFuture;

            this.min = center.offset(-xRadii, -yRadii, -zRadii);
            this.max = center.offset(xRadii, yRadii, zRadii);

            this.world = ChunkRenderCache.createIfNotEmpty(world, this.min.offset(-1, -1, -1), this.max.offset(1, 1, 1), 1);

            this.x = this.min.getX();
            this.y = this.min.getY();
            this.z = this.min.getZ();
        }

        private void runTask(int iterations) {
            for (int i = 0; i < iterations; i++) {
                BlockPos pos = new BlockPos(this.x, this.y, this.z);
                if(this.predicate.test(this.world, pos)) {
                    this.result.add(pos);
                }
                if(this.increment()) {
                    this.finished = true;
                }
            }
        }

        private boolean increment() {
            this.x++;
            if(this.x > this.max.getX()) {
                this.x = this.min.getX();
                this.y++;
                if(this.y > this.max.getY()) {
                    this.y = this.min.getY();
                    this.z++;
                    return this.z > this.max.getZ();
                }
            }
            return false;
        }
    }
}
