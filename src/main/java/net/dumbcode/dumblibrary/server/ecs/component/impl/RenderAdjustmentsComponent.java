package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderCallbackComponent;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;

import java.util.List;


public class RenderAdjustmentsComponent implements RenderCallbackComponent {

    @Getter private final float[] scale = new float[3];
    private final float[] defaultScale = new float[3];

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setFloat("sx", this.defaultScale[0]);
        compound.setFloat("sy", this.defaultScale[1]);
        compound.setFloat("sz", this.defaultScale[2]);
        return compound;
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        this.defaultScale[0] = compound.getFloat("sx");
        this.defaultScale[1] = compound.getFloat("sy");
        this.defaultScale[2] = compound.getFloat("sz");
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeFloat(this.defaultScale[0]);
        buf.writeFloat(this.defaultScale[1]);
        buf.writeFloat(this.defaultScale[2]);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        this.defaultScale[0] = buf.readFloat();
        this.defaultScale[1] = buf.readFloat();
        this.defaultScale[2] = buf.readFloat();
    }

    private void resetScale() {
        this.scale[0] = this.defaultScale[0];
        this.scale[1] = this.defaultScale[1];
        this.scale[2] = this.defaultScale[2];
    }

    @Override
    public void addCallbacks(List<SubCallback> preRenderCallbacks, List<MainCallback> renderCallbacks, List<SubCallback> postRenderCallback) {
        preRenderCallbacks.add((context1, entity1, x, y, z, entityYaw, partialTicks) -> GlStateManager.scale(this.scale[0], this.scale[1], this.scale[2]));

        postRenderCallback.add((context1, entity1, x, y, z, entityYaw, partialTicks) -> this.resetScale());

    }

    @Accessors(chain = true)
    @Setter
    public static class Storage implements EntityComponentStorage<RenderAdjustmentsComponent> {

        private float scaleX = 1F;
        private float scaleY = 1F;
        private float scaleZ = 1F;

        @Override
        public RenderAdjustmentsComponent construct() {
            RenderAdjustmentsComponent component = new RenderAdjustmentsComponent();
            component.defaultScale[0] = this.scaleX;
            component.defaultScale[1] = this.scaleY;
            component.defaultScale[2] = this.scaleZ;

            component.resetScale();
            return component;
        }

        @Override
        public void readJson(JsonObject json) {
            this.scaleX = JsonUtils.getFloat(json, "sx");
            this.scaleY = JsonUtils.getFloat(json, "sy");
            this.scaleZ = JsonUtils.getFloat(json, "sz");
        }

        @Override
        public void writeJson(JsonObject json) {
            json.addProperty("sx", this.scaleX);
            json.addProperty("sy", this.scaleY);
            json.addProperty("sz", this.scaleZ);

        }
    }
}
