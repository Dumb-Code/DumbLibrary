package net.dumbcode.dumblibrary.server.utils;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

//This is used so once travis loads the world it doesn't go on forever
@Mod.EventBusSubscriber(modid = DumbLibrary.MODID)
public class TravisWorldTickListener {
    public static final Minecraft MC = Minecraft.getMinecraft();
    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if("true".equals(System.getenv("TRAVIS"))) {
            FMLCommonHandler.instance().getMinecraftServerInstance().initiateShutdown();
        }
        MinecraftForge.EVENT_BUS.unregister(TravisWorldTickListener.class);
    }
}
