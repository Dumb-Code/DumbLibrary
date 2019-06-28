package net.dumbcode.dumblibrary.server.entity;

import io.netty.buffer.ByteBuf;
import net.dumbcode.dumblibrary.server.entity.component.*;
import net.minecraft.entity.EntityCreature;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public abstract class ComposableCreatureEntity extends EntityCreature implements ComponentWriteAccess, IEntityAdditionalSpawnData {
    private final EntityComponentMap components = new EntityComponentMap();

    public ComposableCreatureEntity(World world) {
        super(world);
        this.attachComponents();
    }


    protected void attachComponents() {
    }

    @Override
    public void finalizeComponents() {
        for (EntityComponent component : this.getAllComponents()) {
            if (component instanceof FinalizableComponent) {
                FinalizableComponent aiComponent = (FinalizableComponent) component;
                aiComponent.finalizeComponent(this);
            }
        }
    }

    @Override
    public <T extends EntityComponent, S extends EntityComponentStorage<T>> void attachComponent(EntityComponentType<T, ?> type, T component) {
        if(component == null) {
            throw new NullPointerException("Component on type " + type.getIdentifier() + " is null.");
        }
        this.components.put(type, component);
    }

    @Nullable
    @Override
    public <T extends EntityComponent, S extends EntityComponentStorage<T>> T getOrNull(EntityComponentType<T, S> type) {
        return this.components.getNullable(type);
    }

    @Nonnull
    @Override
    public <T extends EntityComponent, S extends EntityComponentStorage<T>> T getOrExcept(EntityComponentType<T, S> type) {
        T component = this.components.getNullable(type);
        if (component == null) {
            throw new IllegalArgumentException("Component '" + type.getIdentifier() + "' is not present on entity");
        }
        return component;
    }

    @Nonnull
    @Override
    public Collection<EntityComponent> getAllComponents() {
        return this.components.values();
    }

    @Override
    public boolean contains(EntityComponentType<?, ?> type) {
        return this.components.containsKey(type);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        compound.setTag("components", this.components.serialize(new NBTTagList()));

        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        NBTTagList componentList = compound.getTagList("components", Constants.NBT.TAG_COMPOUND);
        this.components.deserialize(componentList);
        this.finalizeComponents();
    }

    @Override
    public void writeSpawnData(ByteBuf buf) {
        this.components.serialize(buf);
    }

    @Override
    public void readSpawnData(ByteBuf buf) {
        this.components.deserialize(buf);
        this.finalizeComponents();
    }
}
