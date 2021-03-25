package net.dumbcode.dumblibrary.client.model.command;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraft.block.BlockState;
import net.minecraft.state.Property;
import net.minecraft.util.JSONUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class CommandDerived {

    public static final Map<String, DerivedKey<?>> DERIVED_TYPE_MAP = new HashMap<>();

    public static final DerivedKey<BlockState> BLOCKSTATE = new DerivedKey<>(BlockState.class, "blockstate", object -> {
        String key = JSONUtils.getAsString(object, "blockstate_key");
        return new DerivedType<>(value -> {
            for (Property<?> property : value.getProperties()) {
                if(property.getName().equals(key)) {
                    return value.getValue(property);
                }
            }
            DumbLibrary.getLogger().warn("Unable to find property of type " + key);
            return null;
        });
    });

    @Data
    public static class DerivedKey<T> {
        private final Class<T> clazz;
        private final String name;
        private final Function<JsonObject, DerivedType<T>> function;

        public DerivedKey(Class<T> clazz, String name, Function<JsonObject, DerivedType<T>> function) {
            this.clazz = clazz;
            this.name = name;
            this.function = function;
            DERIVED_TYPE_MAP.put(name, this);
        }
    }

    @Data
    public static class DerivedType<T> {
        private final Function<T, Object> function;
    }

    @Data
    public static class Derived<T> {
        private final DerivedKey<T> key;
        private final DerivedType<T> value;
    }

    public static Derived<?> getType(JsonObject object) {
        DerivedKey<?> key = DERIVED_TYPE_MAP.get(JSONUtils.getAsString(object, "derived_from"));
        if(key == null) {
            return null;
        }
        return new Derived(key, key.function.apply(object));
    }

    @AllArgsConstructor
    public static class DerivedContext {
        private final Map<DerivedKey<?>, Object> derivedMap = new HashMap<>();

        public <T> void set(DerivedKey<T> key, T object) {
            this.derivedMap.put(key, object);
        }

        @SuppressWarnings("unchecked")
        private <T> T get(DerivedKey<T> key) {
            return (T) this.derivedMap.get(key);
        }

        <T> Object get(Derived<T> derived) {
            T t = this.get(derived.getKey());
            if(t == null) {
                return null;
            }
            return derived.getValue().getFunction().apply(t);
        }

        public ExpectedVariableResolver createResolver(ModelCommandLoader.CommandEntry entry) {
            return new ExpectedVariableResolver(this, entry);
        }
    }

    @AllArgsConstructor
    public static class ExpectedVariableResolver {
        private final DerivedContext context;
        private final ModelCommandLoader.CommandEntry command;
        public <T> T get(ModelCommandRegistry.ExpectedVariable<T> variable) {
            return get(variable, null);
        }

        public <T> T get(ModelCommandRegistry.ExpectedVariable<T> variable, T fallback) {
            if(variable.getFoundValue() != null) {
                return variable.getFoundValue();
            }
            Object o = this.context.get(this.command.getValueSupplier().get(variable.getVariableName()));
            if(variable.getType().getClazz().isInstance(o)) {
                return (T) o;
            }
            DumbLibrary.getLogger().error("Unable to get expected model variable " + variable.getVariableName() + ". Found object " + o + " of class " + (o == null ? "null" : o.getClass().getSimpleName()));
            return fallback;
        }
    }
}
