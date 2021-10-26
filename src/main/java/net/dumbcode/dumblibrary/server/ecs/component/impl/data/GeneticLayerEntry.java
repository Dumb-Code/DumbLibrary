package net.dumbcode.dumblibrary.server.ecs.component.impl.data;

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.client.TextureUtils;
import net.dumbcode.dumblibrary.server.dna.data.ColouredGeneticDataHandler;
import net.dumbcode.dumblibrary.server.dna.data.GeneticTint;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLocationComponent;
import net.dumbcode.dumblibrary.server.utils.GeneticUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraftforge.common.util.Constants;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.*;

@Data
@AllArgsConstructor(access = AccessLevel.NONE)
@Accessors(chain = true)
public class GeneticLayerEntry implements Cloneable {
    private final String layerName;
    private final float layerWeight;
    private final float index;
    private final boolean checkIfExists;
    private final boolean variesOpacity;
    private final boolean primary;

    private final float colourMutate;
    private final float alphaMutate;

    private ResourceLocation textureLocationCache;

    private float[] colorCache = null;
    private final Map<UUID, GeneticTint.Part> baseTints = new HashMap<>();
    private final Map<UUID, GeneticTint.Part> targetTints = new HashMap<>();
    private Vector3f averageColor = null;
    private boolean doesTextureExist;

    public GeneticLayerEntry(String layerName, float layerWeight, float index, boolean checkIfExists, boolean variesOpacity, boolean primary, float colourMutate, float alphaMutate) {
        this.layerName = layerName;
        this.layerWeight = layerWeight;
        this.index = index;
        this.checkIfExists = checkIfExists;
        this.variesOpacity = variesOpacity;
        this.primary = primary;

        this.colourMutate = colourMutate;
        this.alphaMutate = alphaMutate;
    }

    public GeneticLayerEntry cloneForEntity() {
        return new GeneticLayerEntry(this.layerName, this.layerWeight, this.index, this.checkIfExists, this.variesOpacity, this.primary, this.colourMutate, this.alphaMutate);
    }

    public Optional<ResourceLocation> getTextureLocation(RenderLocationComponent.ConfigurableLocation baseLocation) {
        ResourceLocation location = baseLocation.copy().addFileName(this.layerName, Integer.MAX_VALUE).getLocation();
        if (!location.equals(this.textureLocationCache)) {
            this.textureLocationCache = location;
            if (this.checkIfExists) {
                this.doesTextureExist = Minecraft.getInstance().getResourceManager().hasResource(this.textureLocationCache);
            }
            if (!this.checkIfExists || this.doesTextureExist) {
                this.tryGetAverageColor();
            }
        }
        if (this.checkIfExists && !this.doesTextureExist) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.textureLocationCache);
    }

    public static GeneticLayerEntryBuilder builder(String layerName, float index) {
        return new GeneticLayerEntryBuilder(layerName, index);
    }

    private void tryGetAverageColor() {
        TextureUtils.attemptAverageColor(this.textureLocationCache, vector3d -> this.setAverageColor((float) vector3d.x, (float) vector3d.y, (float) vector3d.z));
    }

    public float[] getColours() {
        if (this.colorCache != null) {
            return this.colorCache;
        }
        List<GeneticTint.Part> tints = new ArrayList<>(this.baseTints.values());
        if (this.averageColor != null) {
            for (GeneticTint.Part value : this.targetTints.values()) {
                //TODO: verify if this is correct
                tints.add(new GeneticTint.Part(
                    value.getR() / this.averageColor.x(),
                    value.getG() / this.averageColor.y(),
                    value.getB() / this.averageColor.z(),
                    value.getA(),
                    value.getImportance()
                ));
            }
        }
        if (tints.isEmpty()) {
            return this.colorCache = new float[]{1, 1, 1, 1};
        }
        int size = 0;
        Vector4f result = new Vector4f();
        for (GeneticTint.Part tint : tints) {
            result.setX(result.x() + tint.getR() * tint.getImportance());
            result.setY(result.y() + tint.getG() * tint.getImportance());
            result.setZ(result.z() + tint.getB() * tint.getImportance());
            result.setW(result.w() + tint.getA() * tint.getImportance());
            size += tint.getImportance();
        }

        //If less than DEFAULT_COLOUR_IMPORTANCE, fill the rest with white.
        if(size < GeneticUtils.DEFAULT_COLOUR_IMPORTANCE) {
            int amount = GeneticUtils.DEFAULT_COLOUR_IMPORTANCE - size;
            result.setX(result.x() + amount);
            result.setY(result.y() + amount);
            result.setZ(result.z() + amount);
            result.setW(result.w() + amount);
            size = GeneticUtils.DEFAULT_COLOUR_IMPORTANCE;
        }

        int whiteAmount = (int) (this.layerWeight * size);
        result.setX(result.x() + whiteAmount);
        result.setY(result.y() + whiteAmount);
        result.setZ(result.z() + whiteAmount);
        result.setW(result.w() + whiteAmount);
        size += whiteAmount;

        return this.colorCache = new float[]{result.x() / size, result.y() / size, result.z() / size, this.variesOpacity ? result.w() / size : 1F};
    }

    public void setAverageColor(float r, float g, float b) {
        this.averageColor = new Vector3f(r, g, b);
        this.colorCache = null;
    }

    public void addDirectTint(UUID uuid, GeneticTint.Part part) {
        this.baseTints.put(uuid, part);
        this.colorCache = null;
    }

    public void removeDirectTint(UUID uuid) {
        this.baseTints.remove(uuid);
        this.colorCache = null;
    }

    public void addTargetTint(UUID uuid, GeneticTint.Part part) {
        this.targetTints.put(uuid, part);
        this.colorCache = null;
    }

    public void removeTargetTint(UUID uuid) {
        this.targetTints.remove(uuid);
        this.colorCache = null;
    }


    public static void serialize(GeneticLayerEntry entry, PacketBuffer buf) {
        buf.writeUtf(entry.layerName);
        buf.writeFloat(entry.layerWeight);
        buf.writeFloat(entry.index);
        buf.writeBoolean(entry.checkIfExists);
        buf.writeBoolean(entry.variesOpacity);
        buf.writeBoolean(entry.primary);

        buf.writeFloat(entry.colourMutate);
        buf.writeFloat(entry.alphaMutate);

        writeMap(entry.baseTints, buf);
        writeMap(entry.targetTints, buf);
    }

    private static void writeMap(Map<UUID, GeneticTint.Part> map, PacketBuffer buf) {
        buf.writeByte(map.size());
        map.forEach((uuid, col) -> {
            buf.writeUUID(uuid);
            ColouredGeneticDataHandler.writePartBuffer(col, buf);
        });
    }

    public static CompoundNBT serialize(GeneticLayerEntry entry, CompoundNBT tag) {
        tag.putString("layer", entry.layerName);
        tag.putFloat("layer_weight", entry.layerWeight);
        tag.putFloat("index", entry.index);
        tag.putBoolean("primary", entry.primary);
        tag.putBoolean("check_existing", entry.checkIfExists);
        tag.putBoolean("varies_opacity", entry.variesOpacity);

        tag.putFloat("colour_mutate", entry.colourMutate);
        tag.putFloat("alpha_mutate", entry.alphaMutate);

        tag.put("BaseTints", writeMap(entry.baseTints));
        tag.put("TargetTints", writeMap(entry.targetTints));


        return tag;
    }

    private static ListNBT writeMap(Map<UUID, GeneticTint.Part> map) {
        ListNBT list = new ListNBT();
        map.forEach((uuid, col) -> {
            CompoundNBT nbt = ColouredGeneticDataHandler.writePartNBT(col);
            nbt.putUUID("UUID", uuid);
            list.add(nbt);
        });
        return list;
    }

    public static JsonObject serialize(GeneticLayerEntry entry, JsonObject json) {
        json.addProperty("layer", entry.layerName);
        json.addProperty("layer_weight", entry.layerWeight);
        json.addProperty("index", entry.index);
        json.addProperty("primary", entry.primary);
        json.addProperty("check_existing", entry.checkIfExists);
        json.addProperty("varies_opacity", entry.variesOpacity);

        json.addProperty("colour_mutate", entry.colourMutate);
        json.addProperty("alpha_mutate", entry.alphaMutate);

        return json;
    }

    public static GeneticLayerEntry deserailize(CompoundNBT nbt) {
        GeneticLayerEntry entry = new GeneticLayerEntry(
            nbt.getString("layer"),
            nbt.getFloat("layer_weight"),
            nbt.getFloat("index"),
            nbt.getBoolean("check_existing"),
            nbt.getBoolean("varies_opacity"),
            nbt.getBoolean("primary"),

            nbt.getFloat("colour_mutate"),
            nbt.getFloat("alpha_mutate")
        );

        readMap(nbt.getList("BaseTints", Constants.NBT.TAG_STRING), entry::addDirectTint);
        readMap(nbt.getList("TargetTints", Constants.NBT.TAG_STRING), entry::addTargetTint);

        return entry;
    }

    private static void readMap(ListNBT list, BiConsumer<UUID, GeneticTint.Part> consumer) {
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT tag = list.getCompound(i);
            consumer.accept(
                tag.getUUID("uuid"),
                ColouredGeneticDataHandler.readPartNBT(tag)
            );
        }
    }

    public static GeneticLayerEntry deserailize(JsonObject json) {
        return new GeneticLayerEntry(
            JSONUtils.getAsString(json, "layer"),
            JSONUtils.getAsFloat(json, "layer_weight"),
            JSONUtils.getAsFloat(json, "index"),
            JSONUtils.getAsBoolean(json, "check_existing"),
            JSONUtils.getAsBoolean(json, "varies_opacity"),
            JSONUtils.getAsBoolean(json, "primary"),

            JSONUtils.getAsFloat(json, "colour_mutate"),
            JSONUtils.getAsFloat(json, "alpha_mutate")
        );
    }

    public static GeneticLayerEntry deserailize(PacketBuffer buf) {
        GeneticLayerEntry entry = new GeneticLayerEntry(
            buf.readUtf(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readBoolean(),
            buf.readBoolean(),
            buf.readBoolean(),

            buf.readFloat(),
            buf.readFloat()
        );

        readMap(buf, entry::addDirectTint);
        readMap(buf, entry::addTargetTint);

        return entry;
    }

    private static void readMap(PacketBuffer buf, BiConsumer<UUID, GeneticTint.Part> consumer) {
        byte size = buf.readByte();
        for (byte i = 0; i < size; i++) {
            consumer.accept(
                buf.readUUID(),
                ColouredGeneticDataHandler.readPartBuffer(buf)
            );
        }
    }

    public static class GeneticLayerEntryBuilder {
        private final String layerName;
        private final float index;
        private boolean checkIfExists = false;
        private boolean variesOpacity = false;
        private boolean primary = true;
        private float colorMutate = 0.7F;
        private float alphaMutate = 0.5F;
        private float layerWeight = 0.5F; //The higher this is, the less of the tint that is effected.

        GeneticLayerEntryBuilder(String layerName, float index) {
            this.layerName = layerName;
            this.index = index;
        }

        public GeneticLayerEntryBuilder checkIfExists() {
            return this.checkIfExists(true);
        }

        public GeneticLayerEntryBuilder checkIfExists(boolean checkIfExists) {
            this.checkIfExists = checkIfExists;
            return this;
        }

        public GeneticLayerEntryBuilder variesOpacity() {
            return this.variesOpacity(true);
        }

        public GeneticLayerEntryBuilder variesOpacity(boolean variesOpacity) {
            this.variesOpacity = variesOpacity;
            return this;
        }

        public GeneticLayerEntryBuilder weightAndColourMutate(float value) {
            this.colorMutateMin(value);
            this.layerWeight(value);
            return this;
        }

        public GeneticLayerEntryBuilder colorMutateMin(float defaultColorMutate) {
            this.colorMutate = defaultColorMutate;
            return this;
        }

        public GeneticLayerEntryBuilder alphaMutateMin(float defaultAlphaMin) {
            this.alphaMutate = defaultAlphaMin;
            return this;
        }

        public GeneticLayerEntryBuilder layerWeight(float layerWeight) {
            this.layerWeight = layerWeight;
            return this;
        }

        public GeneticLayerEntryBuilder secondaryColours() {
            this.primary = false;
            return this;
        }


        public GeneticLayerEntry build() {
            return new GeneticLayerEntry(layerName, layerWeight, index, checkIfExists, variesOpacity, primary, colorMutate, alphaMutate);
        }

        @Override
        public String toString() {
            return "GeneticLayerEntry.GeneticLayerEntryBuilder(" +
                "layerName=" + this.layerName +
                ", index=" + this.index +
                ", checkIfExists=" + this.checkIfExists +
                ", variesOpacity=" + this.variesOpacity +
                ", colorMutate=" + this.colorMutate +
                ", alphaMutate=" + this.alphaMutate +
                ")";
        }
    }
}
