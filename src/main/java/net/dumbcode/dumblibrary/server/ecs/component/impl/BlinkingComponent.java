package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;

@Getter
public class BlinkingComponent extends EntityComponent {

    private int tickTimeOpen;
    private int tickTimeClose;

    @Override
    public CompoundNBT serialize(CompoundNBT compound) {
        compound.putInt("tick_time_on", this.tickTimeOpen);
        compound.putInt("tick_time_off", this.tickTimeClose);
        return super.serialize(compound);
    }

    @Override
    public void deserialize(CompoundNBT compound) {
        this.tickTimeOpen = compound.getInt("tick_time_on");
        this.tickTimeClose = compound.getInt("tick_time_off");
        super.deserialize(compound);
    }

    @Override
    public void serialize(PacketBuffer buf) {
        buf.writeInt(this.tickTimeOpen);
        buf.writeInt(this.tickTimeClose);
        super.serialize(buf);
    }

    @Override
    public void deserialize(PacketBuffer buf) {
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
        public void constructTo(BlinkingComponent component) {
            component.tickTimeOpen = this.tickTimeOpen;
            component.tickTimeClose = this.tickTimeClose;
        }

        @Override
        public void writeJson(JsonObject json) {
            json.addProperty("tick_time_on", this.tickTimeOpen);
            json.addProperty("tick_time_off", this.tickTimeClose);
        }

        @Override
        public void readJson(JsonObject json) {
            this.tickTimeOpen = JSONUtils.getAsInt(json, "tick_time_on");
            this.tickTimeClose = JSONUtils.getAsInt(json, "tick_time_off");
        }
    }
}
