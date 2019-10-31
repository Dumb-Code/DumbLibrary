package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.dna.GeneticEntry;
import net.dumbcode.dumblibrary.server.dna.GeneticType;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.utils.IOCollectors;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

public class GeneticComponent extends EntityComponent {
    private final Map<GeneticType, GeneticEntry> geneticMap = Maps.newHashMap();

    public void setGeneticEntry(GeneticType type, int modifier) {
        modifier = MathHelper.clamp(modifier, 0, 256);
        if(!this.geneticMap.containsKey(type)) {
            DumbLibrary.getLogger().warn("Tried to set illegal type {} with modifier {} to genetics", type.getRegistryName(), modifier);
            return;
        }
        GeneticEntry entry = this.geneticMap.get(type);
        entry.setModifier(modifier);
        type.getOnChange().accept(this.access, entry.getBaseValue() + entry.getModifierRange() * (modifier - 128F) / 128F);
    }

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setTag("genetics", this.geneticMap.values().stream().map(e -> e.serialize(new NBTTagCompound())).collect(IOCollectors.toNBTTagList()));
        return super.serialize(compound);
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        this.geneticMap.clear();
        StreamSupport.stream(compound.getTagList("genetics", Constants.NBT.TAG_COMPOUND).spliterator(), false)
            .map(b -> GeneticEntry.deseraize((NBTTagCompound) b))
            .forEach(e -> this.geneticMap.put(e.getType(), e));
        super.deserialize(compound);
    }


    public static class Storage implements EntityComponentStorage<GeneticComponent> {

        private final List<GeneticEntry> baseEntries = new ArrayList<>();

        public Storage addGeneticEntry(GeneticType type, float baseValue, float modifierRange) {
            this.baseEntries.add(new GeneticEntry(type, baseValue, modifierRange));
            return this;
        }

        @Override
        public GeneticComponent construct() {
            GeneticComponent component = new GeneticComponent();
            baseEntries.stream().map(GeneticEntry::new).forEach(e -> component.geneticMap.put(e.getType(), e));
            return component;
        }

        @Override
        public void readJson(JsonObject json) {
            json.add("genetics", this.baseEntries.stream().map(e -> e.serialize(new JsonObject())).collect(IOCollectors.toJsonArray()));
        }

        @Override
        public void writeJson(JsonObject json) {
            this.baseEntries.clear();
            StreamSupport.stream(JsonUtils.getJsonArray(json, "genetics").spliterator(), false)
                .map(b -> GeneticEntry.deseraize(JsonUtils.getJsonObject(b, "genetic_list_entry")))
                .forEach(this.baseEntries::add);
        }
    }
}
