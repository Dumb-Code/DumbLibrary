package net.dumbcode.dumblibrary.server.entity.component;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.Wither;
import net.dumbcode.dumblibrary.server.entity.ComponentWriteAccess;
import net.dumbcode.dumblibrary.server.registry.DumbRegistries;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Getter
public class EntityComponentAttacher {
    private final List<ComponentPair> allPairs = Lists.newArrayList();
    private final ConstructConfiguration defaultConfig = new ConstructConfiguration().withDefaultTypes(true);

    public <T extends EntityComponent, S extends EntityComponentStorage<T>> S addComponent(EntityComponentType<T, S> type) {
        S storage = type.constructStorage();
        this.allPairs.add(new ComponentPair<>(type, storage));
        return storage;
    }

    public JsonArray writeToJson(JsonArray jarr) {
        for (ComponentPair allPair : this.allPairs) {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", allPair.getType().getIdentifier().toString());

            if(allPair.getStorage() != null) {
                JsonObject storageObj = new JsonObject();
                allPair.getStorage().writeJson(storageObj);
                obj.add("storage", storageObj);
            }

            jarr.add(obj);

        }
        return jarr;
    }

    @SuppressWarnings("unchecked")
    public void readFromJson(JsonArray jarr) {
        this.allPairs.clear();
        for (JsonElement element : jarr) {
            JsonObject json = JsonUtils.getJsonObject(element, "attacherEntry");
            EntityComponentType<?, ?> value = DumbRegistries.COMPONENT_REGISTRY.getValue(new ResourceLocation(JsonUtils.getString(json, "name")));
            if(value != null) {
                EntityComponentStorage<?> storage = value.constructStorage();
                if(storage != null) {
                    storage.readJson(JsonUtils.getJsonObject(json, "storage"));
                }
                this.allPairs.add(new ComponentPair(value, storage));
            }
        }
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public <T extends EntityComponent, S extends EntityComponentStorage<T>> S getStorage(EntityComponentType<T, S> type) {
        for (ComponentPair allPair : this.allPairs) {
            if (allPair.type == type) {
                if (allPair.storage == null) {
                    throw new IllegalArgumentException("Requested storage on " + type.getIdentifier() + " but none were found");
                }
                return (S) allPair.storage;
            }
        }
        throw new IllegalArgumentException("Requested storage on component " + type.getIdentifier() + " but component was not attached");
    }

    @Wither
    public class ConstructConfiguration {
        private final boolean defaultTypes;
        private final List<EntityComponentType> addedTypes;
        private final List<EntityComponentType> removedTypes;

        private ConstructConfiguration() {
            this(true, Lists.newArrayList(), Lists.newArrayList());
        }

        public ConstructConfiguration(boolean useDefaultTypes, List<EntityComponentType> addedTypes, List<EntityComponentType> removedTypes) {
            this.defaultTypes = useDefaultTypes;
            this.addedTypes = addedTypes;
            this.removedTypes = removedTypes;
        }

        public ConstructConfiguration withType(EntityComponentType type) {
            List<EntityComponentType> temp = Lists.newArrayList(this.addedTypes);
            temp.add(type);
            return new ConstructConfiguration(this.defaultTypes, Collections.unmodifiableList(temp), this.removedTypes);
        }

        public ConstructConfiguration withoutType(EntityComponentType type) {
            List<EntityComponentType> temp = Lists.newArrayList(this.removedTypes);
            temp.remove(type);
            return new ConstructConfiguration(this.defaultTypes, this.addedTypes, Collections.unmodifiableList(temp));
        }

        public List<ComponentPair> getTypes() {
            List<ComponentPair> out = Lists.newArrayList();
            for (ComponentPair pair : EntityComponentAttacher.this.allPairs) {
                if(this.defaultTypes && pair.type.defaultAttach() && !this.removedTypes.contains(pair.type)) {
                    out.add(pair);
                }
                if(addedTypes.contains(pair.type) && !this.removedTypes.contains(pair.type)) {
                    out.add(pair);
                }
            }
            return out;
        }

        public void attachAll(ComponentWriteAccess cwa) {
            for (ComponentPair type : this.getTypes()) {
                type.attach(cwa);
            }
        }
        
    }

    @Value
    public static class ComponentPair<T extends EntityComponent, S extends EntityComponentStorage<T>> {
        private final EntityComponentType<T, S> type;
        @Nullable
        private final S storage;

        public void attach(ComponentWriteAccess access) {
            access.attachComponent(this.type, this.storage);
        }
    }
}
