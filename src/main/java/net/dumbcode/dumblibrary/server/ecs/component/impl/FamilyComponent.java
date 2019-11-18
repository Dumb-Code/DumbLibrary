package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.ecs.FamilySavedData;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.json.JsonUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import java.util.Optional;
import java.util.UUID;


public class FamilyComponent extends EntityComponent {
    @Getter @Setter private UUID familyUUID;
    @Getter @Setter private ResourceLocation familyTypeId;
    private FamilySavedData dataCache;

    public Optional<FamilySavedData> getDataCache() {
        if(this.dataCache == null) {
            this.dataCache = FamilySavedData.getData(this.familyUUID);
        }
        return Optional.of(this.dataCache);
    }

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setUniqueId("family_uuid", this.familyUUID);
        compound.setString("family_type", this.familyTypeId.toString());
        return super.serialize(compound);
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        this.familyUUID = compound.getUniqueId("family_uuid");
        this.familyTypeId = new ResourceLocation(compound.getString("family_type"));
        super.deserialize(compound);
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Storage implements EntityComponentStorage<FamilyComponent> {

        private ResourceLocation familyType;

        @Override
        public FamilyComponent construct() {
            FamilyComponent component = new FamilyComponent();
            component.familyTypeId = this.familyType;
            return component;
        }

        @Override
        public void writeJson(JsonObject json) {
            json.addProperty("family_type", this.familyType.toString());
        }

        @Override
        public void readJson(JsonObject json) {
            this.familyType = new ResourceLocation(JsonUtils.getString(json, "family_type"));
        }
    }
}
