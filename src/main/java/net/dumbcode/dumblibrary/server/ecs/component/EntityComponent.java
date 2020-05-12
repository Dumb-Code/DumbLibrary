package net.dumbcode.dumblibrary.server.ecs.component;

import io.netty.buffer.ByteBuf;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.network.S2SyncComponent;
import net.dumbcode.dumblibrary.server.registry.DumbRegistries;
import net.dumbcode.dumblibrary.server.utils.TaskScheduler;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;

public abstract class EntityComponent {

    protected Runnable syncer;
    protected EntityComponentType type;
    @Nullable private SaveableEntityStorage storage;
    @Nullable private String storageID;

    public NBTTagCompound serialize(NBTTagCompound compound) {
        if(this.storage != null) {
            NBTTagCompound storageTag = new NBTTagCompound();
            if(this.storageID != null) {
                storageTag.setString("storage_id", this.storageID);
            }
            this.storage.writeNBT(storageTag);
            compound.setTag("storage", storageTag);
        }
        return compound;
    }

    public void deserialize(NBTTagCompound compound) {
        ResourceLocation identifier = new ResourceLocation(compound.getString("identifier"));
        this.type = DumbRegistries.COMPONENT_REGISTRY.getValue(identifier);
        if(compound.hasKey("storage", Constants.NBT.TAG_COMPOUND)) {
            NBTTagCompound storageTag = compound.getCompoundTag("storage");
            if(storageTag.hasKey("storage_id", Constants.NBT.TAG_STRING)) {
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

    public void serialize(ByteBuf buf) {
    }

    public void serializeSync(ByteBuf buf) {
        this.serialize(buf);
    }

    public void deserialize(ByteBuf buf) {
    }

    public void deserializeSync(ByteBuf buf) {
        this.deserialize(buf);
    }

    public void syncToClient() {
        TaskScheduler.addTask(() -> this.syncer.run(), 3);
    }

    public void setResync(ComponentAccess access) {
        this.syncer = () -> {
            if(access instanceof Entity && !((Entity) access).world.isRemote) {
                DumbLibrary.NETWORK.sendToDimension(new S2SyncComponent(((Entity) access).getEntityId(), this.type, this), ((Entity) access).world.provider.getDimension());
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
