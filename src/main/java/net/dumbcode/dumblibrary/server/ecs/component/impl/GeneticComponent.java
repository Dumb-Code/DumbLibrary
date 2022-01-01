package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import net.dumbcode.dumblibrary.server.dna.GeneticEntry;
import net.dumbcode.dumblibrary.server.dna.GeneticFactoryStorage;
import net.dumbcode.dumblibrary.server.dna.GeneticType;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentType;
import net.dumbcode.dumblibrary.server.ecs.component.FinalizableComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.GatherGeneticsComponent;
import net.dumbcode.dumblibrary.server.utils.CollectorUtils;
import net.dumbcode.dumblibrary.server.utils.GeneticUtils;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class GeneticComponent extends EntityComponent implements FinalizableComponent {
//    private final List<GeneticEntry<?, ?>> genetics = new ArrayList<>();
    private final Map<GeneticType<?, ?>, Map<Object, GeneticEntry<?, ?>>> genetics = new HashMap<>(); //<Type, <CombinerKey, Genetic>>

    //Delay adding default genetics to the actual genetic list till we finalize, where we can then use `shouldRandomizeGenetics`
    private final List<GeneticEntry<?, ?>> defaultGenetics = new ArrayList<>();

    private boolean doneGatherGenetics = true;

    private boolean shouldRandomizeGenetics = true;

    public void disableRandomGenetics() {
        this.shouldRandomizeGenetics = false;
    }

    private <T extends GeneticFactoryStorage<O>, O> void applyChange(ComponentAccess entity, GeneticEntry<T, O> entry) {
        entry.getType().getOnChange().apply(entry.getModifier(), entity, entry.getStorage());
    }

    private <T extends GeneticFactoryStorage<O>, O> void applyRemove(ComponentAccess entity, GeneticEntry<T, O> entry) {
        entry.getType().getOnRemove().apply(entry.getModifier(), entity, entry.getStorage());
    }

    public List<GeneticEntry<?, ?>> getGenetics() {
        return this.genetics.values().stream().flatMap(m -> m.values().stream()).collect(Collectors.toList());
    }

    public <T extends GeneticFactoryStorage<?>> Optional<GeneticEntry<?, ?>> findEntry(GeneticEntry<T, ?> entry) {
        Map<Object, GeneticEntry<?, ?>> map = this.genetics.get(entry.getType());
        if(map == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(map.get(GeneticUtils.getCombinerKey(entry)));
    }

    private void applyChangeToAll(ComponentAccess entity) {
        for (GeneticEntry<?, ?> value : this.getGenetics()) {
            this.applyChange(entity, value);
        }
    }

    public <T extends GeneticFactoryStorage<?>> void insertGenetic(GeneticEntry<T, ?> entry) {
        Map<Object, GeneticEntry<?, ?>> map = this.genetics.computeIfAbsent(entry.getType(), t -> Maps.newHashMap());
        map.put(GeneticUtils.getCombinerKey(entry), entry);
    }

    @Override
    public CompoundNBT serialize(CompoundNBT compound) {
        compound.put("genetics", this.getGenetics().stream().map(e -> e.serialize(new CompoundNBT())).collect(CollectorUtils.toNBTTagList()));
        return super.serialize(compound);
    }

    @Override
    public void deserialize(CompoundNBT compound) {
        this.genetics.clear();
        StreamUtils.stream(compound.getList("genetics", Constants.NBT.TAG_COMPOUND))
            .map(b -> GeneticEntry.deserialize((CompoundNBT) b))
            .forEach(this::insertGenetic);
        super.deserialize(compound);
    }

    @Override
    public void finalizeComponent(ComponentAccess entity) {
        if(!this.doneGatherGenetics) {
            Random random = new Random();
            this.doneGatherGenetics = true;
            List<GeneticEntry<?, ?>> toAdd = this.getGenetics();
            for (EntityComponent component : entity.getAllComponents()) {
                if(component instanceof GatherGeneticsComponent) {
                    ((GatherGeneticsComponent) component).gatherGenetics(entity, toAdd::add, this.shouldRandomizeGenetics);
                }
            }
            for (GeneticEntry<?, ?> genetic : this.defaultGenetics) {
                if(this.shouldRandomizeGenetics) {
                    genetic.setRandomModifier(random);
                }
                toAdd.add(genetic);
            }
            GeneticUtils.combineAll(toAdd).forEach(this::insertGenetic);
        }
        this.applyChangeToAll(entity);
    }

    public void useExistingGenetics() {
        this.doneGatherGenetics = true;
    }

    @Override
    public void onCreated(EntityComponentType type, @Nullable EntityComponentStorage storage, @Nullable String storageID) {
        this.doneGatherGenetics = false;
        super.onCreated(type, storage, storageID);
    }

    public static void mutateGenes(GeneticComponent component, ComponentAccess access) {
        Random random = new Random();
        for (GeneticEntry<?, ?> genetic : component.getGenetics()) {
            genetic.mutateModifier(random, 0.1F);
        }
        component.applyChangeToAll(access);
    }

    /**
     * Replaces the genetics on component `component`.
     * `entries` has to be combined.
     */
    public static void replaceGenetics(GeneticComponent component, ComponentAccess access, List<GeneticEntry<?, ?>> entries) {
        for (GeneticEntry<?, ?> genetic : component.getGenetics()) {
            component.applyRemove(access, genetic);
        }
        component.genetics.clear();
        for (GeneticEntry<?, ?> entry : entries) {
            component.insertGenetic(entry);
        }
        component.applyChangeToAll(access);
    }

    public static class Storage implements EntityComponentStorage<GeneticComponent> {

        private final List<GeneticEntry<?, ?>> baseEntries = new ArrayList<>();

        public <T extends GeneticFactoryStorage<Float>> Storage addGeneticEntry(GeneticType<T, Float> type) {
            return this.addGeneticEntry(type, 0F);
        }

        public <T extends GeneticFactoryStorage<O>, O> Storage addGeneticEntry(GeneticType<T, O> type, O defaultValue) {
            return this.addGeneticEntry(type, type.getStorage().get(), defaultValue);
        }

        public <T extends GeneticFactoryStorage<O>, O> Storage addGeneticEntry(GeneticType<T, O> type, T storage, O defaultValue) {
            this.baseEntries.add(new GeneticEntry<>(type, storage).setModifier(defaultValue));
            return this;
        }

        @Override
        public void constructTo(GeneticComponent component) {
            this.baseEntries.stream().map(GeneticEntry::copy).forEach(component.defaultGenetics::add);
        }

        @Override
        public void readJson(JsonObject json) {
            this.baseEntries.clear();
            StreamUtils.stream(JSONUtils.getAsJsonArray(json, "genetics"))
                .map(b -> GeneticEntry.deserialize(JSONUtils.convertToJsonObject(b, "genetic_list_entry")))
                .forEach(this.baseEntries::add);
        }

        @Override
        public void writeJson(JsonObject json) {
            json.add("genetics", this.baseEntries.stream().map(e -> e.serialize(new JsonObject())).collect(CollectorUtils.toJsonArray()));
        }
    }
}
