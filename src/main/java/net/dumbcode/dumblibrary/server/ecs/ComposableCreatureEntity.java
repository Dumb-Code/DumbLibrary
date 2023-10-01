package net.dumbcode.dumblibrary.server.ecs;

import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentMap;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.RenderAdjustmentsComponent;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

public abstract class ComposableCreatureEntity extends AmbientCreature implements ComponentMapWriteAccess, IEntityAdditionalSpawnData {
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
    public AABB getBoundingBoxForCulling() {
        return this.get(EntityComponentTypes.CULL_SIZE)
            .map(c -> {
                float halfWidth = c.getWidth() / 2;
                float[] scale = this.get(EntityComponentTypes.RENDER_ADJUSTMENTS).map(RenderAdjustmentsComponent::getScale).orElse(new float[]{1, 1, 1});
                return new AxisAlignedBB(this.position().subtract(halfWidth*scale[0], 0, halfWidth*scale[2]), this.position().add(halfWidth*scale[0], c.getHeight()*scale[1], halfWidth*scale[2]));
            }).orElse(super.getBoundingBoxForCulling());
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
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
