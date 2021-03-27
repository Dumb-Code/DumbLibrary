package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.animation.Animation;
import net.dumbcode.dumblibrary.server.attributes.ModifiableField;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.MovePredicateComponent;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

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

    public int snoringTicks;

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
    public CompoundNBT serialize(CompoundNBT compound) {
        compound.putString("animation", Objects.requireNonNull(this.sleepingAnimation.getKey()).toString());
        compound.put("sleep_time", this.sleepTime.writeToNBT());
        compound.put("wakeup_time", this.wakeupTime.writeToNBT());
        compound.putBoolean("is_sleeping", this.isSleeping);
        return super.serialize(compound);
    }

    @Override
    public void deserialize(CompoundNBT compound) {
        this.sleepingAnimation = new Animation(new ResourceLocation(compound.getString("animation")));
        this.sleepTime.readFromNBT(compound.getCompound("sleep_time"));
        this.wakeupTime.readFromNBT(compound.getCompound("wakeup_time"));
        this.isSleeping = compound.getBoolean("is_sleeping");
        super.deserialize(compound);
    }

    @Override
    public void serialize(PacketBuffer buf) {
        buf.writeUtf(this.sleepingAnimation.getKey().toString());
        buf.writeBoolean(this.isSleeping);
        super.serialize(buf);
    }

    @Override
    public void deserialize(PacketBuffer buf) {
        this.sleepingAnimation = new Animation(new ResourceLocation(buf.readUtf()));
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
            this.sleepingAnimation = new ResourceLocation(JSONUtils.getAsString(json, "animation"));
            this.sleepTime = JSONUtils.getAsFloat(json, "sleep_time");
            this.wakeupTime = JSONUtils.getAsFloat(json, "wakeup_time");
        }
    }

}
