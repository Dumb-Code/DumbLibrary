package net.dumbcode.dumblibrary.server.registry;

import lombok.RequiredArgsConstructor;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

//An abstraction of the deferred register that allows easy extension
public abstract class WrappedDeferredRegister<T extends IForgeRegistryEntry<T>> {
    protected final Class<T> base;
    protected final DeferredRegister<T> register;

    protected WrappedDeferredRegister(Class<T> base, DeferredRegister<T> register) {
        this.base = base;
        this.register = register;
    }

    public Collection<RegistryObject<T>> getEntries() {
        return this.register.getEntries();
    }

    public <I extends T> RegistryObject<I> register(String name, Supplier<? extends I> sup) {
        return this.register.register(name, sup);
    }

    public Supplier<IForgeRegistry<T>> makeRegistry(String name, Supplier<RegistryBuilder<T>> sup) {
        return this.register.makeRegistry(name, sup);
    }


    public void register(IEventBus bus) {
        this.register.register(bus);
    }


    @RequiredArgsConstructor
    protected static class EventBusDelegate implements IEventBus {
        private final IEventBus bus;
        private final Function<DeferredRegister.EventDispatcher, Object> onRegister;

        @Override
        public void register(Object target) {
            if (target instanceof DeferredRegister.EventDispatcher) {
                this.bus.register(this.onRegister.apply((DeferredRegister.EventDispatcher) target));
            } else {
                this.bus.register(target);
            }
        }

        //Delegates as @Delegate throws a weird error.
        public <E extends Event> void addListener(Consumer<E> consumer) {
            this.bus.addListener(consumer);
        }

        public <E extends Event> void addListener(EventPriority priority, Consumer<E> consumer) {
            this.bus.addListener(priority, consumer);
        }

        public <E extends Event> void addListener(EventPriority priority, boolean receiveCancelled, Consumer<E> consumer) {
            this.bus.addListener(priority, receiveCancelled, consumer);
        }

        public <E extends Event> void addListener(EventPriority priority, boolean receiveCancelled, Class<E> eventType, Consumer<E> consumer) {
            this.bus.addListener(priority, receiveCancelled, eventType, consumer);
        }

        public <E extends GenericEvent<? extends F>, F> void addGenericListener(Class<F> genericClassFilter, Consumer<E> consumer) {
            this.bus.addGenericListener(genericClassFilter, consumer);
        }

        public <E extends GenericEvent<? extends F>, F> void addGenericListener(Class<F> genericClassFilter, EventPriority priority, Consumer<E> consumer) {
            this.bus.addGenericListener(genericClassFilter, priority, consumer);
        }

        public <E extends GenericEvent<? extends F>, F> void addGenericListener(Class<F> genericClassFilter, EventPriority priority, boolean receiveCancelled, Consumer<E> consumer) {
            this.bus.addGenericListener(genericClassFilter, priority, receiveCancelled, consumer);
        }

        public <E extends GenericEvent<? extends F>, F> void addGenericListener(Class<F> genericClassFilter, EventPriority priority, boolean receiveCancelled, Class<E> eventType, Consumer<E> consumer) {
            this.bus.addGenericListener(genericClassFilter, priority, receiveCancelled, eventType, consumer);
        }

        public void unregister(Object object) {
            this.bus.unregister(object);
        }

        public boolean post(Event event) {
            return this.bus.post(event);
        }

        public boolean post(Event event, IEventBusInvokeDispatcher wrapper) {
            return this.bus.post(event, wrapper);
        }

        public void shutdown() {
            this.bus.shutdown();
        }

        public void start() {
            this.bus.start();
        }
    }
}
