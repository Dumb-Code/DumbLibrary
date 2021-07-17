package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;

@Getter
public class CullSizeComponent extends EntityComponent {
    private float width;
    private float height;

    @Override
    public CompoundNBT serialize(CompoundNBT compound) {
        compound.putFloat("width", this.width);
        compound.putFloat("height", this.height);
        return super.serialize(compound);
    }

    @Override
    public void deserialize(CompoundNBT compound) {
        this.width = compound.getFloat("width");
        this.height = compound.getFloat("height");
        super.deserialize(compound);
    }

    @Override
    public void serialize(PacketBuffer buf) {
        buf.writeFloat(this.width);
        buf.writeFloat(this.height);
        super.serialize(buf);
    }

    @Override
    public void deserialize(PacketBuffer buf) {
        this.width = buf.readFloat();
        this.height = buf.readFloat();
        super.deserialize(buf);
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Storage implements EntityComponentStorage<CullSizeComponent> {

        //Width and height are in blocks.
        private float width;
        private float height;

        @Override
        public void constructTo(CullSizeComponent component) {
            component.width = this.width;
            component.height = this.height;
        }

        @Override
        public void writeJson(JsonObject json) {
            json.addProperty("width", this.width);
            json.addProperty("height", this.height);
        }

        @Override
        public void readJson(JsonObject json) {
            this.width = JSONUtils.getAsFloat(json, "width");
            this.height = JSONUtils.getAsFloat(json, "height");
        }
    }
}
