package net.dumbcode.dumblibrary.server.entity;

import io.netty.buffer.ByteBuf;
import net.dumbcode.dumblibrary.server.entity.component.EntityComponentMap;
import net.minecraft.entity.EntityCreature;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public abstract class ComposableCreatureEntity extends EntityCreature implements ComponentMapWriteAccess, IEntityAdditionalSpawnData {
    private final EntityComponentMap components = new EntityComponentMap();

    public ComposableCreatureEntity(World world) {
        super(world);
        this.attachComponents();
    }


    protected void attachComponents() {
    }

    @Override
    public EntityComponentMap getComponentMap() {
        return this.components;
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
