package net.dumbcode.dumblibrary.server.utils;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(modid = DumbLibrary.MODID)
public class TravisWorldTickListener {
    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        System.out.println(System.getenv("TRAVIS"));
        MinecraftForge.EVENT_BUS.unregister(TravisWorldTickListener.class);
    }
}
