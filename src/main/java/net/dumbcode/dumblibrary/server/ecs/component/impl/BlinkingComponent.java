package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;

@Getter
public class BlinkingComponent extends EntityComponent {

    private int tickTimeOpen;
    private int tickTimeClose;

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setInteger("tick_time_on", this.tickTimeOpen);
        compound.setInteger("tick_time_off", this.tickTimeClose);
        return super.serialize(compound);
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        this.tickTimeOpen = compound.getInteger("tick_time_on");
        this.tickTimeClose = compound.getInteger("tick_time_off");
        super.deserialize(compound);
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeInt(this.tickTimeOpen);
        buf.writeInt(this.tickTimeClose);
        super.serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        this.tickTimeOpen = buf.readInt();
        this.tickTimeClose = buf.readInt();
        super.deserialize(buf);
    }

    @Setter
    @Accessors(chain = true)
    public static class Storage implements EntityComponentStorage<BlinkingComponent> {

        private int tickTimeOpen;
        private int tickTimeClose;

        @Override
        public BlinkingComponent constructTo(BlinkingComponent component) {
            component.tickTimeOpen = this.tickTimeOpen;
            component.tickTimeClose = this.tickTimeClose;
            return component;
        }

        @Override
        public void writeJson(JsonObject json) {
            json.addProperty("tick_time_on", this.tickTimeOpen);
            json.addProperty("tick_time_off", this.tickTimeClose);
        }

        @Override
        public void readJson(JsonObject json) {
            this.tickTimeOpen = JsonUtils.getInt(json, "tick_time_on");
            this.tickTimeClose = JsonUtils.getInt(json, "tick_time_off");
        }
    }
}
