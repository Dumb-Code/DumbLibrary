package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.attributes.ModifiableField;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.SaveableEntityStorage;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.GatherEnemiesComponent;
import net.dumbcode.dumblibrary.server.utils.CollectorUtils;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.JSONUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
@Setter
public class CloseProximityAngryComponent extends EntityComponent implements GatherEnemiesComponent {
    private final ModifiableField range = new ModifiableField();
    private Predicate<Entity> predicate = e -> true;
    private UUID angryAtEntity;


    @Override
    public CompoundNBT serialize(CompoundNBT compound) {
        compound.put("Range", this.range.writeToNBT());
        return super.serialize(compound);
    }

    @Override
    public void deserialize(CompoundNBT compound) {
        this.range.readFromNBT(compound.getCompound("Range"));
        super.deserialize(compound);
    }

    @Override
    public void gatherEnemyPredicates(Consumer<Predicate<LivingEntity>> registry) {
        registry.accept(e -> e.getUUID().equals(this.angryAtEntity));
    }

    @Setter
    @Getter
    @Accessors(chain = true)
    public static class Storage implements EntityComponentStorage<CloseProximityAngryComponent> {
        protected float range;

        @Override
        public void constructTo(CloseProximityAngryComponent component) {
            component.range.setBaseValue(range);
        }

        @Override
        public void writeJson(JsonObject json) {
            json.addProperty("range", this.range);
        }

        @Override
        public void readJson(JsonObject json) {
            this.range = JSONUtils.getAsFloat(json, "range");
        }
    }
}
