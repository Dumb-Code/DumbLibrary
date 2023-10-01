package net.dumbcode.dumblibrary.server.ecs.component;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.IModBusEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

//Markes that stoarges should now be registered.
public class RegisterStorageOverridesEvent extends Event implements IModBusEvent {
    public static void postOnBus() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addGenericListener(Block.class, EventPriority.HIGH, (RegistryEvent.Register<Block> event) -> bus.post(new RegisterStorageOverridesEvent()));
    }
}
