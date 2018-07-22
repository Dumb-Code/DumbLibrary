package net.dumbcode.dumblibrary;

import net.dumbcode.dumblibrary.client.model.TransformTypeModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = DumbLibrary.MODID, name = DumbLibrary.NAME, version = DumbLibrary.VERSION)
@Mod.EventBusSubscriber(modid = DumbLibrary.MODID)
public class DumbLibrary
{
    public static final String MODID = "dumblibrary";
    public static final String NAME = "Dumb Library";
    public static final String VERSION = "0.0.2";

    private static Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        ModelLoaderRegistry.registerLoader(TransformTypeModelLoader.INSTANCE);
    }

    public static Logger getLogger() {
        return logger;
    }
}
