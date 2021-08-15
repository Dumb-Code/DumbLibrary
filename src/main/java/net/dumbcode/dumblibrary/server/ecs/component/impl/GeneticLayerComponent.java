package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.gson.JsonObject;
import lombok.*;
import net.dumbcode.dumblibrary.server.dna.GeneticEntry;
import net.dumbcode.dumblibrary.server.dna.GeneticTypes;
import net.dumbcode.dumblibrary.server.dna.data.ColouredGeneticDataHandler;
import net.dumbcode.dumblibrary.server.dna.data.GeneticTint;
import net.dumbcode.dumblibrary.server.dna.storages.GeneticColorStorage;
import net.dumbcode.dumblibrary.server.dna.storages.GeneticLayerColorStorage;
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
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.common.util.Constants;
import org.apache.logging.log4j.util.BiConsumer;

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


    public void setLayerValues(UUID uuid, String layer, GeneticTint.Part part) {
        for (GeneticLayerEntry entry : this.entries) {
            if(entry.getLayerName().equals(layer)) {
                entry.addDirectTint(uuid, part);
            }
        }
        this.syncToClient();
    }

    public void removeLayerValues(UUID uuid, String layer) {
        for (GeneticLayerEntry entry : this.entries) {
            if(entry.getLayerName().equals(layer)) {
                entry.removeTargetTint(uuid);
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
    public void gatherGenetics(ComponentAccess entity, Consumer<GeneticEntry<?, ?>> registry, boolean randomGeneticVariation) {
        this.entries
            .forEach(e -> {
                String n = e.getLayerName();
                GeneticEntry<GeneticLayerColorStorage, GeneticTint> entry = new GeneticEntry<>(GeneticTypes.LAYER_COLORS, new GeneticLayerColorStorage().setLayerName(n));
                if(randomGeneticVariation) {
                    float cm = e.getColourMutate();
                    float cr = 1 - cm;
                    float am = e.getAlphaMutate();
                    float ar = 1 - am;
                    entry.setModifier(new GeneticTint(
                        new GeneticTint.Part(
                            cm + RAND.nextFloat() * cr, cm + RAND.nextFloat() * cr, cm + RAND.nextFloat() * cr, am + RAND.nextFloat() * ar, GeneticUtils.DEFAULT_COLOUR_IMPORTANCE
                        ),
                        new GeneticTint.Part(
                            cm + RAND.nextFloat() * cr, cm + RAND.nextFloat() * cr, cm + RAND.nextFloat() * cr, am + RAND.nextFloat() * ar, GeneticUtils.DEFAULT_COLOUR_IMPORTANCE
                        )
                    ));

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
            for (GeneticLayerEntry entry : this.entries) {
                component.entries.add(entry.cloneForEntity());
            }
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
