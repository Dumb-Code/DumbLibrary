package net.dumbcode.dumblibrary;

import net.dumbcode.dumblibrary.client.model.TransformTypeModelLoader;
import net.dumbcode.dumblibrary.server.DumbGuiHandler;
import net.dumbcode.dumblibrary.server.ecs.EntityManager;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.RegisterStoragesEvent;
import net.dumbcode.dumblibrary.server.ecs.item.ItemCompoundAccess;
import net.dumbcode.dumblibrary.server.ecs.item.components.ItemRenderModelComponent;
import net.dumbcode.dumblibrary.server.network.S0SyncAnimation;
import net.dumbcode.dumblibrary.server.network.S1PlayItemCrackParticle;
import net.dumbcode.dumblibrary.server.network.S2SyncComponent;
import net.dumbcode.dumblibrary.server.utils.InjectedUtils;
import net.dumbcode.dumblibrary.server.utils.SidedExecutor;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.ObjectHolderRegistry;
import org.apache.logging.log4j.Logger;

@Mod(modid = DumbLibrary.MODID, name = DumbLibrary.NAME, version = DumbLibrary.VERSION)
@Mod.EventBusSubscriber(modid = DumbLibrary.MODID)
public class DumbLibrary {
    public static final String MODID = "dumblibrary";
    public static final String NAME = "Dumb Library";
    public static final String VERSION = "0.3.0";

    private static Logger logger;

    @Mod.Instance(MODID)
    public static DumbLibrary MOD_INSTANCE;

    @GameRegistry.ObjectHolder("dumblibrary:component_item")
    public static final Item COMPONENT_ITEM = InjectedUtils.injected();


    @CapabilityInject(EntityManager.class)
    public static final Capability<EntityManager> ENTITY_MANAGER = InjectedUtils.injected();

    public static final SimpleNetworkWrapper NETWORK = new SimpleNetworkWrapper(MODID);

    public static final ResourceLocation MODEL_MISSING = new ResourceLocation("model_missing");

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ObjectHolderRegistry.INSTANCE.applyObjectHolders();
        MinecraftForge.EVENT_BUS.post(new RegisterStoragesEvent());

        logger = event.getModLog();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new DumbGuiHandler());

        NETWORK.registerMessage(new S0SyncAnimation.Handler(), S0SyncAnimation.class, 0, Side.CLIENT);
        NETWORK.registerMessage(new S1PlayItemCrackParticle.Handler(), S1PlayItemCrackParticle.class, 1, Side.CLIENT);
        NETWORK.registerMessage(new S2SyncComponent.Handler(), S2SyncComponent.class, 2, Side.CLIENT);

        SidedExecutor.runClient(() -> () -> ModelLoaderRegistry.registerLoader(TransformTypeModelLoader.INSTANCE));
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        SidedExecutor.runClient(() -> () ->
                Minecraft.getMinecraft().getRenderItem().getItemModelMesher()
                        .register(COMPONENT_ITEM, stack -> ModelLoader.getInventoryVariant(
                                ItemCompoundAccess.getAccess(stack)
                                        .flatMap(EntityComponentTypes.ITEM_RENDER)
                                        .map(ItemRenderModelComponent::getLocation)
                                        .orElse(MODEL_MISSING)
                                        .toString()
                                )
                        ));
    }


    public static Logger getLogger() {
        return logger;
    }


}
