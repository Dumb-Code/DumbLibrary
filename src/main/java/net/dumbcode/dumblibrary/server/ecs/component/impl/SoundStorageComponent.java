package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.ECSSound;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

@Getter
@Setter
public class SoundStorageComponent extends EntityComponent {

    private final Map<ECSSound, SoundEvent> soundMap = new HashMap<>();

    public Optional<SoundEvent> getSound(ECSSound sound) {
        if(this.soundMap.containsKey(sound)) {
            return Optional.of(this.soundMap.get(sound));
        }
        return Optional.empty();
    }

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        NBTTagCompound tag = new NBTTagCompound();
        this.soundMap.forEach((sound, event) -> tag.setString(sound.getType(), Objects.requireNonNull(event.getRegistryName()).toString()));
        compound.setTag("Sounds", tag);
        return super.serialize(compound);
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        this.soundMap.clear();
        NBTTagCompound tag = compound.getCompoundTag("Sounds");
        for (String s : tag.getKeySet()) {
            this.soundMap.put(new ECSSound(s), ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(tag.getString(s))));
        }
        super.deserialize(compound);
    }

    @Getter
    public static class Storage implements EntityComponentStorage<SoundStorageComponent> {
        private final Map<ECSSound, Supplier<SoundEvent>> soundMap = new HashMap<>();

        public Storage addSound(ECSSound sound, Supplier<SoundEvent> event) {
            this.soundMap.put(sound, event);
            return this;
        }


        @Override
        public void constructTo(SoundStorageComponent component) {
            this.soundMap.forEach((sound, event) -> component.soundMap.put(sound, event.get()));
        }

        //TODO: reading and writing to json

    }
}
