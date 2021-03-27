package net.dumbcode.dumblibrary.server.ecs;

import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentMap;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public abstract class ComposableCreatureEntity extends CreatureEntity implements ComponentMapWriteAccess, IEntityAdditionalSpawnData {
    private final EntityComponentMap components = new EntityComponentMap();

    protected ComposableCreatureEntity(EntityType<? extends CreatureEntity> type, World world) {
        super(type, world);
        this.attachComponents();

    }


    protected void attachComponents() {
    }

    @Override
    public EntityComponentMap getComponentMap() {
        return this.components;
    }


    @Override
    public void addAdditionalSaveData(CompoundNBT compound) {
        super.addAdditionalSaveData(compound);
        compound.put("components", this.components.serialize(new ListNBT()));
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT compound) {
        super.readAdditionalSaveData(compound);

        ListNBT componentList = compound.getList("components", Constants.NBT.TAG_COMPOUND);
        this.components.deserialize(componentList);
        this.finalizeComponents();
    }


    @Override
    public void writeSpawnData(PacketBuffer buf) {
        this.components.serialize(buf);
    }

    @Override
    public void readSpawnData(PacketBuffer buf) {
        this.components.deserialize(buf);
        this.finalizeComponents();
    }
}
