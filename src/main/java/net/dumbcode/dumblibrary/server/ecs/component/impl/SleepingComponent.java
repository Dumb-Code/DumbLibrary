package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.animation.objects.Animation;
import net.dumbcode.dumblibrary.server.attributes.ModifiableField;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.registry.DumbRegistries;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import java.util.Objects;

@Getter
@Setter
public class SleepingComponent extends EntityComponent {

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
