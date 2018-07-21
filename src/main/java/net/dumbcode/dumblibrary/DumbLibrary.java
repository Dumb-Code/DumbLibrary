package net.dumbcode.dumblibrary;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = net.dumbcode.dumblibrary.DumbLibrary.MODID, name = net.dumbcode.dumblibrary.DumbLibrary.NAME, version = net.dumbcode.dumblibrary.DumbLibrary.VERSION)
public class DumbLibrary
{
    public static final String MODID = "dumblibrary";
    public static final String NAME = "Dumb Library";
    public static final String VERSION = "0.0.2";

    private static Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
    }

    public static Logger getLogger() {
        return logger;
    }
}
