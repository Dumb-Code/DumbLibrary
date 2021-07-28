package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderFlattenedLayerComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.data.FlattenedLayerProperty;
import net.dumbcode.dumblibrary.server.utils.IndexedObject;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.common.util.Constants;

import java.util.function.Consumer;

@Getter
public class EyesClosedComponent extends EntityComponent implements RenderFlattenedLayerComponent {
    private float index;
    private String eyesOpenTexture;
    private String eyesClosedTexture;

    private int blinkTicksLeft;

    @Override
    public void gatherComponents(ComponentAccess entity, Consumer<IndexedObject<FlattenedLayerProperty>> registry) {
        registry.accept(new IndexedObject<>(new FlattenedLayerProperty(() -> this.blinkTicksLeft > 0 ? this.eyesClosedTexture : this.eyesOpenTexture), this.index));
    }

    @Override
    public CompoundNBT serialize(CompoundNBT compound) {
        if(this.eyesOpenTexture != null) {
            compound.putString("open_texture", this.eyesOpenTexture);
        }
        if(this.eyesClosedTexture != null) {
            compound.putString("closed_texture", this.eyesClosedTexture);
        }

        compound.putInt("blink_tick_time", this.blinkTicksLeft);
        compound.putFloat("index", this.index);
        return super.serialize(compound);
    }

    @Override
    public void deserialize(CompoundNBT compound) {
        this.eyesOpenTexture = compound.contains("open_texture", Constants.NBT.TAG_STRING) ? compound.getString("on_texture") : null;
        this.eyesClosedTexture = compound.contains("closed_texture", Constants.NBT.TAG_STRING) ? compound.getString("off_texture") : null;

        this.blinkTicksLeft = compound.getInt("blink_tick_time");
        this.index = compound.getFloat("index");
        super.deserialize(compound);
    }

    @Override
    public void serialize(PacketBuffer buf) {
        buf.writeBoolean(this.eyesOpenTexture != null);
        if(this.eyesOpenTexture != null) {
            buf.writeUtf(this.eyesOpenTexture);
        }
        buf.writeBoolean(this.eyesClosedTexture != null);
        if(this.eyesClosedTexture != null) {
            buf.writeUtf(this.eyesClosedTexture);
        }

        buf.writeInt(this.blinkTicksLeft);
        buf.writeFloat(this.index);
        super.serialize(buf);
    }

    @Override
    public void serializeSync(PacketBuffer buf) {
        buf.writeInt(this.blinkTicksLeft);
    }

    public void setBlinkTicksLeft(int blinkTicksLeft) {
        this.blinkTicksLeft = blinkTicksLeft;
        if(blinkTicksLeft <= 0) {
            this.syncToClient();
        }
    }

    public void blink(int ticks) {
        this.blinkTicksLeft = Math.max(this.blinkTicksLeft, ticks);
        if(this.blinkTicksLeft == ticks) {
            this.syncToClient();
        }
    }

    public void forceOpenEyes() {
        this.blinkTicksLeft = 0;
        this.syncToClient();
    }

    @Override
    public void deserialize(PacketBuffer buf) {
        this.eyesOpenTexture = buf.readBoolean() ? buf.readUtf() : null;
        this.eyesClosedTexture = buf.readBoolean() ? buf.readUtf() : null;

        this.blinkTicksLeft = buf.readInt();
        this.index = buf.readFloat();
        super.deserialize(buf);
    }

    @Override
    public void deserializeSync(PacketBuffer buf) {
        this.blinkTicksLeft = buf.readInt();
    }

    @Setter
    @Accessors(chain = true)
    public static class Storage implements EntityComponentStorage<EyesClosedComponent> {

        private float index;
        private String eyesOpenTexture;
        private String eyesClosedTexture;

        @Override
        public void constructTo(EyesClosedComponent component) {
            component.eyesOpenTexture = this.eyesOpenTexture;
            component.eyesClosedTexture = this.eyesClosedTexture;
            component.index = this.index;
        }

        @Override
        public void writeJson(JsonObject json) {
            if(this.eyesOpenTexture != null) {
                json.addProperty("open_texture", this.eyesOpenTexture);
            }
            if(this.eyesClosedTexture != null) {
                json.addProperty("closed_texture", this.eyesClosedTexture);
            }
            json.addProperty("index", this.index);
        }

        @Override
        public void readJson(JsonObject json) {
            this.eyesOpenTexture = JSONUtils.getAsString(json, "open_texture", null);
            this.eyesClosedTexture = JSONUtils.getAsString(json, "closed_texture", null);
            this.index = JSONUtils.getAsFloat(json, "index");
        }
    }
}
