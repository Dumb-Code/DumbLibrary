package net.dumbcode.dumblibrary;

import net.dumbcode.dumblibrary.client.model.TransformTypeModelLoader;
import net.dumbcode.dumblibrary.server.DumbGuiHandler;
import net.dumbcode.dumblibrary.server.guidebooks.GuidebooksManager;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.Logger;

@Mod(modid = DumbLibrary.MODID, name = DumbLibrary.NAME, version = DumbLibrary.VERSION)
@Mod.EventBusSubscriber(modid = DumbLibrary.MODID)
public class DumbLibrary
{
    public static final String MODID = "dumblibrary";
    public static final String NAME = "Dumb Library";
    public static final String VERSION = "0.1.6";

    private static Logger logger;

    @Mod.Instance(MODID)
    public static DumbLibrary MOD_INSTANCE;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        ModelLoaderRegistry.registerLoader(TransformTypeModelLoader.INSTANCE);
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new DumbGuiHandler());

        GuidebooksManager.createGuidebookFactories();
    }

    public static Logger getLogger() {
        return logger;
    }
}
