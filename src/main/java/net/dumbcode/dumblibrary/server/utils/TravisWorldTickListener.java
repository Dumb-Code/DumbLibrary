package net.dumbcode.dumblibrary.server.utils;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

//This is used so once travis loads the world it doesn't go on forever
@Mod.EventBusSubscriber(modid = DumbLibrary.MODID)
public class TravisWorldTickListener {

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if("true".equals(System.getenv("TRAVIS"))) {
            DumbLibrary.getLogger().info("Dumb Library has detected that this server is being run on a travis server. We will now exit");
            FMLCommonHandler.instance().getMinecraftServerInstance().initiateShutdown();
        }
        MinecraftForge.EVENT_BUS.unregister(TravisWorldTickListener.class);
    }
}
