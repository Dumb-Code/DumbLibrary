package net.dumbcode.dumblibrary.server.registry;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.IModBusEvent;

import java.util.concurrent.CompletableFuture;

//Fired after registries are created, but before the BLOCK registry event is fired.
public class PreBlockRegistryEvent extends Event implements IModBusEvent {
    public static class Pre extends  PreBlockRegistryEvent {}
    public static class Normal extends  PreBlockRegistryEvent {}
    public static class Post extends  PreBlockRegistryEvent {}
}
