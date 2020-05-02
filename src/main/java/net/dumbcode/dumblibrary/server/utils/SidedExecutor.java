package net.dumbcode.dumblibrary.server.utils;

import net.minecraftforge.fml.relauncher.FMLLaunchHandler;

import java.util.function.Supplier;

public class SidedExecutor {

    private SidedExecutor() {
    }

    public static final boolean CLIENT = FMLLaunchHandler.side().isClient();

    public static void runClient(Supplier<Runnable> clientRun) {
        runSided(clientRun, () -> () -> {});
    }

    public static void runServer(Supplier<Runnable> serverRun) {
        runSided(() -> () -> {}, serverRun);
    }

    public static void runSided(Supplier<Runnable> clientRun, Supplier<Runnable> serverRun) {
        (CLIENT ? clientRun : serverRun).get().run();
    }




    public static <T> T getClient(Supplier<Supplier<T>> clientRun, T serverValue) {
        return getSided(clientRun, () -> () -> serverValue);
    }

    public static <T> T getServer(Supplier<Supplier<T>> serverRun, T clientValue) {
        return getSided(() -> () -> clientValue, serverRun);
    }

    public static <T> T getSided(Supplier<Supplier<T>> clientRun, Supplier<Supplier<T>> serverRun) {
        return (CLIENT ? clientRun : serverRun).get().get();
    }

}
