package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.gson.JsonObject;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.HerdSavedData;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.FinalizableComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

public class HerdComponent implements FinalizableComponent {

    public UUID herdUUID;
    public ResourceLocation herdTypeID;

    public HerdSavedData herdData;

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        if(this.herdUUID != null) {
            compound.setUniqueId("uuid", this.herdUUID);
        }

        compound.setString("herd_type_id", this.herdTypeID.toString());

        return compound;
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        if(compound.hasUniqueId("uuid")) {
            this.herdUUID = compound.getUniqueId("uuid");
        } else {
            this.herdUUID = null;
        }

        this.herdTypeID = new ResourceLocation(compound.getString("herd_type_id"));
    }

    @Override
    public void finalizeComponent(ComponentAccess entity) {
        if(entity instanceof Entity) {
            if(entity instanceof EntityLivingBase) {
                ((EntityLivingBase)entity).getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(120.0D);
            }
        }

    }

    public void addMember(UUID uniqueID, HerdComponent herd) {
        herd.herdUUID = this.herdUUID;
        herd.herdData = this.herdData;
        this.herdData.addMember(uniqueID);
    }

    public void removeMember(UUID uniqueID, HerdComponent herd) {
        this.herdData.removeMember(uniqueID);
        herd.herdUUID = null;
        herd.herdData = null;
    }

    @Accessors(chain = true)
    @Setter
    public static class Storage implements EntityComponentStorage<HerdComponent> {

        private ResourceLocation herdTypeID; //Used to check if entities are compatible with other herds ect.

        @Override
        public HerdComponent construct() {
            HerdComponent component = new HerdComponent();
            component.herdTypeID = this.herdTypeID;
            return component;
        }

        @Override
        public void readJson(JsonObject json) {
            this.herdTypeID = new ResourceLocation(json.get("herd_type_id").getAsString());
        }

        @Override
        public void writeJson(JsonObject json) {
            json.addProperty("herd_type_id", this.herdTypeID.toString());
        }
    }
}
