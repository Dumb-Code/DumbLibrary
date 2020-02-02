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

    private double tiredness; //2^(x / k). x = tiredness, k = tireness it take to double change of being asleep
    private final ModifiableField tirednessLossPerTickSleeping = new ModifiableField(); //rename var?

    private final ModifiableField tirednessChanceConstant = new ModifiableField(); //The amount of tiredness it takes to double the chance of being to sleep

    private int sleepingTicksLeft;

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setString("animation", Objects.requireNonNull(this.sleepingAnimation.getRegistryName()).toString());
        compound.setDouble("tiredness", this.tiredness);
        compound.setTag("tiredness_loss_per_tick_sleeping", this.tirednessLossPerTickSleeping.writeToNBT());
        compound.setTag("tiredness_chance_constant", this.tirednessChanceConstant.writeToNBT());
        compound.setInteger("sleeping_ticks_left", this.sleepingTicksLeft);
        return super.serialize(compound);
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        this.sleepingAnimation = DumbRegistries.ANIMATION_REGISTRY.getValue(new ResourceLocation(compound.getString("animation")));
        this.tiredness = compound.getDouble("tiredness");
        this.tirednessLossPerTickSleeping.readFromNBT(compound.getCompoundTag("tiredness_loss_per_tick_sleeping"));
        this.tirednessChanceConstant.readFromNBT(compound.getCompoundTag("tiredness_chance_constant"));
        this.sleepingTicksLeft = compound.getInteger("sleeping_ticks_left");
        super.deserialize(compound);
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Storage implements EntityComponentStorage<SleepingComponent> {

        private ResourceLocation sleepingAnimation;
        private double tirednessLossPerTickSleeping;
        private double tirednessChanceConstant; //The amount of tiredness it takes to double the chance of being to sleep

        @Override
        public SleepingComponent constructTo(SleepingComponent component) {
            component.sleepingAnimation = DumbRegistries.ANIMATION_REGISTRY.getValue(this.sleepingAnimation);
            component.tirednessLossPerTickSleeping.setBaseValue(this.tirednessLossPerTickSleeping);
            component.tirednessChanceConstant.setBaseValue(this.tirednessChanceConstant);
            return component;
        }

        @Override
        public void writeJson(JsonObject json) {
            json.addProperty("animation", this.sleepingAnimation.toString());
            json.addProperty("tiredness_loss_per_tick_sleeping", this.tirednessLossPerTickSleeping);
            json.addProperty("tiredness_chance_constant", this.tirednessChanceConstant);
        }

        @Override
        public void readJson(JsonObject json) {
            this.sleepingAnimation = new ResourceLocation(JsonUtils.getString(json, "animation"));
            this.tirednessLossPerTickSleeping = JsonUtils.getFloat(json, "tiredness_loss_per_tick_sleeping");
            this.tirednessChanceConstant = JsonUtils.getFloat(json, "tiredness_chance_constant");
        }
    }

}
