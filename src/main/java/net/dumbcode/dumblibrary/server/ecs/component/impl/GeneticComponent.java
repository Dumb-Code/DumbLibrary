package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.gson.JsonObject;
import lombok.Getter;
import net.dumbcode.dumblibrary.server.dna.GeneticEntry;
import net.dumbcode.dumblibrary.server.dna.GeneticFactoryStorage;
import net.dumbcode.dumblibrary.server.dna.GeneticType;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentType;
import net.dumbcode.dumblibrary.server.ecs.component.FinalizableComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.GatherGeneticsComponent;
import net.dumbcode.dumblibrary.server.utils.IOCollectors;
import net.dumbcode.dumblibrary.server.utils.JavaUtils;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GeneticComponent extends EntityComponent implements FinalizableComponent {
    @Getter
    private final List<GeneticEntry<?>> genetics = new ArrayList<>();

    private boolean doneGatherGenetics = true;

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
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setTag("genetics", this.genetics.stream().map(e -> e.serialize(new NBTTagCompound())).collect(IOCollectors.toNBTTagList()));
        return super.serialize(compound);
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        this.genetics.clear();
        StreamUtils.stream(compound.getTagList("genetics", Constants.NBT.TAG_COMPOUND))
            .map(b -> GeneticEntry.deserialize((NBTTagCompound) b))
            .forEach(this.genetics::add);
        super.deserialize(compound);
    }

    @Override
    public void finalizeComponent(ComponentAccess entity) {
        if(!this.doneGatherGenetics) {
            this.doneGatherGenetics = true;
            for (EntityComponent component : entity.getAllComponents()) {
                if(component instanceof GatherGeneticsComponent) {
                    ((GatherGeneticsComponent) component).gatherGenetics(entity, this.genetics::add);
                }
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

        private final List<GeneticEntry> baseEntries = new ArrayList<>();

        public <T extends GeneticFactoryStorage> Storage addGeneticEntry(GeneticType<T> type, String identifier,  float baseValue, float modifierRange) {
            return this.addGeneticEntry(type, identifier, baseValue, modifierRange, t -> {});
        }

        public <T extends GeneticFactoryStorage> Storage addGeneticEntry(GeneticType<T> type, String identifier, float baseValue, float modifierRange, Consumer<T> storageInitializer) {
            this.baseEntries.add(new GeneticEntry<>(type, identifier, JavaUtils.nullApply(type.getStorage().get(), storageInitializer), baseValue, modifierRange));
            return this;
        }

        @Override
        public void constructTo(GeneticComponent component) {
            this.baseEntries.stream().map(GeneticEntry::copy).forEach(e -> {
                e.setRandomModifier();
                component.genetics.add(e);
            });
        }

        @Override
        public void readJson(JsonObject json) {
            this.baseEntries.clear();
            StreamUtils.stream(JsonUtils.getJsonArray(json, "genetics"))
                .map(b -> GeneticEntry.deserialize(JsonUtils.getJsonObject(b, "genetic_list_entry")))
                .forEach(this.baseEntries::add);
        }

        @Override
        public void writeJson(JsonObject json) {
            json.add("genetics", this.baseEntries.stream().map(e -> e.serialize(new JsonObject())).collect(IOCollectors.toJsonArray()));
        }
    }
}
