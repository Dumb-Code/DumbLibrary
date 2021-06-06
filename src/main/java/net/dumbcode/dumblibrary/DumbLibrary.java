package net.dumbcode.dumblibrary;

import net.dumbcode.dumblibrary.client.BakedModelResolver;
import net.dumbcode.dumblibrary.client.model.ModelHandler;
import net.dumbcode.dumblibrary.server.ItemComponent;
import net.dumbcode.dumblibrary.server.dna.GeneticTypes;
import net.dumbcode.dumblibrary.server.ecs.EntityManager;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.RegisterStoragesEvent;
import net.dumbcode.dumblibrary.server.network.*;
import net.dumbcode.dumblibrary.server.registry.DumbRegistries;
import net.dumbcode.dumblibrary.server.registry.DumbRegistryHandler;
import net.dumbcode.dumblibrary.server.registry.RegisterGeneticTypes;
import net.dumbcode.dumblibrary.server.taxidermy.BaseTaxidermyBlockEntity;
import net.dumbcode.dumblibrary.server.taxidermy.TaxidermyContainer;
import net.dumbcode.dumblibrary.server.utils.InjectedUtils;
import net.dumbcode.dumblibrary.server.utils.MouseUtils;
import net.dumbcode.dumblibrary.server.utils.VoidStorage;
import net.dumbcode.studio.model.ModelMirror;
import net.dumbcode.studio.model.RotationOrder;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolderRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

@Mod(DumbLibrary.MODID)
@Mod.EventBusSubscriber(modid = DumbLibrary.MODID)
public class DumbLibrary {
    public static final String MODID = "dumblibrary";
    public static final String NAME = "Dumb Library";
    public static final String VERSION = "0.5.2";

    private static final Logger logger = LogManager.getLogger(MODID);

    private static final DeferredRegister<Item> DRI = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final RegistryObject<Item> COMPONENT_ITEM = DRI.register("component_item", () -> new ItemComponent(new Item.Properties()));

    private static final DeferredRegister<ContainerType<?>> DRC = DeferredRegister.create(ForgeRegistries.CONTAINERS, MODID);
    public static final RegistryObject<ContainerType<TaxidermyContainer>> TAXIDERMY_CONTAINER = DRC.register("taxidermy_container", create((windowId, inv, data) -> {
        BlockPos blockPos = data.readBlockPos();
        TileEntity entity = inv.player.level.getBlockEntity(blockPos);
        if(entity instanceof BaseTaxidermyBlockEntity) {
            return new TaxidermyContainer((BaseTaxidermyBlockEntity) entity, windowId);
        }
        String teClazz = entity == null ? "@null" : entity.getClass().getSimpleName();
        throw new IllegalStateException("Illegal point, tried to open tracking beacon at " + blockPos + " but found tileentity of " + teClazz);
    }));

    @CapabilityInject(EntityManager.class)
    public static final Capability<EntityManager> ENTITY_MANAGER = InjectedUtils.injected();

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(MODID, "main"), () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals
    );

    public static final ResourceLocation MODEL_MISSING = new ResourceLocation("model_missing");

    public DumbLibrary() {
        RotationOrder.ZYX.applyAsGlobal();
        ModelMirror.XY.applyAsGlobal();

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;

        bus.addListener(ModelHandler::onModelReady);
        bus.addListener(BakedModelResolver::onTextureStitch);
        bus.addListener(BakedModelResolver::onModelBake);
        bus.addListener(this::preInit);
        bus.addListener(DumbRegistryHandler::onRegisteryRegister);
        bus.addListener(EntityComponentTypes::onRegisterComponents);

        forgeBus.addListener(GeneticTypes::onRegisterGenetics);
        forgeBus.addListener(EntityComponentTypes::registerSystems);
        forgeBus.addListener(MouseUtils::onMouseEvent);

        DRI.register(bus);
    }

    public void preInit(FMLCommonSetupEvent event) {
        IEventBus bus = MinecraftForge.EVENT_BUS;

        ObjectHolderRegistry.applyObjectHolders();
        bus.post(new RegisterStoragesEvent());
        bus.post(new RegisterGeneticTypes(DumbRegistries.GENETIC_TYPE_REGISTRY));
        ObjectHolderRegistry.applyObjectHolders();

        NETWORK.registerMessage(0, S2CSyncAnimation.class, S2CSyncAnimation::toBytes, S2CSyncAnimation::fromBytes, S2CSyncAnimation::handle);
        NETWORK.registerMessage(2, S2CSyncComponent.class, S2CSyncComponent::toBytes, S2CSyncComponent::fromBytes, S2CSyncComponent::handle);
        NETWORK.registerMessage(3, S2CStopAnimation.class, S2CStopAnimation::toBytes, S2CStopAnimation::fromBytes, S2CStopAnimation::handle);

        NETWORK.registerMessage(4, C2SMoveSelectedSkeletalPart.class, C2SMoveSelectedSkeletalPart::toBytes, C2SMoveSelectedSkeletalPart::fromBytes, C2SMoveSelectedSkeletalPart::handle);
        NETWORK.registerMessage(5, S2CUpdateSkeletalBuilder.class, S2CUpdateSkeletalBuilder::toBytes, S2CUpdateSkeletalBuilder::fromBytes, S2CUpdateSkeletalBuilder::handle);
        NETWORK.registerMessage(6, C2SSkeletalMovement.class, C2SSkeletalMovement::toBytes, C2SSkeletalMovement::fromBytes, C2SSkeletalMovement::handle);
        NETWORK.registerMessage(7, S2CHistoryRecord.class, S2CHistoryRecord::toBytes, S2CHistoryRecord::fromBytes, S2CHistoryRecord::handle);
        NETWORK.registerMessage(8, C2SMoveInHistory.class, C2SMoveInHistory::toBytes, C2SMoveInHistory::fromBytes, C2SMoveInHistory::handle);
        NETWORK.registerMessage(9, S2CUpdateHistoryIndex.class, S2CUpdateHistoryIndex::toBytes, S2CUpdateHistoryIndex::fromBytes, S2CUpdateHistoryIndex::handle);
        NETWORK.registerMessage(11, S2CFullPoseChange.class, S2CFullPoseChange::toBytes, S2CFullPoseChange::fromBytes, S2CFullPoseChange::handle);
        NETWORK.registerMessage(12, C2SFullPoseChange.class, C2SFullPoseChange::toBytes, C2SFullPoseChange::fromBytes, C2SFullPoseChange::handle);

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

    private static <T extends Container> Supplier<ContainerType<T>> create(IContainerFactory<T> factory) {
        return () -> new ContainerType<>(factory);
    }

}
