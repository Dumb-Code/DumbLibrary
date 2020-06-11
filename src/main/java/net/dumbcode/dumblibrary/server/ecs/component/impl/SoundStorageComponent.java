package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.ECSSound;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.*;
import java.util.function.Supplier;

@Getter
@Setter
public class SoundStorageComponent extends EntityComponent {

    private static final Random RAND = new Random();

    private final Map<ECSSound, SoundEvent[]> soundMap = new HashMap<>();

    public Optional<SoundEvent> getSound(ECSSound sound) {
        if(this.soundMap.containsKey(sound)) {
            SoundEvent[] events = this.soundMap.get(sound);
            if(events.length > 0) {
                return Optional.of(events[RAND.nextInt(events.length)]);
            }
        }
        return Optional.empty();
    }

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        NBTTagCompound tag = new NBTTagCompound();
        this.soundMap.forEach((sound, events) -> {
            NBTTagList list = new NBTTagList();
            for (SoundEvent event : events) {
                list.appendTag(new NBTTagString(Objects.requireNonNull(event.getRegistryName()).toString()));
            }
            tag.setTag(sound.getType(), list);
        });
        compound.setTag("Sounds", tag);
        return super.serialize(compound);
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        this.soundMap.clear();
        NBTTagCompound tag = compound.getCompoundTag("Sounds");
        for (String s : tag.getKeySet()) {
            NBTTagList list = tag.getTagList(s, Constants.NBT.TAG_STRING);
            List<SoundEvent> events = new ArrayList<>();
            for (NBTBase base : list) {
                events.add(ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(((NBTTagString)base).getString())));
            }
            this.soundMap.put(new ECSSound(s), events.toArray(new SoundEvent[0]));
        }
        super.deserialize(compound);
    }

    @Getter
    public static class Storage implements EntityComponentStorage<SoundStorageComponent> {
        private final Map<ECSSound, Supplier<SoundEvent>[]> soundMap = new HashMap<>();

        public Storage addSound(ECSSound sound, Supplier<SoundEvent>... event) {
            this.soundMap.put(sound, event);
            return this;
        }


        @Override
        public void constructTo(SoundStorageComponent component) {
            this.soundMap.forEach((sound, event) -> component.soundMap.put(sound, Arrays.stream(event).map(Supplier::get).toArray(SoundEvent[]::new)));
        }

        //TODO: reading and writing to json

    }
}
