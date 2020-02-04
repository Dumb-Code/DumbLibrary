package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.attributes.ModifiableField;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;

@Getter
public class LightAffectSleepingComponent extends EntityComponent {

    private final ModifiableField skylightLevelStart = new ModifiableField(); //the light level at which the dino will start getting sleepy
    private final ModifiableField blocklightLevelStart = new ModifiableField();

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setTag("sky_light_level_start", this.skylightLevelStart.writeToNBT());
        compound.setTag("block_light_level_start", this.blocklightLevelStart.writeToNBT());
        return super.serialize(compound);
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        this.skylightLevelStart.readFromNBT(compound.getCompoundTag("sky_light_level_start"));
        this.blocklightLevelStart.readFromNBT(compound.getCompoundTag("block_light_level_start"));
        super.deserialize(compound);
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Storage implements EntityComponentStorage<LightAffectSleepingComponent> {

        private int skylightLevelStart; //the light level at which the dino will start getting sleepy
        private int blocklightLevelStart;

        @Override
        public LightAffectSleepingComponent constructTo(LightAffectSleepingComponent component) {
            component.skylightLevelStart.setBaseValue(this.skylightLevelStart);
            component.blocklightLevelStart.setBaseValue(this.blocklightLevelStart);
            return component;
        }

        @Override
        public void writeJson(JsonObject json) {
            json.addProperty("sky_light_level_start", this.skylightLevelStart);
            json.addProperty("block_light_level_start", this.blocklightLevelStart);
        }

        @Override
        public void readJson(JsonObject json) {
            this.skylightLevelStart = JsonUtils.getInt(json, "sky_light_level_start");
            this.blocklightLevelStart = JsonUtils.getInt(json, "block_light_level_start");
        }
    }

}
