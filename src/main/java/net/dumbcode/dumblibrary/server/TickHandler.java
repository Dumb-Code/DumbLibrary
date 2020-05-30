package net.dumbcode.dumblibrary.server;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = DumbLibrary.MODID)
public class TickHandler {
    private static int ticks;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if(event.phase == TickEvent.Phase.START && FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            ticks++;
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if(event.phase == TickEvent.Phase.START && FMLCommonHandler.instance().getSide() == Side.SERVER) {
            ticks++;
        }
    }

    public static int getTicks() {
        return ticks;
    }
}
