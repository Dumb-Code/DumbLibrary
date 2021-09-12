package net.dumbcode.dumblibrary.server.registry;

import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PreprocessRegisterDeferredRegister<T extends IForgeRegistryEntry<T>> extends WrappedDeferredRegister<T> {

    protected final List<Runnable> preRegistry = new ArrayList<>();

    public static <T extends IForgeRegistryEntry<T>> PreprocessRegisterDeferredRegister<T> create(Class<T> base, String modid) {
        return new PreprocessRegisterDeferredRegister<T>(base, DeferredRegister.create(base, modid));
    }

    protected PreprocessRegisterDeferredRegister(Class<T> base, DeferredRegister<T> register) {
        super(base, register);
    }

    @Override
    public void register(IEventBus bus) {
        super.register(new EventBusDelegate(bus, this::onRegister));
    }

    protected Object onRegister(DeferredRegister.EventDispatcher dispatcher) {
        return new DelegateEventDispatcher<>(this.base, this.preRegistry, dispatcher);
    }

    @RequiredArgsConstructor
    private static class DelegateEventDispatcher<T extends IForgeRegistryEntry<T>> {
        private final Class<T> base;
        private final List<Runnable> preRegistry;
        private final DeferredRegister.EventDispatcher delegate;

        @SubscribeEvent
        public void onRegistryEvent(RegistryEvent.Register<?> event) {
            if(event.getRegistry().getRegistrySuperType() != this.base) {
                return;
            }
            this.preRegistry.forEach(Runnable::run);
            this.delegate.handleEvent(event);
        }
    }

    public void beforeRegister(Runnable runnable) {
        this.preRegistry.add(runnable);
    }

    public <I> I beforeRegister(I i, Consumer<I> consumer) {
        this.beforeRegister(() -> consumer.accept(i));
        return i;
    }
}
