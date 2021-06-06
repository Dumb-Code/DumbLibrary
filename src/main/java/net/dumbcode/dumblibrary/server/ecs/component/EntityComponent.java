package net.dumbcode.dumblibrary.server.ecs.component;

import io.netty.buffer.Unpooled;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.network.S2CSyncComponent;
import net.dumbcode.dumblibrary.server.registry.DumbRegistries;
import net.dumbcode.dumblibrary.server.utils.TaskScheduler;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;

public abstract class EntityComponent {

    protected Runnable syncer;
    protected EntityComponentType type;
    @Nullable private SaveableEntityStorage storage;
    @Nullable private String storageID;

    public CompoundNBT serialize(CompoundNBT compound) {
        if(this.storage != null) {
            CompoundNBT storageTag = new CompoundNBT();
            if(this.storageID != null) {
                storageTag.putString("storage_id", this.storageID);
            }
            this.storage.writeNBT(storageTag);
            compound.put("storage", storageTag);
        }
        return compound;
    }

    public void deserialize(CompoundNBT compound) {
        ResourceLocation identifier = new ResourceLocation(compound.getString("identifier"));
        this.type = DumbRegistries.COMPONENT_REGISTRY.getValue(identifier);
        if(compound.contains("storage", Constants.NBT.TAG_COMPOUND)) {
            CompoundNBT storageTag = compound.getCompound("storage");
            if(storageTag.contains("storage_id", Constants.NBT.TAG_STRING)) {
                String tagStorageId = storageTag.getString("storage_id");
                EntityComponentType.StorageOverride override = EntityComponentType.StorageOverride.overrides.get(this.type).get(tagStorageId);
                if(override == null) {
                    throw new IllegalArgumentException("Requested storage id: " + tagStorageId + " for type " + identifier + " but none was found.");
                }
                EntityComponentStorage constructedStorage = override.construct();
                if(constructedStorage instanceof SaveableEntityStorage) {
                    SaveableEntityStorage saveableStorage = (SaveableEntityStorage) constructedStorage;
                    saveableStorage.readNBT(storageTag);
                    saveableStorage.constructTo(this);
                } else {
                    throw new IllegalArgumentException("Cannot load storage from a non saveable storage id: " + tagStorageId + " for type " + identifier + ". Attempting to construct from non serialized.");
                }
            }
        }
    }

    public void serialize(PacketBuffer buf) {
    }

    public void serializeSync(PacketBuffer buf) {
        this.serialize(buf);
    }

    public final byte[] serializeSync() {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        this.serializeSync(buffer);
        return buffer.array();
    }

    public void deserialize(PacketBuffer buf) {
    }

    public void deserializeSync(PacketBuffer buf) {
        this.deserialize(buf);
    }

    public void syncToClient() {
        TaskScheduler.addTask(() -> this.syncer.run(), 3);
    }

    public void setResync(ComponentAccess access) {
        this.syncer = () -> {
            if(access instanceof Entity && !((Entity) access).level.isClientSide) {
                DumbLibrary.NETWORK.send(PacketDistributor.DIMENSION.with(() -> ((Entity) access).level.dimension()), new S2CSyncComponent(((Entity) access).getId(), this.type, this.serializeSync()));
            }
        };
    }

    public void onCreated(EntityComponentType type, @Nullable EntityComponentStorage storage, @Nullable String storageID) {
        this.type = type;
        if(storage instanceof SaveableEntityStorage) {
            this.storage = (SaveableEntityStorage) storage;
            this.storageID = storageID;
        }
    }
}
