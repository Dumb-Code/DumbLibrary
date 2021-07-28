package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.gson.JsonObject;
import lombok.*;
import net.dumbcode.dumblibrary.server.dna.GeneticEntry;
import net.dumbcode.dumblibrary.server.dna.GeneticTypes;
import net.dumbcode.dumblibrary.server.dna.storages.GeneticTypeLayerColorStorage;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.FinalizableComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.GatherGeneticsComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLayer;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLayerComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLocationComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.data.GeneticLayerEntry;
import net.dumbcode.dumblibrary.server.utils.CollectorUtils;
import net.dumbcode.dumblibrary.server.utils.GeneticUtils;
import net.dumbcode.dumblibrary.server.utils.IndexedObject;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.common.util.Constants;

import java.util.*;
import java.util.function.Consumer;

public class GeneticLayerComponent extends EntityComponent implements RenderLayerComponent, FinalizableComponent, GatherGeneticsComponent {

    @Getter
    private final List<GeneticLayerEntry> entries = new ArrayList<>();

    private RenderLocationComponent.ConfigurableLocation baseLocation = null;

    private static final Random RAND = new Random();

    @Override
    public void gatherLayers(ComponentAccess entity, Consumer<IndexedObject<RenderLayer>> registry) {
        for (GeneticLayerEntry entry : this.entries) {
            registry.accept(new IndexedObject<>(new RenderLayer.DefaultTexture(() -> {
                if(this.baseLocation != null) {
                    float[] colours = entry.getColours();
                    return entry.getTextureLocation(this.baseLocation)
                        .map(r -> new RenderLayer.DefaultLayerData(r, colours[0], colours[1], colours[2], colours[3]))
                        .orElse(null);
                }
                return null;
            }), entry.getIndex()));
        }
    }


    public void setLayerValues(UUID uuid, String layer, float[] rgb) {
        for (GeneticLayerEntry entry : this.entries) {
            if(entry.getLayerName().equals(layer)) {
                entry.addDirectTint(uuid, rgb);
            }
        }
        this.syncToClient();
    }

    @Override
    public void deserialize(CompoundNBT compound) {
        this.entries.clear();
        StreamUtils.stream(compound.getList("entries", Constants.NBT.TAG_COMPOUND))
            .map(b -> GeneticLayerEntry.deserailize((CompoundNBT) b))
            .forEach(this.entries::add);
        super.deserialize(compound);
    }

    @Override
    public CompoundNBT serialize(CompoundNBT compound) {
        compound.put("entries", this.entries.stream().map(g -> GeneticLayerEntry.serialize(g, new CompoundNBT())).collect(CollectorUtils.toNBTTagList()));
        return super.serialize(compound);
    }

    @Override
    public void serialize(PacketBuffer buf) {
        buf.writeShort(this.entries.size());
        for (GeneticLayerEntry entry : this.entries) {
            GeneticLayerEntry.serialize(entry, buf);
        }
        super.serialize(buf);
    }

    @Override
    public void deserialize(PacketBuffer buf) {
        this.entries.clear();
        int size = buf.readShort();
        for (int i = 0; i < size; i++) {
            this.entries.add(GeneticLayerEntry.deserailize(buf));
        }
        super.deserialize(buf);
    }

    @Override
    public void finalizeComponent(ComponentAccess entity) {
        entity.get(EntityComponentTypes.MODEL).ifPresent(m -> this.baseLocation = m.getTexture());
    }

    @Override
    public void gatherGenetics(ComponentAccess entity, Consumer<GeneticEntry<?>> registry, boolean randomGeneticVariation) {
        this.entries
            .forEach(e -> {
                String n = e.getLayerName();
                GeneticEntry<?> entry = new GeneticEntry<>(GeneticTypes.LAYER_COLORS, "genetic_layer_" + n, new GeneticTypeLayerColorStorage().setLayerName(n), 0F, 1F);
                if(randomGeneticVariation) {
                    float cm = e.getDefaultColorMin();
                    float cr = e.getDefaultColorMax() - cm;
                    float am = e.getDefaultAlphaMin();
                    float ar = e.getDefaultAlphaMax() - am;
                    entry.setModifier(GeneticUtils.encodeFloatColor(cm + RAND.nextFloat() * cr, cm + RAND.nextFloat() * cr, cm + RAND.nextFloat() * cr, am + RAND.nextFloat() * ar));

                } else {
                    entry.setModifier(GeneticUtils.encodeFloatColor(1F, 1F, 1F, 1F));
                }
                registry.accept(entry);
            });
    }

    public static class Storage implements EntityComponentStorage<GeneticLayerComponent> {

        private final List<GeneticLayerEntry> entries = new ArrayList<>();

        public Storage addLayer(String name, float index) {
            return this.addLayer(GeneticLayerEntry.builder(name, index));
        }

        public Storage addLayer(GeneticLayerEntry.GeneticLayerEntryBuilder builder) {
            return this.addLayer(builder.build());
        }

        public Storage addLayer(GeneticLayerEntry layer) {
            this.entries.add(layer);
            return this;
        }

        @Override
        public void constructTo(GeneticLayerComponent component) {
            component.entries.addAll(this.entries);
        }

        @Override
        public void writeJson(JsonObject json) {
            json.add("entries", this.entries.stream().map(e -> GeneticLayerEntry.serialize(e, new JsonObject())).collect(CollectorUtils.toJsonArray()));
        }

        @Override
        public void readJson(JsonObject json) {
            this.entries.clear();
            StreamUtils.stream(JSONUtils.getAsJsonArray(json, "entries"))
                .map(e -> GeneticLayerEntry.deserailize(e.getAsJsonObject()))
                .forEach(this.entries::add);
        }
    }

}
