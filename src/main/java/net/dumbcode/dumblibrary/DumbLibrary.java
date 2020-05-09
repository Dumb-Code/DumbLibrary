package net.dumbcode.dumblibrary;

import net.dumbcode.dumblibrary.server.DumbGuiHandler;
import net.dumbcode.dumblibrary.server.ItemComponent;
import net.dumbcode.dumblibrary.server.ecs.EntityManager;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.RegisterStoragesEvent;
import net.dumbcode.dumblibrary.server.ecs.item.ItemCompoundAccess;
import net.dumbcode.dumblibrary.server.ecs.item.components.ItemRenderModelComponent;
import net.dumbcode.dumblibrary.server.network.*;
import net.dumbcode.dumblibrary.server.registry.DumbRegistries;
import net.dumbcode.dumblibrary.server.registry.RegisterGeneticTypes;
import net.dumbcode.dumblibrary.server.utils.InjectedUtils;
import net.dumbcode.dumblibrary.server.utils.SidedExecutor;
import net.dumbcode.dumblibrary.server.utils.VoidStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
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
    public static final String VERSION = "0.5.2";

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
        MinecraftForge.EVENT_BUS.post(new RegisterGeneticTypes(DumbRegistries.GENETIC_TYPE_REGISTRY));
        ObjectHolderRegistry.INSTANCE.applyObjectHolders();

        logger = event.getModLog();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new DumbGuiHandler());

        NETWORK.registerMessage(new S0SyncAnimation.Handler(), S0SyncAnimation.class, 0, Side.CLIENT);
        NETWORK.registerMessage(new S1PlayItemCrackParticle.Handler(), S1PlayItemCrackParticle.class, 1, Side.CLIENT);
        NETWORK.registerMessage(new S2SyncComponent.Handler(), S2SyncComponent.class, 2, Side.CLIENT);
        NETWORK.registerMessage(new S3StopAnimation.Handler(), S3StopAnimation.class, 3, Side.CLIENT);

        NETWORK.registerMessage(new C4MoveSelectedSkeletalPart.Handler(), C4MoveSelectedSkeletalPart.class, 4, Side.SERVER);
        NETWORK.registerMessage(new S5UpdateSkeletalBuilder.Handler(), S5UpdateSkeletalBuilder.class, 5, Side.CLIENT);
        NETWORK.registerMessage(new C6SkeletalMovement.Handler(), C6SkeletalMovement.class, 6, Side.SERVER);
        NETWORK.registerMessage(new S7HistoryRecord.Handler(), S7HistoryRecord.class, 7, Side.CLIENT);
        NETWORK.registerMessage(new C8MoveInHistory.Handler(), C8MoveInHistory.class, 8, Side.SERVER);
        NETWORK.registerMessage(new S9UpdateHistoryIndex.Handler(), S9UpdateHistoryIndex.class, 9, Side.CLIENT);
        NETWORK.registerMessage(new S11FullPoseChange.Handler(), S11FullPoseChange.class, 11, Side.CLIENT);
        NETWORK.registerMessage(new C12FullPoseChange.Handler(), C12FullPoseChange.class, 12, Side.SERVER);

        NETWORK.registerMessage(new B13SplitNetworkPacket.Handler(), B13SplitNetworkPacket.class, 13, Side.CLIENT);
        NETWORK.registerMessage(new B13SplitNetworkPacket.Handler(), B13SplitNetworkPacket.class, 14, Side.SERVER);
        NETWORK.registerMessage(new B14ReleaseCollection.Handler(), B14ReleaseCollection.class, 15, Side.CLIENT);
        NETWORK.registerMessage(new B14ReleaseCollection.Handler(), B14ReleaseCollection.class, 16, Side.SERVER);

        CapabilityManager.INSTANCE.register(EntityManager.class, new VoidStorage<>(), EntityManager.Impl::new);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        SidedExecutor.runClient(() -> () -> {
            Minecraft.getMinecraft().getRenderItem().getItemModelMesher()
                .register(COMPONENT_ITEM, stack -> ModelLoader.getInventoryVariant(
                    ItemCompoundAccess.getAccess(stack)
                        .flatMap(EntityComponentTypes.ITEM_RENDER)
                        .map(ItemRenderModelComponent::getLocation)
                        .orElse(MODEL_MISSING)
                        .toString()
                    )
                );

        });
    }

    @SubscribeEvent
    public static void onItemRegister(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
            new ItemComponent().setRegistryName("component_item").setTranslationKey("component_item")
        );
    }

    public static Logger getLogger() {
        return logger;
    }


}
