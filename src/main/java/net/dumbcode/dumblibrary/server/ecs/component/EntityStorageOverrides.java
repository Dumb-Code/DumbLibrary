package net.dumbcode.dumblibrary.server.ecs.component;

import net.dumbcode.dumblibrary.server.ecs.blocks.components.BlockPlaceableComponent;
import net.dumbcode.dumblibrary.server.ecs.blocks.storages.FlowerBlockPlaceableStorage;

public class EntityStorageOverrides {
    //todo: move storage overrides to registry event ?

    public static EntityComponentType.StorageOverride<BlockPlaceableComponent, FlowerBlockPlaceableStorage> PLANT_PLACEABLE;

    public static void onRegisterStorages() {
        PLANT_PLACEABLE = EntityComponentType.registerStorageOverride(EntityComponentTypes.BLOCK_PLACEABLE.get(), "plant_placeable", FlowerBlockPlaceableStorage::new);
    }
}
