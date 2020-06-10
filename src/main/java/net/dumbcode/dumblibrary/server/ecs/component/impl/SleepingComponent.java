package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.animation.objects.Animation;
import net.dumbcode.dumblibrary.server.attributes.ModifiableField;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.MovePredicateComponent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Getter
public class SleepingComponent extends EntityComponent implements MovePredicateComponent {

    private Animation sleepingAnimation;

    private final ModifiableField sleepTime = new ModifiableField();
    private final ModifiableField wakeupTime = new ModifiableField();
    private final ModifiableField nocturnalChance = new ModifiableField(); //If passed 1 then is nocturnal

    private boolean isSleeping;

    public boolean isNocturnal() {
        return this.nocturnalChance.getValue() >= 1;
    }

    public ModifiableField getSleepTime() {
        return this.isNocturnal() ? this.wakeupTime : this.sleepTime;
    }

    public ModifiableField getWakeupTime() {
        return this.isNocturnal() ? this.sleepTime : this.wakeupTime;
    }

    public void setSleeping(boolean sleeping) {
        this.isSleeping = sleeping;
        this.syncToClient();
    }

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setString("animation", Objects.requireNonNull(this.sleepingAnimation.getKey()).toString());
        compound.setTag("sleep_time", this.sleepTime.writeToNBT());
        compound.setTag("wakeup_time", this.wakeupTime.writeToNBT());
        compound.setBoolean("is_sleeping", this.isSleeping);
        return super.serialize(compound);
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        this.sleepingAnimation = new Animation(new ResourceLocation(compound.getString("animation")));
        this.sleepTime.readFromNBT(compound.getCompoundTag("sleep_time"));
        this.wakeupTime.readFromNBT(compound.getCompoundTag("wakeup_time"));
        this.isSleeping = compound.getBoolean("is_sleeping");
        super.deserialize(compound);
    }

    @Override
    public void serialize(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.sleepingAnimation.getKey().toString());
        buf.writeBoolean(this.isSleeping);
        super.serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        this.sleepingAnimation = new Animation(new ResourceLocation(ByteBufUtils.readUTF8String(buf)));
        this.isSleeping = buf.readBoolean();
        super.deserialize(buf);
    }

    @Override
    public void addBlockers(Consumer<Supplier<Boolean>> registry) {
        registry.accept(() -> !this.isSleeping());
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Storage implements EntityComponentStorage<SleepingComponent> {

        private ResourceLocation sleepingAnimation;
        private double sleepTime;
        private double wakeupTime;

        @Override
        public void constructTo(SleepingComponent component) {
            component.sleepingAnimation = new Animation(this.sleepingAnimation);
            component.sleepTime.setBaseValue(this.sleepTime);
            component.wakeupTime.setBaseValue(this.wakeupTime);
            component.nocturnalChance.setBaseValue(0D);
        }

        @Override
        public void writeJson(JsonObject json) {
            json.addProperty("animation", this.sleepingAnimation.toString());
            json.addProperty("sleep_time", this.sleepTime);
            json.addProperty("wakeup_time", this.wakeupTime);
        }

        @Override
        public void readJson(JsonObject json) {
            this.sleepingAnimation = new ResourceLocation(JsonUtils.getString(json, "animation"));
            this.sleepTime = JsonUtils.getFloat(json, "sleep_time");
            this.wakeupTime = JsonUtils.getFloat(json, "wakeup_time");
        }
    }

}
