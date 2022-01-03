package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.attributes.ModifiableField;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.FinalizableComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderCallbackComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.ScaleAdjustmentComponent;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;

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
    public CompoundNBT serialize(CompoundNBT compound) {
        compound.putFloat("sx", this.defaultScale[0]);
        compound.putFloat("sy", this.defaultScale[1]);
        compound.putFloat("sz", this.defaultScale[2]);
        compound.put("modifier", this.scaleModifier.writeToNBT());
        return super.serialize(compound);
    }

    @Override
    public void deserialize(CompoundNBT compound) {
        super.deserialize(compound);
        this.defaultScale[0] = compound.getFloat("sx");
        this.defaultScale[1] = compound.getFloat("sy");
        this.defaultScale[2] = compound.getFloat("sz");
        this.scaleModifier.readFromNBT(compound.getCompound("modifier"));
    }

    @Override
    public void serialize(PacketBuffer buf) {
        buf.writeFloat(this.defaultScale[0]);
        buf.writeFloat(this.defaultScale[1]);
        buf.writeFloat(this.defaultScale[2]);
        this.scaleModifier.writeToBuffer(buf);
    }

    @Override
    public void deserialize(PacketBuffer buf) {
        this.defaultScale[0] = buf.readFloat();
        this.defaultScale[1] = buf.readFloat();
        this.defaultScale[2] = buf.readFloat();
        this.scaleModifier.readFromBuffer(buf);
    }


    @Override
    public void addCallbacks(List<SubCallback> preRenderCallbacks, List<MainCallback> renderCallbacks, List<SubCallback> postRenderCallback) {
//        preRenderCallbacks.add((context1, entity1, x, y, z, entityYaw, partialTicks) -> {
//            float[] scale = this.getScale();
//            GlStateManager.scale(scale[0], scale[1], scale[2]);
//        });
        preRenderCallbacks.add((context, entity, entityYaw, partialTicks, stack, buffer, light) -> {
            float[] scale = this.getScale();
            stack.scale(scale[0], scale[1], scale[2]);
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
        public void constructTo(RenderAdjustmentsComponent component) {
            component.defaultScale[0] = this.scaleX;
            component.defaultScale[1] = this.scaleY;
            component.defaultScale[2] = this.scaleZ;
        }

        @Override
        public void readJson(JsonObject json) {
            this.scaleX = JSONUtils.getAsFloat(json, "sx");
            this.scaleY = JSONUtils.getAsFloat(json, "sy");
            this.scaleZ = JSONUtils.getAsFloat(json, "sz");
        }

        @Override
        public void writeJson(JsonObject json) {
            json.addProperty("sx", this.scaleX);
            json.addProperty("sy", this.scaleY);
            json.addProperty("sz", this.scaleZ);

        }
    }
}
