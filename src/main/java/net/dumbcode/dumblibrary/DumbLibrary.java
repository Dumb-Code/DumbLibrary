package net.dumbcode.dumblibrary;

import net.dumbcode.dumblibrary.client.model.TransformTypeModelLoader;
import net.dumbcode.dumblibrary.server.DumbGuiHandler;
import net.dumbcode.dumblibrary.server.guidebooks.GuidebooksManager;
import net.dumbcode.dumblibrary.server.network.S0SyncAnimation;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Logger;

@Mod(modid = DumbLibrary.MODID, name = DumbLibrary.NAME, version = DumbLibrary.VERSION)
@Mod.EventBusSubscriber(modid = DumbLibrary.MODID)
public class DumbLibrary
{
    public static final String MODID = "dumblibrary";
    public static final String NAME = "Dumb Library";
    public static final String VERSION = "0.2.0";

    private static Logger logger;

    @Mod.Instance(MODID)
    public static DumbLibrary MOD_INSTANCE;

    public static SimpleNetworkWrapper NETWORK = new SimpleNetworkWrapper(MODID);


    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new DumbGuiHandler());

        NETWORK.registerMessage(new S0SyncAnimation.Handler(), S0SyncAnimation.class, 0, Side.CLIENT);


        GuidebooksManager.createGuidebookFactories();

        if(FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            this.clientPreInit();
        }

    }

    @SideOnly(Side.CLIENT)
    private void clientPreInit() {
        ModelLoaderRegistry.registerLoader(TransformTypeModelLoader.INSTANCE);
    }

    public static Logger getLogger() {
        return logger;
    }
}
