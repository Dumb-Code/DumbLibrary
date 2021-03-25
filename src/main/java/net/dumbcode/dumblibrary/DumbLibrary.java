package net.dumbcode.dumblibrary;

import net.dumbcode.dumblibrary.client.model.ModelHandler;
import net.dumbcode.dumblibrary.server.ItemComponent;
import net.dumbcode.dumblibrary.server.ecs.EntityManager;
import net.dumbcode.dumblibrary.server.ecs.component.RegisterStoragesEvent;
import net.dumbcode.dumblibrary.server.network.*;
import net.dumbcode.dumblibrary.server.registry.DumbRegistries;
import net.dumbcode.dumblibrary.server.registry.RegisterGeneticTypes;
import net.dumbcode.dumblibrary.server.utils.InjectedUtils;
import net.dumbcode.dumblibrary.server.utils.VoidStorage;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolderRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(DumbLibrary.MODID)
@Mod.EventBusSubscriber(modid = DumbLibrary.MODID)
public class DumbLibrary {
    public static final String MODID = "dumblibrary";
    public static final String NAME = "Dumb Library";
    public static final String VERSION = "0.5.2";

    private static final Logger logger = LogManager.getLogger(MODID);

    private static final DeferredRegister<Item> DR = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final RegistryObject<Item> COMPONENT_ITEM = DR.register("component_item", ItemComponent::new);

    @CapabilityInject(EntityManager.class)
    public static final Capability<EntityManager> ENTITY_MANAGER = InjectedUtils.injected();

    private static final String PROTOCOL_VERSION = "1 debug";
    public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(MODID, "main"), () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals
    );

    public static final ResourceLocation MODEL_MISSING = new ResourceLocation("model_missing");

    public DumbLibrary() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;

        forgeBus.addListener(ModelHandler::onModelReady);

        DR.register(bus);
    }

    public void preInit(FMLCommonSetupEvent event) {
        IEventBus bus = MinecraftForge.EVENT_BUS;

        ObjectHolderRegistry.applyObjectHolders();
        bus.post(new RegisterStoragesEvent());
        bus.post(new RegisterGeneticTypes(DumbRegistries.GENETIC_TYPE_REGISTRY));
        ObjectHolderRegistry.applyObjectHolders();

        NETWORK.registerMessage(0, S0SyncAnimation.class, S0SyncAnimation::toBytes, S0SyncAnimation::fromBytes, S0SyncAnimation::handle);
        NETWORK.registerMessage(2, S2SyncComponent.class, S2SyncComponent::toBytes, S2SyncComponent::fromBytes, S2SyncComponent::handle);
        NETWORK.registerMessage(3, S3StopAnimation.class, S3StopAnimation::toBytes, S3StopAnimation::fromBytes, S3StopAnimation::handle);

        NETWORK.registerMessage(4, C4MoveSelectedSkeletalPart.class, C4MoveSelectedSkeletalPart::toBytes, C4MoveSelectedSkeletalPart::fromBytes, C4MoveSelectedSkeletalPart::handle);
        NETWORK.registerMessage(5, S5UpdateSkeletalBuilder.class, S5UpdateSkeletalBuilder::toBytes, S5UpdateSkeletalBuilder::fromBytes, S5UpdateSkeletalBuilder::handle);
        NETWORK.registerMessage(6, C6SkeletalMovement.class, C6SkeletalMovement::toBytes, C6SkeletalMovement::fromBytes, C6SkeletalMovement::handle);
        NETWORK.registerMessage(7, S7HistoryRecord.class, S7HistoryRecord::toBytes, S7HistoryRecord::fromBytes, S7HistoryRecord::handle);
        NETWORK.registerMessage(8, C8MoveInHistory.class, C8MoveInHistory::toBytes, C8MoveInHistory::fromBytes, C8MoveInHistory::handle);
        NETWORK.registerMessage(9, S9UpdateHistoryIndex.class, S9UpdateHistoryIndex::toBytes, S9UpdateHistoryIndex::fromBytes, S9UpdateHistoryIndex::handle);
        NETWORK.registerMessage(11, S11FullPoseChange.class, S11FullPoseChange::toBytes, S11FullPoseChange::fromBytes, S11FullPoseChange::handle);
        NETWORK.registerMessage(12, C12FullPoseChange.class, C12FullPoseChange::toBytes, C12FullPoseChange::fromBytes, C12FullPoseChange::handle);

        NETWORK.registerMessage(13, B13SplitNetworkPacket.class, B13SplitNetworkPacket::toBytes, B13SplitNetworkPacket::fromBytes, B13SplitNetworkPacket::handle);
        NETWORK.registerMessage(14, B14ReleaseCollection.class, B14ReleaseCollection::toBytes, B14ReleaseCollection::fromBytes, B14ReleaseCollection::handle);

        CapabilityManager.INSTANCE.register(EntityManager.class, new VoidStorage<>(), EntityManager.Impl::new);
    }

//    @Mod.EventHandler
//    public void init(FMLInitializationEvent event) {
//        SidedExecutor.runClient(() -> () -> {
//            Minecraft.getInstance().getItemRenderer().getItemModelShaper()
//                .register(COMPONENT_ITEM, stack -> ModelLoader.getInventoryVariant(
//                    ItemCompoundAccess.getAccess(stack)
//                        .flatMap(EntityComponentTypes.ITEM_RENDER)
//                        .map(ItemRenderModelComponent::getLocation)
//                        .orElse(MODEL_MISSING)
//                        .toString()
//                    )
//                );
//
//        });
//    }


    public static Logger getLogger() {
        return logger;
    }


}
