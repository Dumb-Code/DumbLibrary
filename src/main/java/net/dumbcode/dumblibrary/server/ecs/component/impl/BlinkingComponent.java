package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderFlattenedLayerComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.data.FlattenedLayerProperty;
import net.dumbcode.dumblibrary.server.utils.IndexedObject;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.function.Consumer;

public class BlinkingComponent extends EntityComponent implements RenderFlattenedLayerComponent {
    private String eyesOnTexture;
    private String eyesOffTexture;

    private int tickTimeOpen;
    private int tickTimeClose;

    @Override
    public void gatherComponents(Consumer<IndexedObject<FlattenedLayerProperty>> registry) {
        registry.accept(new IndexedObject<>(new FlattenedLayerProperty(() -> {
            if(this.access instanceof Entity) {
                int ticksExisted = ((Entity) this.access).ticksExisted;
                if(ticksExisted % (this.tickTimeOpen + this.tickTimeClose) <= this.tickTimeOpen) {
                    return this.eyesOnTexture;
                }
            }
            return this.eyesOffTexture;
        }, this.eyesOnTexture, this.eyesOffTexture), 0F));
    }

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setString("on_texture", this.eyesOnTexture);
        compound.setString("off_texture", this.eyesOffTexture);

        compound.setInteger("tick_time_on", this.tickTimeOpen);
        compound.setInteger("tick_time_off", this.tickTimeClose);
        return super.serialize(compound);
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        this.eyesOnTexture = compound.getString("on_texture");
        this.eyesOffTexture = compound.getString("off_texture");

        this.tickTimeOpen = compound.getInteger("tick_time_on");
        this.tickTimeClose = compound.getInteger("tick_time_off");
        super.deserialize(compound);
    }

    @Override
    public void serialize(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.eyesOnTexture);
        ByteBufUtils.writeUTF8String(buf, this.eyesOffTexture);

        buf.writeInt(this.tickTimeOpen);
        buf.writeInt(this.tickTimeClose);
        super.serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        this.eyesOnTexture = ByteBufUtils.readUTF8String(buf);
        this.eyesOffTexture = ByteBufUtils.readUTF8String(buf);

        this.tickTimeOpen = buf.readInt();
        this.tickTimeClose = buf.readInt();
        super.deserialize(buf);
    }

    @Setter
    @Accessors(chain = true)
    public static class Storage implements EntityComponentStorage<BlinkingComponent> {

        private String eyesOnTexture;
        private String eyesOffTexture;

        private int tickTimeOpen;
        private int tickTimeClose;

        @Override
        public BlinkingComponent constructTo(BlinkingComponent component) {
            component.eyesOnTexture = this.eyesOnTexture;
            component.eyesOffTexture = this.eyesOffTexture;

            component.tickTimeOpen = this.tickTimeOpen;
            component.tickTimeClose = this.tickTimeClose;
            return component;
        }

        @Override
        public void writeJson(JsonObject json) {
            json.addProperty("on_texture", this.eyesOnTexture);
            json.addProperty("off_texture", this.eyesOffTexture);

            json.addProperty("tick_time_on", this.tickTimeOpen);
            json.addProperty("tick_time_off", this.tickTimeClose);
        }

        @Override
        public void readJson(JsonObject json) {
            this.eyesOnTexture = JsonUtils.getString(json, "on_texture");
            this.eyesOffTexture = JsonUtils.getString(json, "off_texture");

            this.tickTimeOpen = JsonUtils.getInt(json, "tick_time_on");
            this.tickTimeClose = JsonUtils.getInt(json, "tick_time_off");
        }
    }
}
