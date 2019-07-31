package net.dumbcode.dumblibrary.server.utils;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.UUID;

public class WorldUtils {
    public static Optional<Entity> getEntityFromUUID(World world, UUID uuid) {
        for (Entity entity : world.loadedEntityList) {
            if(entity.getUniqueID().equals(uuid)) {
                return Optional.of(entity);
            }
        }
        return Optional.empty();
    }
}
