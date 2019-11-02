package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.gson.JsonObject;
import lombok.Getter;
import net.dumbcode.dumblibrary.server.dna.GeneticEntry;
import net.dumbcode.dumblibrary.server.dna.GeneticFactoryStorage;
import net.dumbcode.dumblibrary.server.dna.GeneticType;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.FinalizableComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.GatherGeneticsComponent;
import net.dumbcode.dumblibrary.server.utils.IOCollectors;
import net.dumbcode.dumblibrary.server.utils.JavaUtils;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GeneticComponent extends EntityComponent implements FinalizableComponent {
    @Getter
    private final List<GeneticEntry> genetics = new ArrayList<>();
    private final List<GeneticEntry> serializableTypes = new ArrayList<>();

    private <T extends GeneticFactoryStorage> void applyChange(GeneticEntry<T> entry, int modifier) {
        modifier = MathHelper.clamp(modifier, 0, 255);

        entry.setModifier(modifier);
        entry.getType().getOnChange().apply(entry.getBaseValue() + entry.getModifierRange() * (modifier - 127.5F) / 127.5F, modifier, this.access, entry.getStorage());
    }

    private void applyChangeToAll() {
        for (GeneticEntry<?> value : this.genetics) {
            this.applyChange(value, value.getModifier());
        }
    }

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setTag("genetics", this.serializableTypes.stream().map(e -> e.serialize(new NBTTagCompound())).collect(IOCollectors.toNBTTagList()));
        return super.serialize(compound);
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        this.genetics.clear();
        StreamUtils.stream(compound.getTagList("genetics", Constants.NBT.TAG_COMPOUND))
            .map(b -> GeneticEntry.deserialize((NBTTagCompound) b))
            .forEach(this.genetics::add);
        super.deserialize(compound);
        this.applyChangeToAll();
    }

    @Override
    public void finalizeComponent(ComponentAccess entity) {
        for (EntityComponent component : entity.getAllComponents()) {
            if(component instanceof GatherGeneticsComponent) {
                ((GatherGeneticsComponent) component).gatherGenetics(this.genetics::add);
            }
        }
        this.applyChangeToAll();
    }

    public static class Storage implements EntityComponentStorage<GeneticComponent> {

        private final List<GeneticEntry> baseEntries = new ArrayList<>();

        public <T extends GeneticFactoryStorage> Storage addGeneticEntry(GeneticType<T> type, float baseValue, float modifierRange) {
            return this.addGeneticEntry(type, baseValue, modifierRange, t -> {});
        }

        public <T extends GeneticFactoryStorage> Storage addGeneticEntry(GeneticType<T> type, float baseValue, float modifierRange, Consumer<T> storageInitilizer) {
            this.baseEntries.add(new GeneticEntry<>(type, JavaUtils.nullApply(JavaUtils.nullOr(type.getStorage(), Supplier::get), storageInitilizer), baseValue, modifierRange));
            return this;
        }

        @Override
        public GeneticComponent construct() {
            GeneticComponent component = new GeneticComponent();
            this.baseEntries.stream().map(GeneticEntry::copy).forEach(e -> {
                e.setRandomModifier();
                component.genetics.add(e);
                component.serializableTypes.add(e);
            });
            return component;
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
