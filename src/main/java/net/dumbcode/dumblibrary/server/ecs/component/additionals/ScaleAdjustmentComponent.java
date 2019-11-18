package net.dumbcode.dumblibrary.server.ecs.component.additionals;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface ScaleAdjustmentComponent {
    void applyScale(Consumer<Supplier<Float>> scale);
}
