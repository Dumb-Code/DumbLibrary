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

import java.util.function.Consumer;

@Getter
public class EyesClosedComponent extends EntityComponent implements RenderFlattenedLayerComponent {
    private String eyesOnTexture;
    private String eyesOffTexture;

    private int blinkTicksLeft;

    @Override
    public void gatherComponents(ComponentAccess entity, Consumer<IndexedObject<FlattenedLayerProperty>> registry) {
        registry.accept(new IndexedObject<>(new FlattenedLayerProperty(() -> this.blinkTicksLeft > 0 ? this.eyesOffTexture : this.eyesOnTexture), 0F));
    }

    @Override
    public CompoundNBT serialize(CompoundNBT compound) {
        compound.putString("on_texture", this.eyesOnTexture);
        compound.putString("off_texture", this.eyesOffTexture);

        compound.putInt("blink_tick_time", this.blinkTicksLeft);
        return super.serialize(compound);
    }

    @Override
    public void deserialize(CompoundNBT compound) {
        this.eyesOnTexture = compound.getString("on_texture");
        this.eyesOffTexture = compound.getString("off_texture");

        this.blinkTicksLeft = compound.getInt("blink_tick_time");
        super.deserialize(compound);
    }

    @Override
    public void serialize(PacketBuffer buf) {
        buf.writeUtf(this.eyesOnTexture);
        buf.writeUtf(this.eyesOffTexture);

        buf.writeInt(this.blinkTicksLeft);
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
        this.eyesOnTexture = buf.readUtf();
        this.eyesOffTexture = buf.readUtf();

        this.blinkTicksLeft = buf.readInt();
        super.deserialize(buf);
    }

    @Override
    public void deserializeSync(PacketBuffer buf) {
        this.blinkTicksLeft = buf.readInt();
    }

    @Setter
    @Accessors(chain = true)
    public static class Storage implements EntityComponentStorage<EyesClosedComponent> {

        private String eyesOnTexture;
        private String eyesOffTexture;

        @Override
        public void constructTo(EyesClosedComponent component) {
            component.eyesOnTexture = this.eyesOnTexture;
            component.eyesOffTexture = this.eyesOffTexture;
        }

        @Override
        public void writeJson(JsonObject json) {
            json.addProperty("on_texture", this.eyesOnTexture);
            json.addProperty("off_texture", this.eyesOffTexture);
        }

        @Override
        public void readJson(JsonObject json) {
            this.eyesOnTexture = JSONUtils.getAsString(json, "on_texture");
            this.eyesOffTexture = JSONUtils.getAsString(json, "off_texture");
        }
    }
}
