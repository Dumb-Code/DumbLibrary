package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.attributes.ModifiableField;
import net.dumbcode.dumblibrary.server.attributes.ModifiableFieldModifier;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.FinalizableComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderCallbackComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.ScaleAdjustmentComponent;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;


public class RenderAdjustmentsComponent extends EntityComponent implements RenderCallbackComponent, FinalizableComponent {

    private final float[] defaultScale = new float[3];

    private final List<Supplier<Float>> modifiers = new ArrayList<>();

    @Getter
    public final ModifiableField scaleModifier = ModifiableField.createField(1D);

    public float[] getScale() {
        float[] mutScale = new float[] { this.defaultScale[0], this.defaultScale[1], this.defaultScale[0] };
        for (Supplier<Float> modifier : this.modifiers) {
            float mod = modifier.get();

            mutScale[0] *= mod;
            mutScale[1] *= mod;
            mutScale[2] *= mod;
        }
        return mutScale;
    }

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setFloat("sx", this.defaultScale[0]);
        compound.setFloat("sy", this.defaultScale[1]);
        compound.setFloat("sz", this.defaultScale[2]);
        return super.serialize(compound);
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        super.deserialize(compound);
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


    @Override
    public void addCallbacks(List<SubCallback> preRenderCallbacks, List<MainCallback> renderCallbacks, List<SubCallback> postRenderCallback) {
        preRenderCallbacks.add((context1, entity1, x, y, z, entityYaw, partialTicks) -> {
            float[] scale = this.getScale();
            GlStateManager.scale(scale[0], scale[1], scale[2]);
        });
    }

    @Override
    public void finalizeComponent(ComponentAccess entity) {
        this.modifiers.clear();
        this.modifiers.add(() -> (float) this.scaleModifier.getValue());
        for (EntityComponent component : entity.getAllComponents()) {
            if (component instanceof ScaleAdjustmentComponent) {
                ((ScaleAdjustmentComponent) component).applyScale(this.modifiers::add);
            }
        }
    }

    @Accessors(chain = true)
    @Setter
    public static class Storage implements EntityComponentStorage<RenderAdjustmentsComponent> {

        private float scaleX = 1F;
        private float scaleY = 1F;
        private float scaleZ = 1F;

        @Override
        public RenderAdjustmentsComponent constructTo(RenderAdjustmentsComponent component) {
            component.defaultScale[0] = this.scaleX;
            component.defaultScale[1] = this.scaleY;
            component.defaultScale[2] = this.scaleZ;
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
