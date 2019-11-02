package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.dna.GeneticEntry;
import net.dumbcode.dumblibrary.server.dna.GeneticTypes;
import net.dumbcode.dumblibrary.server.dna.storages.GeneticTypeLayerColorStorage;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.*;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.GatherGeneticsComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLayerComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLocationComponent;
import net.dumbcode.dumblibrary.server.utils.IOCollectors;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class GeneticLayerComponent extends EntityComponent implements RenderLayerComponent, FinalizableComponent, GatherGeneticsComponent {

    private final List<GeneticLayerEntry> entries = new ArrayList<>();

    private static final TextureManager RENDER_ENGINE = Minecraft.getMinecraft().renderEngine;

    private RenderLocationComponent.ConfigurableLocation baseLocation = null;

    @Override
    public void gatherLayers(Consumer<Consumer<Runnable>> registry) {
        for (GeneticLayerEntry entry : this.entries) {
            registry.accept(runnable -> {
                if(this.baseLocation != null) {
                    RenderLocationComponent.ConfigurableLocation copy = this.baseLocation.copy();
                    copy.addFileName(entry.locationSuffix, Integer.MAX_VALUE);
                    ResourceLocation location = copy.getLocation();
                    RENDER_ENGINE.bindTexture(location);
                    GlStateManager.color(entry.colours[0], entry.colours[1], entry.colours[2], 1F);
                    runnable.run();
                    GlStateManager.color(1F, 1F, 1F, 1F);
                }
            });
        }
    }

    public void setLayerValues(String layer, int value) { //0 -> 256
        float percentage = value / 255F;
        for (GeneticLayerEntry entry : this.entries) {
            if(entry.getLayerName().equals(layer)) {
                int[] range = entry.getColourRange();
                entry.setColours(new float[] {
                    (range[0] + (range[1] - range[0]) * percentage) / 255F,
                    (range[2] + (range[3] - range[2]) * percentage) / 255F,
                    (range[4] + (range[5] - range[4]) * percentage) / 255F
                });
            }
        }
        this.syncToClient();
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        this.entries.clear();
        StreamUtils.stream(compound.getTagList("entries", Constants.NBT.TAG_COMPOUND))
            .map(b -> GeneticLayerEntry.deserailize((NBTTagCompound) b))
            .forEach(this.entries::add);
        super.deserialize(compound);
    }

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setTag("entries", this.entries.stream().map(g -> GeneticLayerEntry.serialize(g, new NBTTagCompound())).collect(IOCollectors.toNBTTagList()));
        return super.serialize(compound);
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeShort(this.entries.size());
        for (GeneticLayerEntry entry : this.entries) {
            GeneticLayerEntry.serialize(entry, buf);
        }
        super.serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
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
    public void gatherGenetics(Consumer<GeneticEntry> registry) {
        this.entries.stream()
            .map(GeneticLayerEntry::getLayerName)
            .forEach(n -> registry.accept(new GeneticEntry<>(GeneticTypes.LAYER_COLORS, new GeneticTypeLayerColorStorage().setLayerName(n), 128, 128).setRandomModifier()));
    }

    public static class Storage implements EntityComponentStorage<GeneticLayerComponent> {

        private final List<GeneticLayerEntry> entries = new ArrayList<>();

        public Storage addLayer(String layerName, String locationSuffix, int redStart, int redEnd, int blueStart, int blueEnd, int greenStart, int greenEnd) {
            this.entries.add(new GeneticLayerEntry(layerName, locationSuffix, redStart, redEnd, blueStart, blueEnd, greenStart, greenEnd));
            return this;
        }

        @Override
        public GeneticLayerComponent construct() {
            GeneticLayerComponent component = new GeneticLayerComponent();
            component.entries.addAll(this.entries);
            return component;
        }

        @Override
        public void writeJson(JsonObject json) {
            json.add("entries", this.entries.stream().map(e -> GeneticLayerEntry.serialize(e, new JsonObject())).collect(IOCollectors.toJsonArray()));
        }

        @Override
        public void readJson(JsonObject json) {
            this.entries.clear();
            StreamUtils.stream(JsonUtils.getJsonArray(json, "entries"))
                .map(e -> GeneticLayerEntry.deserailize(e.getAsJsonObject()))
                .forEach(this.entries::add);
        }
    }

    //todo:move to own class
    @Data
    @Accessors(chain = true)
    private static class GeneticLayerEntry {
        private final String layerName;
        private final String locationSuffix;

        private final int[] colourRange; //[redStart, redEnd, blueStart, blueEnd, greenStart, greenEnd]

        private float[] colours = { 1, 1, 1 }; //Only used clientside.

        private GeneticLayerEntry(String layerName, String locationSuffix, int[] colourRange) {
            this.layerName = layerName;
            this.locationSuffix = locationSuffix;
            this.colourRange = colourRange;
        }

        private GeneticLayerEntry(String layerName, String locationSuffix, int redStart, int redEnd, int blueStart, int blueEnd, int greenStart, int greenEnd) {
            this(layerName, locationSuffix, new int[]{ redStart, redEnd, blueStart, blueEnd, greenStart, greenEnd });
        }

        public static void serialize(GeneticLayerEntry entry, ByteBuf buf) {
            ByteBufUtils.writeUTF8String(buf, entry.layerName);
            ByteBufUtils.writeUTF8String(buf, entry.locationSuffix);

            for (int i : entry.colourRange) {
                buf.writeShort(i);
            }

            for (float colour : entry.colours) {
                buf.writeFloat(colour);
            }
        }

        public static NBTTagCompound serialize(GeneticLayerEntry entry, NBTTagCompound tag) {
            tag.setString("layer", entry.layerName);
            tag.setString("location", entry.locationSuffix);
            tag.setIntArray("colour_range", entry.colourRange);

            tag.setFloat("colour_r", entry.colours[0]);
            tag.setFloat("colour_g", entry.colours[1]);
            tag.setFloat("colour_b", entry.colours[2]);
            return tag;
        }

        public static JsonObject serialize(GeneticLayerEntry entry, JsonObject json) {
            json.addProperty("layer", entry.layerName);
            json.addProperty("location", entry.locationSuffix);
            json.add("colour_range", Arrays.stream(entry.colourRange).mapToObj(JsonPrimitive::new).collect(IOCollectors.toJsonArray()));
            return json;
        }

        public static GeneticLayerEntry deserailize(NBTTagCompound nbt) {
            return new GeneticLayerEntry(
                nbt.getString("layer"),
                nbt.getString("location"),
                nbt.getIntArray("colour_range")
            ).setColours(new float[]{ nbt.getFloat("colour_r"), nbt.getFloat("colour_g"), nbt.getFloat("colour_b") });
        }

        public static GeneticLayerEntry deserailize(JsonObject json) {
            return new GeneticLayerEntry(
                JsonUtils.getString(json, "layer"),
                JsonUtils.getString(json, "location"),
                StreamUtils.stream(JsonUtils.getJsonArray(json, "colour_range")).mapToInt(JsonElement::getAsInt).toArray()
            );
        }

        public static GeneticLayerEntry deserailize(ByteBuf buf) {
            return new GeneticLayerEntry(
                ByteBufUtils.readUTF8String(buf),
                ByteBufUtils.readUTF8String(buf),
                new int[]{ buf.readShort(), buf.readShort(), buf.readShort(), buf.readShort(), buf.readShort(), buf.readShort() }
            ).setColours(new float[]{ buf.readFloat(), buf.readFloat(), buf.readFloat() });
        }

    }
}
