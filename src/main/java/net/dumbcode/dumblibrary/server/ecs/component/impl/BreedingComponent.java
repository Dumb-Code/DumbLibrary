package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.CanBreedComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.JSONUtils;

@Getter
public class BreedingComponent extends EntityComponent implements CanBreedComponent {
    @Setter
    private int ticksSinceLastBreed = 2000;
    private int minTicksBetweenBreeding = 2000;

    @Override
    public CompoundTag serialize(CompoundTag compound) {
        compound.putInt("min_ticks_between_breeding", this.minTicksBetweenBreeding);
        compound.putInt("ticks_since_last_breeding", this.ticksSinceLastBreed);
        return super.serialize(compound);
    }

    @Override
    public void deserialize(CompoundTag compound) {
        this.minTicksBetweenBreeding = compound.getInt("min_ticks_between_breeding");
        this.ticksSinceLastBreed = compound.getInt("ticks_since_last_breeding");
        super.deserialize(compound);
    }

    @Override
    public boolean canBreedWith(ComponentAccess otherEntity) {
        return this.ticksSinceLastBreed >= this.minTicksBetweenBreeding && otherEntity.get(EntityComponentTypes.BREEDING).map(b -> b.ticksSinceLastBreed >= b.minTicksBetweenBreeding).orElse(true);
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Storage implements EntityComponentStorage<BreedingComponent> {

        private int minTicksBetweenBreeding = 2000;

        @Override
        public void constructTo(BreedingComponent component) {
            component.minTicksBetweenBreeding = component.ticksSinceLastBreed = this.minTicksBetweenBreeding;
        }

        @Override
        public void writeJson(JsonObject json) {
            json.addProperty("min_ticks_between_breeding", this.minTicksBetweenBreeding);
        }

        @Override
        public void readJson(JsonObject json) {
            this.minTicksBetweenBreeding = JSONUtils.getAsInt(json, "min_ticks_between_breeding");
        }
    }
}
