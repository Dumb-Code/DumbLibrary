package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.gson.JsonObject;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.HerdSavedData;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.FinalizableComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.CanBreedComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.GatherEnemiesComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class HerdComponent extends EntityComponent implements FinalizableComponent, CanBreedComponent, GatherEnemiesComponent {

    public UUID herdUUID;
    public ResourceLocation herdTypeID;

    private HerdSavedData herdData;

    @Override
    public CompoundNBT serialize(CompoundNBT compound) {
        if(this.herdUUID != null) {
            compound.putUUID("uuid", this.herdUUID);
        }

        compound.putString("herd_type_id", this.herdTypeID.toString());

        return compound;
    }

    @Override
    public void deserialize(CompoundNBT compound) {
        if(compound.hasUUID("uuid")) {
            this.herdUUID = compound.getUUID("uuid");
        } else {
            this.herdUUID = null;
        }

        this.herdTypeID = new ResourceLocation(compound.getString("herd_type_id"));
    }

    @Override
    public void finalizeComponent(ComponentAccess entity) {
        if(entity instanceof Entity) {
            if(entity instanceof LivingEntity) {
                ModifiableAttributeInstance attribute = ((LivingEntity) entity).getAttribute(Attributes.FOLLOW_RANGE);
                if(attribute != null) {
                    attribute.setBaseValue(120.0D);
                }
            }
        }

    }

    public void clearHerd() {
        this.herdData = null;
    }

    public boolean isInHerd() {
        return this.herdUUID != null;
    }

    public void setHerdData(@NonNull HerdSavedData herdData) {
        this.herdData = herdData;
        this.herdUUID = herdData.getHerdUUID();
    }

    public Optional<HerdSavedData> getHerdData() {
        if(this.herdData == null && this.herdUUID != null) {
            this.herdData = HerdSavedData.getData(this.herdUUID);
        }
        return Optional.ofNullable(this.herdData);
    }

    @Override
    public boolean canBreedWith(ComponentAccess otherEntity) {
        return otherEntity.get(EntityComponentTypes.HERD).map(h -> Objects.equals(this.herdUUID, h.herdUUID)).orElse(true);
    }

    @Override
    public void gatherEnemyPredicates(Consumer<Predicate<LivingEntity>> registry) {
        registry.accept(e -> this.getHerdData().map(h -> h.getEnemies().contains(e.getUUID())).orElse(false));
    }

    @Accessors(chain = true)
    @Setter
    public static class Storage implements EntityComponentStorage<HerdComponent> {

        private ResourceLocation herdTypeID; //Used to check if entities are compatible with other herds ect.

        @Override
        public void constructTo(HerdComponent component) {
            component.herdTypeID = this.herdTypeID;
        }

        @Override
        public void readJson(JsonObject json) {
            this.herdTypeID = new ResourceLocation(json.get("herd_type_id").getAsString());
        }

        @Override
        public void writeJson(JsonObject json) {
            json.addProperty("herd_type_id", this.herdTypeID.toString());
        }
    }
}
