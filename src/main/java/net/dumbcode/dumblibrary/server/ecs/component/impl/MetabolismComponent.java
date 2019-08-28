package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.gson.JsonObject;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.ai.DrinkingAI;
import net.dumbcode.dumblibrary.server.ecs.ai.FeedingAI;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.FinalizableComponent;
import net.dumbcode.dumblibrary.server.ecs.objects.FeedingDiet;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;

public class MetabolismComponent extends EntityComponent implements FinalizableComponent {
    public int food;
    public int water;

    public int foodRate;
    public int waterRate;

    public FeedingDiet diet = new FeedingDiet();
    public int foodSmellDistance;
    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setInteger("food", this.food);
        compound.setInteger("water", this.water);

        compound.setTag("diet", this.diet.writeToNBT(new NBTTagCompound()));
        return super.serialize(compound);
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        super.deserialize(compound);
        this.food = compound.getInteger("food");
        this.water = compound.getInteger("water");

        this.diet.fromNBT(compound.getCompoundTag("diet"));
    }

    @Override
    public void finalizeComponent(ComponentAccess entity) {
        if(entity instanceof EntityLiving) {
            EntityLiving living = (EntityLiving) entity;
            living.tasks.addTask(2, new FeedingAI((EntityLiving) entity, this));
            living.tasks.addTask(2, new DrinkingAI((EntityLiving) entity, this));
        }
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

        private int distanceSmellFood;

        private FeedingDiet diet = new FeedingDiet(); //todo serialize

        @Override
        public MetabolismComponent construct() {
            MetabolismComponent component = new MetabolismComponent();
            component.food = this.maxFood;
            component.water = this.maxWater;
            component.waterRate = this.waterRate;
            component.foodRate = this.foodRate;

            component.foodSmellDistance = this.distanceSmellFood;

            component.diet = this.diet;
            return component;
        }

        @Override
        public void readJson(JsonObject json) {
            this.maxFood = json.get("max_food").getAsInt();
            this.maxWater = json.get("max_water").getAsInt();
            this.waterRate = json.get("water_rate").getAsInt();
            this.foodRate = json.get("food_rate").getAsInt();

            this.distanceSmellFood = json.get("food_smell_distance").getAsInt();
        }

        @Override
        public void writeJson(JsonObject json) {
            json.addProperty("max_food", this.maxFood);
            json.addProperty("max_water", this.maxWater);
            json.addProperty("water_rate", this.waterRate);
            json.addProperty("food_rate", this.foodRate);

            json.addProperty("food_smell_distance", this.distanceSmellFood);
        }
    }

}
