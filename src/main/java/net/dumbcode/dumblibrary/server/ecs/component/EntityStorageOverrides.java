package net.dumbcode.dumblibrary.server.ecs.component;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.ecs.blocks.components.BlockPlaceableComponent;
import net.dumbcode.dumblibrary.server.ecs.blocks.storages.FlowerBlockPlaceableStorage;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DumbLibrary.MODID)
public class EntityStorageOverrides {
    //todo: move storage overrides to registry event ?

    public static EntityComponentType.StorageOverride<BlockPlaceableComponent, FlowerBlockPlaceableStorage> PLANT_PLACEABLE;

    @SubscribeEvent
    public static void onRegisterStorages(RegisterStoragesEvent event) {
        PLANT_PLACEABLE = event.register(EntityComponentTypes.BLOCK_PLACEABLE, "plant_placeable", FlowerBlockPlaceableStorage::new);
    }
}
