package net.dumbcode.dumblibrary.server.ecs.component.additionals;

import lombok.Value;
import net.dumbcode.dumblibrary.server.ecs.component.impl.SoundStorageComponent;
import net.minecraft.util.SoundEvent;

import java.util.Optional;
import java.util.function.Function;

//todo: remove this inplace for a "audio timeline"
@Value
public class ECSSound implements Function<SoundStorageComponent, Optional<SoundEvent>> {
    String type; //Maybe move to a resource location ?

    @Override
    public Optional<SoundEvent> apply(SoundStorageComponent soundStorageComponent) {
        return soundStorageComponent.getSound(this);
    }
}
