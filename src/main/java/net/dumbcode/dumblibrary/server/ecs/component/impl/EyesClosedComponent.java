package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderFlattenedLayerComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.data.FlattenedLayerProperty;
import net.dumbcode.dumblibrary.server.ecs.component.storge.ShowcasingTextureStorage;
import net.dumbcode.dumblibrary.server.utils.IndexedObject;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.function.Consumer;

@Getter
public class EyesClosedComponent extends EntityComponent implements RenderFlattenedLayerComponent {
    private String eyesOnTexture;
    private String eyesOffTexture;

    private int blinkTicksLeft;

    @Override
    public void gatherComponents(ComponentAccess entity, Consumer<IndexedObject<FlattenedLayerProperty>> registry) {
        registry.accept(new IndexedObject<>(new FlattenedLayerProperty(() -> {
            if(this.blinkTicksLeft > 0) {
                return this.eyesOffTexture;
            }
            return this.eyesOnTexture;
        }, this.eyesOnTexture, this.eyesOffTexture), 0F));
    }

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setString("on_texture", this.eyesOnTexture);
        compound.setString("off_texture", this.eyesOffTexture);

        compound.setInteger("blink_tick_time", this.blinkTicksLeft);
        return super.serialize(compound);
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        this.eyesOnTexture = compound.getString("on_texture");
        this.eyesOffTexture = compound.getString("off_texture");

        this.blinkTicksLeft = compound.getInteger("blink_tick_time");
        super.deserialize(compound);
    }

    @Override
    public void serialize(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.eyesOnTexture);
        ByteBufUtils.writeUTF8String(buf, this.eyesOffTexture);

        buf.writeInt(this.blinkTicksLeft);
        super.serialize(buf);
    }

    @Override
    public void serializeSync(ByteBuf buf) {
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
    public void deserialize(ByteBuf buf) {
        this.eyesOnTexture = ByteBufUtils.readUTF8String(buf);
        this.eyesOffTexture = ByteBufUtils.readUTF8String(buf);

        this.blinkTicksLeft = buf.readInt();
        super.deserialize(buf);
    }

    @Override
    public void deserializeSync(ByteBuf buf) {
        this.blinkTicksLeft = buf.readInt();
    }

    @Setter
    @Accessors(chain = true)
    public static class Storage implements EntityComponentStorage<EyesClosedComponent>, ShowcasingTextureStorage {

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
            this.eyesOnTexture = JsonUtils.getString(json, "on_texture");
            this.eyesOffTexture = JsonUtils.getString(json, "off_texture");
        }

        @Override
        public void gatherTextures(Consumer<IndexedObject<String>> consumer) {
            consumer.accept(new IndexedObject<>(this.eyesOnTexture, 100));
        }
    }
}
