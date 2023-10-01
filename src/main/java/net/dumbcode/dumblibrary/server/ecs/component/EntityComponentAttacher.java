package net.dumbcode.dumblibrary.server.ecs.component;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Data;
import lombok.Getter;
import lombok.Value;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.ComponentWriteAccess;
import net.dumbcode.dumblibrary.server.registry.DumbRegistries;
import net.minecraft.util.JSONUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.text.html.parser.Entity;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Getter
public class EntityComponentAttacher {
    private final List<ComponentPair<?, ?>> allPairs = Lists.newArrayList();
    private final ConstructConfiguration defaultConfig = new ConstructConfiguration().withDefaultTypes(true);

    public <T extends EntityComponent, S extends EntityComponentStorage<T>> S addComponent(EntityComponentType<T, S> type) {
        S storage = type.constructStorage();
        this.allPairs.add(new ComponentPair<>(type, storage, ""));
        return storage;
    }

    public <T extends EntityComponent, S extends EntityComponentStorage<T>> S addComponent(EntityComponentType<T, ?> type, EntityComponentType.StorageOverride<T, S> override) {
        S storage = override.getStorage().get();
        this.allPairs.add(new ComponentPair<>(type, storage, override.getStorageID()));
        return storage;
    }

    public JsonArray writeToJson(JsonArray jarr) {
        for (ComponentPair<?, ?> allPair : this.allPairs) {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", allPair.getType().getIdentifier().toString());

            if(allPair.getStorage() != null) {
                JsonObject storageObj = new JsonObject();
                allPair.getStorage().writeJson(storageObj);
                if (!StringUtils.isNullOrEmpty(allPair.storageId)) {
                    storageObj.addProperty("storage_id", allPair.storageId);
                }
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
            JsonObject json = JSONUtils.convertToJsonObject(element, "attacherEntry");
            EntityComponentType<?, ?> value = DumbRegistries.COMPONENT_REGISTRY.getValue(new ResourceLocation(JSONUtils.getAsString(json, "name")));
            if(value != null) {
                JsonObject storageObj = JSONUtils.getAsJsonObject(json, "storage");
                String storageId = JSONUtils.getAsString(storageObj, "storage_id", "");

                EntityComponentStorage<?> storage = value.constructStorage();
                if(!StringUtils.isNullOrEmpty(storageId)) {
                    Map<String, EntityComponentType.StorageOverride<?, ?>> overrideMap = EntityComponentType.StorageOverride.overrides.get(value);
                    if(overrideMap != null && overrideMap.containsKey(storageId)) {
                        storage = overrideMap.get(storageId).construct();
                    }
                }
                if(storage != null) {
                    storage.readJson(storageObj);
                }
                this.allPairs.add(new ComponentPair(value, storage, storageId));
            }
        }
    }

    @Nullable
    public <T extends EntityComponent, S extends EntityComponentStorage<T>> S getStorageOrNull(EntityComponentType<T, S> type) {
        for (ComponentPair<?,?> allPair : this.allPairs) {
            if (allPair.type == type) {
                return (S) allPair.storage;
            }
        }
        return null;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public <T extends EntityComponent, S extends EntityComponentStorage<T>> S getStorage(EntityComponentType<T, S> type) {
        for (ComponentPair<?,?> allPair : this.allPairs) {
            if (allPair.type == type) {
                if (allPair.storage == null) {
                    throw new IllegalArgumentException("Requested storage on " + type.getIdentifier() + " but none were found");
                }
                return (S) allPair.storage;
            }
        }
        throw new IllegalArgumentException("Requested storage on component " + type.getIdentifier() + " but component was not attached");
    }
    @Nonnull
    public <T extends EntityComponent, S extends EntityComponentStorage<T>> S getStorage(Supplier<? extends EntityComponentType<T, ? extends S>> supplier) {
        return this.getStorage(supplier.get());
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public <T extends EntityComponent, S extends EntityComponentStorage<T>> S getStorage(EntityComponentType<T, ?> type, EntityComponentType.StorageOverride<T, S> storage) {
        for (ComponentPair<?, ?> allPair : this.allPairs) {
            if (allPair.type == type) {
                if (allPair.storage == null) {
                    throw new IllegalArgumentException("Requested storage on " + type.getIdentifier() + " but none were found");
                }
                if(allPair.storageId.equals(storage.getStorageID())) {
                    return (S) allPair.storage;
                }
                throw new IllegalArgumentException("Requested storage on \"" + type.getIdentifier() + "\" with override " + storage.getStorageID() + " but override of \"" + allPair.storageId + "\"");
            }
        }
        throw new IllegalArgumentException("Requested storage on component " + type.getIdentifier() + " but component was not attached");
    }

    public static EntityComponentAttacher create(Consumer<EntityComponentAttacher> applier) {
        EntityComponentAttacher attacher = new EntityComponentAttacher();
        applier.accept(attacher);
        return attacher;
    }

    public ConstructConfiguration emptyConfiguration() {
        return new ConstructConfiguration();
    }

    public class ConstructConfiguration {
        private final boolean defaultTypes;
        private final List<EntityComponentType<?, ?>> addedTypes;
        private final List<EntityComponentType<?, ?>> removedTypes;
        private final List<ComponentFinalizeCallback<?>> callbacks;

        private ConstructConfiguration() {
            this(true, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        }

        public ConstructConfiguration(boolean useDefaultTypes, List<EntityComponentType<?, ?>> addedTypes, List<EntityComponentType<?, ?>> removedTypes, List<ComponentFinalizeCallback<?>> callbacks) {
            this.defaultTypes = useDefaultTypes;
            this.addedTypes = addedTypes;
            this.removedTypes = removedTypes;
            this.callbacks = callbacks;
        }

        public ConstructConfiguration withDefaultTypes(boolean defaultTypes) {
            return this.defaultTypes == defaultTypes ? this : new ConstructConfiguration(defaultTypes, this.addedTypes, this.removedTypes, this.callbacks);
        }

        public ConstructConfiguration withType(EntityComponentType<?, ?>... types) {
            List<EntityComponentType<?, ?>> temp = Lists.newArrayList(this.addedTypes);
            Collections.addAll(temp, types);
            return new ConstructConfiguration(this.defaultTypes, Collections.unmodifiableList(temp), this.removedTypes, this.callbacks);
        }

        public ConstructConfiguration withoutType(EntityComponentType<?, ?>... types) {
            List<EntityComponentType<?, ?>> temp = Lists.newArrayList(this.removedTypes);
            Collections.addAll(temp, types);
            return new ConstructConfiguration(this.defaultTypes, this.addedTypes, Collections.unmodifiableList(temp), this.callbacks);
        }

        public <T extends EntityComponent> ConstructConfiguration runBeforeFinalize(EntityComponentType<T, ?> type, Consumer<T> consumer) {
            List<ComponentFinalizeCallback<?>> temp = Lists.newArrayList(this.callbacks);
            temp.add(new ComponentFinalizeCallback<>(type, consumer));
            return new ConstructConfiguration(this.defaultTypes, this.addedTypes, this.removedTypes, Collections.unmodifiableList(temp));
        }

        public List<ComponentPair<?, ?>> getTypes() {
            List<ComponentPair<?, ?>> out = Lists.newArrayList();
            for (ComponentPair<?, ?> pair : EntityComponentAttacher.this.allPairs) {
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
            for (ComponentPair<?, ?> type : this.getTypes()) {
                type.attach(cwa);
            }
            for (ComponentFinalizeCallback<?> callback : this.callbacks) {
                callback.apply(cwa);
            }

            cwa.finalizeComponents();
        }
    }

    @Data
    public static class ComponentPair<T extends EntityComponent, S extends EntityComponentStorage<T>> {
        private final EntityComponentType<T, ?> type;
        @Nullable
        private final S storage;
        private final String storageId;

        public void attach(ComponentWriteAccess access) {
            access.attachComponent(this.type, this.storage, this.storageId);
        }
    }

    @Value
    private static class ComponentFinalizeCallback<T extends EntityComponent> {
        EntityComponentType<T, ?> type;
        Consumer<T> consumer;

        private void apply(ComponentAccess access) {
            access.get(this.type).ifPresent(this.consumer);
        }

    }
}
