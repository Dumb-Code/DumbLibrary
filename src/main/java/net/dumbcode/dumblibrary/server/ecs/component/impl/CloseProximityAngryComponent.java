package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.attributes.ModifiableField;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.GatherEnemiesComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Getter
@Setter
public class CloseProximityAngryComponent extends EntityComponent implements GatherEnemiesComponent {
    private final ModifiableField range = new ModifiableField();
    private Predicate<Entity> predicate = e -> true;
    private UUID angryAtEntity;


    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setTag("Range", this.range.writeToNBT());
        return super.serialize(compound);
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        this.range.readFromNBT(compound.getCompoundTag("Range"));
        super.deserialize(compound);
    }

    @Override
    public void gatherEnemyPredicates(Consumer<Predicate<EntityLivingBase>> registry) {
        registry.accept(e -> e.getUniqueID().equals(this.angryAtEntity));
    }

    @Setter
    @Getter
    @Accessors(chain = true)
    public static class Storage implements EntityComponentStorage<CloseProximityAngryComponent> {
        private float range;

        //TODO-stream: don't just add a blacklist class >:(
        private List<Class<? extends EntityLiving>> blackListClasses = new ArrayList<>();

        @Override
        public void constructTo(CloseProximityAngryComponent component) {
            component.range.setBaseValue(range);
            component.predicate = e -> {
                for(Class<?> claz = e.getClass(); claz != null; claz = claz.getSuperclass()) {
                    if(blackListClasses.contains(claz)) {
                        return false;
                    }
                }
                return true;
            };
        }

        @SuppressWarnings("unchecked")
        public Storage add(Class<? extends EntityLiving>... classes) {
            Collections.addAll(this.blackListClasses, classes);
            return this;
        }

        @Override
        public void writeJson(JsonObject json) {
            json.addProperty("range", this.range);
        }

        @Override
        public void readJson(JsonObject json) {
            this.range = JsonUtils.getFloat(json, "range");
        }
    }
}
