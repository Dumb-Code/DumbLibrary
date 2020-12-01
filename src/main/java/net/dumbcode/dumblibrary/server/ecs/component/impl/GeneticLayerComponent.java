package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.client.TextureUtils;
import net.dumbcode.dumblibrary.server.dna.GeneticEntry;
import net.dumbcode.dumblibrary.server.dna.GeneticTypes;
import net.dumbcode.dumblibrary.server.dna.storages.GeneticTypeLayerColorStorage;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.FinalizableComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.GatherGeneticsComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLayerComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLocationComponent;
import net.dumbcode.dumblibrary.server.ecs.component.storge.ShowcasingTextureStorage;
import net.dumbcode.dumblibrary.server.utils.GeneticUtils;
import net.dumbcode.dumblibrary.server.utils.CollectorUtils;
import net.dumbcode.dumblibrary.server.utils.IndexedObject;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import javax.vecmath.Vector3f;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class GeneticLayerComponent extends EntityComponent implements RenderLayerComponent, FinalizableComponent, GatherGeneticsComponent {

    @Getter
    private final List<GeneticLayerEntry> entries = new ArrayList<>();

    private RenderLocationComponent.ConfigurableLocation baseLocation = null;

    private static final Random RAND = new Random();

    @Override
    public void gatherLayers(ComponentAccess entity, Consumer<IndexedObject<Supplier<Layer>>> registry) {
        for (GeneticLayerEntry entry : this.entries) {
            registry.accept(new IndexedObject<>(() -> {
                if(this.baseLocation != null) {
                    float[] colours = entry.getColours();
                    return new Layer(colours[0], colours[1], colours[2], 1F, entry.getTextureLocation(this.baseLocation));
                }
                return null;
            }, entry.index));
        }
    }


    public void setLayerValues(String layer, float[] rgb) {
        for (GeneticLayerEntry entry : this.entries) {
            if(entry.getLayerName().equals(layer)) {
                entry.addDirectTint(new UUID(13, layer.hashCode()), rgb);
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
        compound.setTag("entries", this.entries.stream().map(g -> GeneticLayerEntry.serialize(g, new NBTTagCompound())).collect(CollectorUtils.toNBTTagList()));
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
    public void gatherGenetics(ComponentAccess entity, Consumer<GeneticEntry> registry) {
        this.entries.stream()
            .map(GeneticLayerEntry::getLayerName)
            .forEach(n -> {
                GeneticEntry<?> entry = new GeneticEntry<>(GeneticTypes.LAYER_COLORS, "genetic_layer_" + n, new GeneticTypeLayerColorStorage().setLayerName(n), 128, 128);
                entry.setModifier(GeneticUtils.encode3BitColor(0.7F + RAND.nextFloat() * 0.3F, 0.7F + RAND.nextFloat() * 0.3F, 0.7F + RAND.nextFloat() * 0.3F));
                registry.accept(entry);
            });
    }

    public static class Storage implements EntityComponentStorage<GeneticLayerComponent>, ShowcasingTextureStorage {

        private final List<GeneticLayerEntry> entries = new ArrayList<>();

        public Storage addLayer(String layerName, float index, String... locationsSuffix) {
            this.entries.add(new GeneticLayerEntry(layerName, index, locationsSuffix));
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
            StreamUtils.stream(JsonUtils.getJsonArray(json, "entries"))
                .map(e -> GeneticLayerEntry.deserailize(e.getAsJsonObject()))
                .forEach(this.entries::add);
        }

        @Override
        public void gatherTextures(Consumer<IndexedObject<String>> consumer) {
            for (GeneticLayerEntry entry : this.entries) {
                for (String locationSuffix : entry.getLocationSuffix()) {
                    consumer.accept(new IndexedObject<>(locationSuffix, entry.index));
                }
            }
        }
    }

    //todo:move to own class
    @Data
    @AllArgsConstructor(access = AccessLevel.NONE)
    @Accessors(chain = true)
    public static class GeneticLayerEntry {
        private final String layerName;
        private final float index;
        private final String[] locationSuffix;

        private ResourceLocation textureLocationCache;

        private float[] colorCache = null;
        private final Map<UUID, Vector3f> baseTints = new HashMap<>();
        private final Map<UUID, Vector3f> targetTints = new HashMap<>();
        private Vector3f averageColor = null;

        private GeneticLayerEntry(String layerName, float index, String... locationsSuffix) {
            this.layerName = layerName;
            this.index = index;
            this.locationSuffix = locationsSuffix;
        }

        public ResourceLocation getTextureLocation(RenderLocationComponent.ConfigurableLocation baseLocation) {
            if(this.textureLocationCache == null) {
                this.textureLocationCache = TextureUtils.generateMultipleTexture(Arrays.stream(this.locationSuffix).map(s -> baseLocation.copy().addFileName(s, Integer.MAX_VALUE).getLocation()).toArray(ResourceLocation[]::new));
                this.tryGetAverageColor();
            }
            return this.textureLocationCache;
        }

        private void tryGetAverageColor() {
            ITextureObject texture = Minecraft.getMinecraft().renderEngine.getTexture(this.textureLocationCache);
            if(texture instanceof DynamicTexture) {//Should be true
                int[] data = ((DynamicTexture) texture).getTextureData();
                List<Vec3d> acceptablePixels = new ArrayList<>();
                for (int textureDatum : data) {
                    if(((textureDatum >> 24) & 0xFF) != 0) {
                        acceptablePixels.add(new Vec3d(
                            ((textureDatum >> 16) & 0xFF) / 255F,
                            ((textureDatum >> 8) & 0xFF) / 255F,
                            (textureDatum & 0xFF) / 255F
                        ));
                    }
                }
                int size = acceptablePixels.size();
                acceptablePixels.stream()
                    .reduce(Vec3d::add)
                    .ifPresent(v -> this.setAverageColor((float)v.x/size, (float)v.y/size, (float)v.z/size));
            }
        }

        public float[] getColours() {
            if(this.colorCache != null) {
                return this.colorCache;
            }
            List<Vector3f> tints = new ArrayList<>(this.baseTints.values());
            if(this.averageColor != null) {
                for (Vector3f value : this.targetTints.values()) {
                    tints.add(new Vector3f(
                        value.x / this.averageColor.x,
                        value.y / this.averageColor.y,
                        value.z / this.averageColor.z
                    ));
                }
            }
            if(tints.isEmpty()) {
                return this.colorCache = new float[]{1, 1, 1};
            }
            Vector3f result = new Vector3f();
            tints.forEach(result::add);
            result.scale(1F / tints.size());

            return this.colorCache = new float[]{ result.x, result.y, result.z };
        }

        public void setAverageColor(float r, float b, float g) {
            this.averageColor = new Vector3f(r, g, b);
            this.colorCache = null;
        }

        public void addDirectTint(UUID uuid, float... colours) {
            this.baseTints.put(uuid, new Vector3f(colours));
            this.colorCache = null;
        }

        public void addTargetTint(UUID uuid, float... colours) {
            this.baseTints.put(uuid, new Vector3f(colours));
            this.colorCache = null;
        }

        public static void serialize(GeneticLayerEntry entry, ByteBuf buf) {
            ByteBufUtils.writeUTF8String(buf, entry.layerName);
            buf.writeFloat(entry.index);
            buf.writeShort(entry.getLocationSuffix().length);
            for (String suffix : entry.locationSuffix) {
                ByteBufUtils.writeUTF8String(buf, suffix);
            }
            writeMap(entry.baseTints, buf);
            writeMap(entry.targetTints, buf);
        }

        private static void writeMap(Map<UUID, Vector3f> map, ByteBuf buf) {
            buf.writeByte(map.size());
            map.forEach((uuid, col) -> {
                buf.writeLong(uuid.getMostSignificantBits());
                buf.writeLong(uuid.getLeastSignificantBits());

                buf.writeFloat(col.x);
                buf.writeFloat(col.y);
                buf.writeFloat(col.z);
            });
        }

        public static NBTTagCompound serialize(GeneticLayerEntry entry, NBTTagCompound tag) {
            tag.setString("layer", entry.layerName);
            tag.setFloat("index", entry.index);
            tag.setTag("locations", Arrays.stream(entry.locationSuffix).map(NBTTagString::new).collect(CollectorUtils.toNBTTagList()));

            tag.setTag("BaseTints", writeMap(entry.baseTints));
            tag.setTag("TargetTints", writeMap(entry.targetTints));

            return tag;
        }

        private static NBTTagList writeMap(Map<UUID, Vector3f> map) {
            NBTTagList list = new NBTTagList();
            map.forEach((uuid, col) -> {
                NBTTagCompound nbt = new NBTTagCompound();

                nbt.setUniqueId("UUID", uuid);
                nbt.setFloat("r", col.x);
                nbt.setFloat("g", col.y);
                nbt.setFloat("b", col.z);

                list.appendTag(nbt);
            });
            return list;
        }

        public static JsonObject serialize(GeneticLayerEntry entry, JsonObject json) {
            json.addProperty("layer", entry.layerName);
            json.addProperty("index", entry.index);
            json.add("locations", Arrays.stream(entry.locationSuffix).map(JsonPrimitive::new).collect(CollectorUtils.toJsonArray()));
            return json;
        }

        public static GeneticLayerEntry deserailize(NBTTagCompound nbt) {
            GeneticLayerEntry entry = new GeneticLayerEntry(
                nbt.getString("layer"),
                nbt.getFloat("index"),
                StreamUtils.stream(nbt.getTagList("locations", Constants.NBT.TAG_STRING)).map(b -> ((NBTTagString) b).getString()).toArray(String[]::new)
            );

            readMap(nbt.getTagList("BaseTints", 10), entry::addDirectTint);
            readMap(nbt.getTagList("TargetTints", 10), entry::addTargetTint);

            return entry;
        }

        private static void readMap(NBTTagList list, BiConsumer<UUID, float[]> consumer) {
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound tag = list.getCompoundTagAt(i);
                consumer.accept(
                    tag.getUniqueId("uuid"),
                    new float[]{ tag.getFloat("r"), tag.getFloat("g"), tag.getFloat("b") }
                );
            }
        }

        public static GeneticLayerEntry deserailize(JsonObject json) {
            return new GeneticLayerEntry(
                JsonUtils.getString(json, "layer"),
                JsonUtils.getFloat(json, "index"),
                StreamUtils.stream(JsonUtils.getJsonArray(json, "locations")).map(JsonElement::getAsString).toArray(String[]::new)
            );
        }

        public static GeneticLayerEntry deserailize(ByteBuf buf) {
            GeneticLayerEntry entry = new GeneticLayerEntry(
                ByteBufUtils.readUTF8String(buf),
                buf.readFloat(),
                IntStream.range(0, buf.readShort()).mapToObj(i -> ByteBufUtils.readUTF8String(buf)).toArray(String[]::new)
            );

            readMap(buf, entry::addDirectTint);
            readMap(buf, entry::addTargetTint);

            return entry;
        }

        private static void readMap(ByteBuf buf, BiConsumer<UUID, float[]> consumer) {
            byte size = buf.readByte();
            for (byte i = 0; i < size; i++) {
                consumer.accept(
                    new UUID(buf.readLong(), buf.readLong()),
                    new float[]{ buf.readFloat(), buf.readFloat(), buf.readFloat() }
                );
            }
        }
    }
}
