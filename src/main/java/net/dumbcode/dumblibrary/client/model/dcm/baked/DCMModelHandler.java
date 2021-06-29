package net.dumbcode.dumblibrary.client.model.dcm.baked;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.Value;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.utils.MissingModelInfo;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.dumbcode.studio.model.ModelInfo;
import net.dumbcode.studio.model.ModelLoader;
import net.dumbcode.studio.model.ModelMirror;
import net.dumbcode.studio.model.RotationOrder;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelLoader;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The model handler for loading .dcm models
 */
public enum DCMModelHandler implements IModelLoader<DCMModelGeometry> {
    INSTANCE;

    /**
     * A dummy texture layer representing the missing texture/layer
     */
    public static final TextureLayer MISSING = new TextureLayer("missing", "missingno", s -> true, -1);

    @Override
    @SneakyThrows
    public DCMModelGeometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        JsonObject json = modelContents.getAsJsonObject();

        ResourceLocation location = new ResourceLocation(JSONUtils.getAsString(json, "model"));
        ModelInfo information;
        if (!location.getPath().endsWith(".dcm")) {
            location = new ResourceLocation(location.getNamespace(), location.getPath() + ".dcm");
        }
        try {
            information = StreamUtils.openStream(location, stream -> ModelLoader.loadModel(stream, RotationOrder.global, ModelMirror.NONE));
        } catch (IOException e) {
            DumbLibrary.getLogger().error("Unable to load model " + location, e);
            information = MissingModelInfo.MISSING;
        }

        List<TextureLayer> allTextures = readLayerData(json);
        List<LightupData> lightupData = Lists.newArrayList();

        if (json.has("lightup_data")) {
            for (JsonElement data : JSONUtils.getAsJsonArray(json, "lightup_data")) {
                lightupData.add(LightupData.parse(data.getAsJsonObject()));
            }
        }

        Map<String, Pair<List<CubeFacingValues>, Integer>> directTints = new HashMap<>();
        if(json.has("direct_cube_tint")) {
            for (JsonElement element : JSONUtils.getAsJsonArray(json, "direct_cube_tint")) {
                JsonObject cubeTint = element.getAsJsonObject();
                String layer = JSONUtils.getAsString(cubeTint, "layer");
                directTints.put(layer, Pair.of(parseCubeFacingValues(JSONUtils.getAsJsonArray(cubeTint, "cubes")), JSONUtils.getAsInt(cubeTint, "tint")));
            }
        }

        Map<String, String> renderLayerDataMap = new HashMap<>();
        JsonObject renderLayers = JSONUtils.getAsJsonObject(json, "render_layers", new JsonObject());
        for (Map.Entry<String, JsonElement> entry : renderLayers.entrySet()) {
            if(renderLayers.has(entry.getKey())) {
                for (JsonElement element : entry.getValue().getAsJsonArray()) {
                    renderLayerDataMap.put(element.getAsString(), entry.getKey());
                }
            }
        }

        return new DCMModelGeometry(
            Collections.unmodifiableList(allTextures), lightupData, directTints, renderLayerDataMap,
//            PerspectiveMapWrapper.getTransforms(modelBlock.getTransforms()),
            information, JSONUtils.getAsBoolean(json, "ambientocclusion", true));
    }

    private static List<TextureLayer> readLayerData(JsonObject object) {
        ResourceLocation resourcelocation = AtlasTexture.LOCATION_BLOCKS;
        List<TextureLayer> textureLayers = new ArrayList<>();
        if (object.has("textures")) {
            JsonObject jsonobject = JSONUtils.getAsJsonObject(object, "textures");
            int index = 0;
            for(Map.Entry<String, JsonElement> entry : jsonobject.entrySet()) {
                String name = entry.getKey();
                String value = entry.getValue().getAsString();

                Predicate<String> predicate = Predicates.alwaysTrue();
                if(object.has("texture_data") && object.getAsJsonObject("texture_data").has(name)) {
                    predicate = readLayerPredicate(object.getAsJsonObject("texture_data").getAsJsonObject(name));
                }
                textureLayers.add(new TextureLayer(name, value, predicate, index++));
            }
        }
        return textureLayers;
    }

    private static Predicate<String> readLayerPredicate(JsonObject layerObject) {
        String type = JSONUtils.getAsString(layerObject, "type");
        List<String> cubeNames = StreamUtils.stream(JSONUtils.getAsJsonArray(layerObject, "cubes")).map(JsonElement::getAsString).collect(Collectors.toList());
        if(!type.equals("whitelist") && !type.equals("blacklist")) {
            throw new IllegalArgumentException("Don't know how to handle texture info type " + type);
        }
        Predicate<String> cubePredicate = cubeNames::contains;
        if(type.equals("blacklist")) {
            cubePredicate = cubePredicate.negate();
        }
        return cubePredicate;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {

    }

    @Data
    public static class TextureLayer {
        private final String layerName;
        private final String value;
        private final Predicate<String> cubePredicate;
        private final int index;
        private TextureAtlasSprite sprite;
        private RenderMaterial material;
    }

    /**
     * Lightup data is used to finalizeComponent fake light to the quads. Each entry goes in an array called "lightup_data". The following is an example
     *
     * <pre>{@code
     *     {
     *         "layers_applied": [          <--- These are the layers that this full-bright should finalizeComponent to. These are defined before in your "textures" section of the json file
     *             "layer0",
     *             "layer1",
     *         ],
     *         "cubes_lit": [               <--- A list of the cubes lit. Each one of these entries, no matter the type, is a {@link CubeFacingValues }
     *             "headPiece1",            <--- Having them as strings will mean the whole cube is lit
     *             "headPiece2",
     *             {                        <--- If you don't want the whole cube lit up, you can define the faces for the full-bright to act on
     *                 "cube_name": "some_cube",
     *                 "faces": [           <--- The faces that will be light up with full-bright. Note that if the cube is rotated say 90* on the x axis, then UP will not be UP
     *                     "north",
     *                     "south",
     *                     "west"
     *                 ]
     *             }
     *         ],
     *         "sky_light": 12,              <--- The minimum block-light level that this quad will be set to. If left blank will be defaulted to 15
     *         "block_light": 3             <--- The minimum skylight level that the quad will be set to. If left blank will be defaulted to 15
     *     }
     * }
     * </pre>
     * Note that this is just one entry into a json array. You can have multiple entries for different light levels or different cubes on different layers.
     */
    @Value
    public static class LightupData {
        @Nullable
        private Set<String> layersApplied;
        private List<CubeFacingValues> entry;
        private List<SmoothFace> smoothFace;
        private float blockLight;
        private float skyLight;

        public static LightupData parse(JsonObject json) {
            Set<String> layersApplied = Sets.newHashSet();
            if (json.has("layers_applied")) {
                JsonArray arr = JSONUtils.getAsJsonArray(json, "layers_applied");
                for (int i = 0; i < arr.size(); i++) {
                    layersApplied.add(arr.get(i).getAsString());
                }
            } else {
                layersApplied = null;
            }

            List<SmoothFace> smoothFaces = new ArrayList<>();
            if(json.has("smooth_cubes")) {
                for (JsonElement element : JSONUtils.getAsJsonArray(json, "smooth_cubes")) {
                    JsonObject jsonSFO = element.getAsJsonObject();
                    if(JSONUtils.isStringValue(jsonSFO, "origin")) {
                        String face = JSONUtils.getAsString(jsonSFO, "origin");
                        for (Direction value : Direction.values()) {
                            if(value.getName().equals(face)) {
                                smoothFaces.add(new SmoothFace(JSONUtils.getAsString(jsonSFO, "cube_name"), value, JSONUtils.getAsInt(jsonSFO, "size_block"), JSONUtils.getAsInt(jsonSFO, "size_sky")));
                                break;
                            }
                        }
                    }
                }
            }

            float blockLight = JSONUtils.getAsFloat(json, "block_light", 15F);
            float skyLight = JSONUtils.getAsFloat(json, "sky_light", 15F);

            return new LightupData(layersApplied, parseCubeFacingValues(JSONUtils.getAsJsonArray(json, "cubes_lit", new JsonArray())), smoothFaces, blockLight, skyLight);
        }
    }

    @Value public static class SmoothFace { String cube; Direction smoothFaceOrigin; int blockAmount, skyAmount; }

    public static List<CubeFacingValues> parseCubeFacingValues(JsonArray array) {
        List<CubeFacingValues> list = Lists.newArrayList();
        for (JsonElement cube : array) {
            if (cube.isJsonPrimitive() && ((JsonPrimitive) cube).isString()) {
                list.add(new CubeFacingValues(cube.getAsString(), Sets.newHashSet(Direction.values())));
            } else if (cube.isJsonObject()) {
                JsonObject cubeJson = cube.getAsJsonObject();
                String name = JSONUtils.getAsString(cubeJson, "cube_name");
                JsonArray faces = JSONUtils.getAsJsonArray(cubeJson, "faces");
                String[] astr = new String[faces.size()];
                for (int i = 0; i < faces.size(); i++) {
                    astr[i] = faces.get(i).getAsString();
                }
                Set<Direction> facings = Sets.newHashSet();
                for (Direction value : Direction.values()) {
                    for (String face : astr) {
                        if (value.getName().equalsIgnoreCase(face)) {
                            facings.add(value);
                            break;
                        }
                    }
                }
                list.add(new CubeFacingValues(name, facings));
            } else {
                throw new JsonSyntaxException("Expected a String or a Json Object");
            }
        }
        return list;
    }

    @Value
    public static class CubeFacingValues {
        String cubeName;
        Set<Direction> facing;
    }
}
