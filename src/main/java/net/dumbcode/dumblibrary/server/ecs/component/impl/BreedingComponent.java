package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.CanBreedComponent;

@Getter
public class BreedingComponent extends EntityComponent implements CanBreedComponent {
    @Setter
    private int ticksSinceLastBreed = 2000;
    private int minTicksBetweenBreeding = 2000;

    @Override
    public boolean canBreedWith(ComponentAccess otherEntity) {
        return this.ticksSinceLastBreed >= this.minTicksBetweenBreeding && otherEntity.get(EntityComponentTypes.BREEDING).map(b -> b.ticksSinceLastBreed >= b.minTicksBetweenBreeding).orElse(true);
    }

    public static class Storage implements EntityComponentStorage<BreedingComponent> {

        @Override
        public BreedingComponent constructTo(BreedingComponent component) {
            return component;
        }

        @Override
        public void writeJson(JsonObject json) {

        }

        @Override
        public void readJson(JsonObject json) {

        }
    }
}
