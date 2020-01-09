package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.gson.JsonObject;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.ai.DrinkingAI;
import net.dumbcode.dumblibrary.server.ecs.ai.FeedingAI;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.FinalizableComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.CanBreedComponent;
import net.dumbcode.dumblibrary.server.ecs.objects.FeedingDiet;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;

public class MetabolismComponent extends EntityComponent implements FinalizableComponent, CanBreedComponent {
    public int food;
    public int water;

    public int foodRate;
    public int waterRate;

    public int foodTicks;
    public int waterTicks;

    public FeedingDiet diet = new FeedingDiet();
    public int foodSmellDistance;

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setInteger("food", this.food);
        compound.setInteger("water", this.water);

        compound.setInteger("food_rate", this.foodRate);
        compound.setInteger("water_rate", this.waterRate);

        compound.setInteger("food_ticks", this.foodTicks);
        compound.setInteger("water_ticks", this.waterTicks);

        compound.setInteger("food_smell_distance", this.foodSmellDistance);

        compound.setTag("diet", this.diet.writeToNBT(new NBTTagCompound()));
        return super.serialize(compound);
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        super.deserialize(compound);
        this.food = compound.getInteger("food");
        this.water = compound.getInteger("water");

        this.foodRate = compound.getInteger("food_rate");
        this.waterRate = compound.getInteger("water_rate");

        this.foodTicks = compound.getInteger("food_ticks");
        this.waterTicks = compound.getInteger("water_ticks");

        this.foodSmellDistance = compound.getInteger("food_smell_distance");

        this.diet.fromNBT(compound.getCompoundTag("diet"));
    }

    @Override
    public void finalizeComponent(ComponentAccess entity) {
        if(entity instanceof EntityLiving) {
            EntityLiving living = (EntityLiving) entity;
            living.tasks.addTask(2, new FeedingAI(entity, (EntityLiving) entity, this));
            living.tasks.addTask(2, new DrinkingAI((EntityLiving) entity, this));
        }
    }

    @Override
    public boolean canBreedWith(ComponentAccess otherEntity) {
        return this.food > 10 && this.water > 5 && otherEntity.get(EntityComponentTypes.METABOLISM).map(m -> m.food > 10 && m.water > 5).orElse(true);
    }

    @Accessors(chain = true)
    @Setter
    public static class Storage implements EntityComponentStorage<MetabolismComponent> {

        // Max Food and water that the ecs can have.
        private int maxFood;
        private int maxWater;
        // Rate that the food and water decrease every second
        private int waterRate = 1;
        private int foodRate = 1;

        //Ticks to eat and drnk
        private int foodTicks;
        private int waterTicks;

        private int distanceSmellFood;

        private FeedingDiet diet = new FeedingDiet(); //todo serialize

        @Override
        public MetabolismComponent constructTo(MetabolismComponent component) {
            component.food = this.maxFood;
            component.water = this.maxWater;
            component.waterRate = this.waterRate;
            component.foodRate = this.foodRate;
            component.foodTicks = this.foodTicks;
            component.waterTicks = this.waterTicks;

            component.foodSmellDistance = this.distanceSmellFood;

            component.diet = this.diet;
            return component;
        }

        @Override
        public void readJson(JsonObject json) {
            this.maxFood = JsonUtils.getInt(json, "max_food");
            this.maxWater = JsonUtils.getInt(json, "max_water");

            this.waterRate = JsonUtils.getInt(json, "water_rate");
            this.foodRate = JsonUtils.getInt(json, "food_rate");

            this.foodTicks = JsonUtils.getInt(json, "food_ticks");
            this.waterTicks = JsonUtils.getInt(json, "water_ticks");

            this.distanceSmellFood = json.get("food_smell_distance").getAsInt();
        }

        @Override
        public void writeJson(JsonObject json) {
            json.addProperty("max_food", this.maxFood);
            json.addProperty("max_water", this.maxWater);

            json.addProperty("water_rate", this.waterRate);
            json.addProperty("food_rate", this.foodRate);

            json.addProperty("food_ticks", this.foodTicks);
            json.addProperty("water_ticks", this.waterTicks);

            json.addProperty("food_smell_distance", this.distanceSmellFood);
        }
    }

}
