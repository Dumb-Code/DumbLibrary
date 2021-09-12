package net.dumbcode.dumblibrary.server.registry;

import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.List;

public class PostEarlyDeferredRegister<T extends IForgeRegistryEntry<T>> extends PreprocessRegisterDeferredRegister<T> {

    public static <T extends IForgeRegistryEntry<T>> PostEarlyDeferredRegister<T> create(Class<T> base, String modid) {
        return new PostEarlyDeferredRegister<T>(base, DeferredRegister.create(base, modid));
    }

    protected PostEarlyDeferredRegister(Class<T> base, DeferredRegister<T> register) {
        super(base, register);
    }

    @Override
    protected Object onRegister(DeferredRegister.EventDispatcher dispatcher) {
        return new DelegateEventDispatcher<>(this.base, this.preRegistry, dispatcher);
    }

    @RequiredArgsConstructor
    private static class DelegateEventDispatcher<T extends IForgeRegistryEntry<T>> {
        private final Class<T> base;
        private final List<Runnable> preRegistry;
        private final DeferredRegister.EventDispatcher delegate;

        @SubscribeEvent
        public void onRegistryEvent(PreBlockRegistryEvent.Post event) {
            IForgeRegistry<?> registry = GameRegistry.findRegistry(this.base);
            if(registry == null) {
                DumbLibrary.getLogger().error("Unable to find registry of type " + this.base);
                return;
            }
            this.preRegistry.forEach(Runnable::run);
            this.delegate.handleEvent(new RegistryEvent.Register<>(registry.getRegistryName(), registry));
        }
    }
}
