package net.dumbcode.dumblibrary.server.ecs.component.impl.data;

import com.google.common.collect.Sets;
import lombok.Getter;

import java.util.Set;
import java.util.function.Supplier;

public class FlattenedLayerProperty {
    private final Supplier<String> currentValue;

    public FlattenedLayerProperty(Supplier<String> currentValue) {
        this.currentValue = currentValue;
    }


    public String currentValue() {
        return this.currentValue.get();
    }

    @Getter
    public static class Static extends FlattenedLayerProperty {
        private final String value;
        public Static(String value) {
            super(() -> value);
            this.value = value;
        }
    }

}