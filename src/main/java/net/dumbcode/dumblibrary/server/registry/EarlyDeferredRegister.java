package net.dumbcode.dumblibrary.server.registry;

import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraft.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryManager;

import java.util.function.Consumer;

//An abstraction of the deferred register used to handle stuff that need to go before blocks and items.
public class EarlyDeferredRegister<T extends IForgeRegistryEntry<T>> extends WrappedDeferredRegister<T> {

    public static <T extends IForgeRegistryEntry<T>> EarlyDeferredRegister<T> create(Class<T> base, String modid) {
        return new EarlyDeferredRegister<T>(base, DeferredRegister.create(base, modid));
    }

    protected EarlyDeferredRegister(Class<T> base, DeferredRegister<T> register) {
        super(base, register);
    }

    public void register(IEventBus bus) {
        this.register.register(new EventBusDelegate(bus, ed -> new DelegateEventDispatcher<>(this.base, ed)));
    }

    @RequiredArgsConstructor
    public static class DelegateEventDispatcher<T extends IForgeRegistryEntry<T>> {
        private final Class<T> base;
        private final DeferredRegister.EventDispatcher delegate;

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public void onEarlyEvent(RegistryEvent.Register<Block> event) {
            IForgeRegistry<?> registry = GameRegistry.findRegistry(this.base);
            if(registry == null) {
                DumbLibrary.getLogger().error("Unable to find registry of type " + this.base);
                return;
            }
            this.delegate.handleEvent(new RegistryEvent.Register<>(registry.getRegistryName(), registry));
        }
    }


}
