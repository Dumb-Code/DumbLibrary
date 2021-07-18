package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
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
import net.dumbcode.dumblibrary.server.utils.JavaUtils;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class GeneticComponent extends EntityComponent implements FinalizableComponent {
    @Getter
    private final List<GeneticEntry<?>> genetics = new ArrayList<>();

    //Delay adding default genetics to the actual genetic list till we finalize, where we can then use `shouldRandomizeGenetics`
    private final List<GeneticEntry<?>> defaultGenetics = new ArrayList<>();

    private boolean doneGatherGenetics = true;

    private boolean shouldRandomizeGenetics = true;

    public void disableRandomGenetics() {
        this.shouldRandomizeGenetics = false;
    }

    private <T extends GeneticFactoryStorage> void applyChange(ComponentAccess entity, GeneticEntry<T> entry, float modifier) {
        entry.setModifier(modifier);
        entry.getType().getOnChange().apply(entry.getBaseValue() + entry.getModifierRange() * modifier, modifier, entity, entry.getStorage());
    }

    public Optional<GeneticEntry<?>> findEntry(String identifier) {
        return this.getGenetics().stream().filter(g -> g.getIdentifier().equals(identifier)).findFirst();
    }

    private void applyChangeToAll(ComponentAccess entity) {
        for (GeneticEntry<?> value : this.genetics) {
            this.applyChange(entity, value, value.getModifier());
        }
    }

    @Override
    public CompoundNBT serialize(CompoundNBT compound) {
        compound.put("genetics", this.genetics.stream().map(e -> e.serialize(new CompoundNBT())).collect(CollectorUtils.toNBTTagList()));
        return super.serialize(compound);
    }

    @Override
    public void deserialize(CompoundNBT compound) {
        this.genetics.clear();
        StreamUtils.stream(compound.getList("genetics", Constants.NBT.TAG_COMPOUND))
            .map(b -> GeneticEntry.deserialize((CompoundNBT) b))
            .forEach(this.genetics::add);
        super.deserialize(compound);
    }

    public void addGenetics(GeneticEntry<?> entry) {
        this.genetics.add(entry);
    }

    @Override
    public void finalizeComponent(ComponentAccess entity) {
        if(!this.doneGatherGenetics) {
            this.doneGatherGenetics = true;
            for (EntityComponent component : entity.getAllComponents()) {
                if(component instanceof GatherGeneticsComponent) {
                    ((GatherGeneticsComponent) component).gatherGenetics(entity, this.genetics::add, this.shouldRandomizeGenetics);
                }
            }
            for (GeneticEntry<?> genetic : this.defaultGenetics) {
                if(this.shouldRandomizeGenetics) {
                    genetic.setRandomModifier();
                }
                this.genetics.add(genetic);
            }
        }
        this.applyChangeToAll(entity);
    }

    @Override
    public void onCreated(EntityComponentType type, @Nullable EntityComponentStorage storage, @Nullable String storageID) {
        this.doneGatherGenetics = false;
        super.onCreated(type, storage, storageID);
    }

    public static class Storage implements EntityComponentStorage<GeneticComponent> {

        private final List<GeneticEntry<?>> baseEntries = new ArrayList<>();

        public <T extends GeneticFactoryStorage> Storage addGeneticEntry(GeneticType<T> type, String identifier,  float baseValue, float modifierRange) {
            return this.addGeneticEntry(type, identifier, baseValue, modifierRange, t -> {});
        }

        public <T extends GeneticFactoryStorage> Storage addGeneticEntry(GeneticType<T> type, String identifier, float baseValue, float modifierRange, Consumer<T> storageInitializer) {
            this.baseEntries.add(new GeneticEntry<>(type, identifier, JavaUtils.nullApply(type.getStorage().get(), storageInitializer), baseValue, modifierRange));
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
