package net.dumbcode.dumblibrary.client.model.tabula.baked;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.*;
import lombok.Cleanup;
import lombok.Data;
import lombok.Getter;
import lombok.Value;
import net.dumbcode.dumblibrary.server.animation.TabulaUtils;
import net.dumbcode.dumblibrary.server.json.JsonUtil;
import net.dumbcode.dumblibrary.server.tabula.TabulaModelInformation;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The model handler for loading .tbl models
 */
public enum TabulaModelHandler implements ICustomModelLoader {
    INSTANCE;

    /**
     * A dummy texture layer representing the missing texture/layer
     */
    public static final TextureLayer MISSING = new TextureLayer("missing", new ResourceLocation("missingno"), Predicates.alwaysTrue(), -1);

    private static final JsonParser PARSER = new JsonParser();
    /**
     * regex pattern to get the layer id from the list of layer textures.
     */
    private static final Pattern PATTERN = Pattern.compile("layer(\\d+)$");
    private final Set<String> namespaces = Sets.newHashSet();
    private IResourceManager manager;


    public void allow(String namespace) {
        this.namespaces.add(namespace);
    }

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        return this.namespaces.contains(modelLocation.getNamespace()) && modelLocation.getPath().endsWith(".tbl");
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) throws Exception {
        String path = modelLocation.getPath().replaceAll("\\.tbl", ".json");
        IResource resource = this.manager.getResource(new ResourceLocation(modelLocation.getNamespace(), path));
        @Cleanup InputStreamReader reader = new InputStreamReader(resource.getInputStream());
        String string = IOUtils.toString(reader);
        JsonObject json = PARSER.parse(string).getAsJsonObject();
        TabulaModelInformation information = TabulaUtils.getModelInformation(new ResourceLocation(JsonUtils.getString(json, "tabula")));
        ModelBlock modelBlock = ModelBlock.deserialize(string);
        List<TextureLayer> allTextures = Lists.newArrayList();
        Set<String> layers = Sets.newHashSet();
        JsonObject object = JsonUtils.getJsonObject(json, "texture_data", new JsonObject());
        for (String key : modelBlock.textures.keySet()) {
            int layer = -1;
            Matcher matcher = PATTERN.matcher(key);
            if (matcher.matches()) {
                layer = Integer.parseInt(matcher.group(1));
            }
            Predicate<String> cubePredicate = Predicates.alwaysTrue();
            if(object.has("layer" + layer)) {
                JsonObject layerObject = JsonUtils.getJsonObject(object, "layer" + layer);
                String type = JsonUtils.getString(layerObject, "type");
                List<String> cubeNames = StreamUtils.stream(JsonUtils.getJsonArray(layerObject, "cubes")).map(JsonElement::getAsString).collect(Collectors.toList());
                if(!type.equals("whitelist") && !type.equals("blacklist")) {
                    throw new IllegalArgumentException("Don't know how to handle texture info type " + type);
                }
                cubePredicate = cubeNames::contains;
                if(type.equals("blacklist")) {
                    cubePredicate = cubePredicate.negate();
                }
            }
            allTextures.add(new TextureLayer(key, new ResourceLocation(modelBlock.textures.get(key)), cubePredicate, layer));
            layers.add(key);
        }
        String particle = modelBlock.textures.get("particle");
        ResourceLocation part = particle == null ? new ResourceLocation("missingno") : new ResourceLocation(particle);

        List<LightupData> lightupData = Lists.newArrayList();

        if (JsonUtils.isJsonArray(json, "lightup_data")) {
            for (JsonElement data : JsonUtils.getJsonArray(json, "lightup_data")) {
                lightupData.add(LightupData.parse(data.getAsJsonObject(), layers));
            }
        }

        Map<Integer, Pair<List<CubeFacingValues>, Integer>> directTints = new HashMap<>();
        if(JsonUtils.isJsonArray(json, "direct_cube_tint")) {
            for (JsonElement element : JsonUtils.getJsonArray(json, "direct_cube_tint")) {
                JsonObject cubeTint = element.getAsJsonObject();
                Matcher matcher = PATTERN.matcher(JsonUtils.getString(cubeTint, "layer"));
                if (matcher.matches()) {
                    directTints.put(Integer.parseInt(matcher.group(1)), Pair.of(parseCubeFacingValues(JsonUtils.getJsonArray(cubeTint, "cubes")), JsonUtils.getInt(cubeTint, "tint")));
                }
            }
        }

        Map<Integer, BlockRenderLayer> renderLayerDataMap = new HashMap<>();
        JsonObject renderLayers = JsonUtils.getJsonObject(json, "render_layers", new JsonObject());
        for (BlockRenderLayer value : BlockRenderLayer.values()) {
            String key = value.name().toLowerCase();
            if(renderLayers.has(key)) {
                JsonObject renderLayerObject = JsonUtils.getJsonObject(renderLayers, key);
                if(JsonUtils.isJsonArray(renderLayerObject, "layers")) {
                    for (JsonElement element : JsonUtils.getJsonArray(renderLayerObject, "layers")) {
                        Matcher matcher = PATTERN.matcher(element.getAsString());
                        if (matcher.matches()) {
                            renderLayerDataMap.put(Integer.parseInt(matcher.group(1)), value);
                        }
                    }
                }
            }
        }

        return new TabulaIModel(Collections.unmodifiableList(allTextures), lightupData, directTints, renderLayerDataMap, part, PerspectiveMapWrapper.getTransforms(modelBlock.getAllTransforms()), information, modelBlock.ambientOcclusion, modelBlock.isGui3d(), modelBlock.getOverrides());
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        this.manager = resourceManager;
    }

    @Data
    public static class TextureLayer {
        private final String layerName;
        private final ResourceLocation loc;
        private final Predicate<String> cubePredicate;
        private final int layer;
        private TextureAtlasSprite sprite;
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
        private Set<String> layersApplied;
        private List<CubeFacingValues> entry;
        private float blockLight;
        private float skyLight;

        public static LightupData parse(JsonObject json, Set<String> layers) {
            Set<String> layersApplied = Sets.newHashSet();
            if (JsonUtils.isJsonArray(json, "layers")) {
                JsonArray arr = JsonUtils.getJsonArray(json, "layers");
                for (int i = 0; i < arr.size(); i++) {
                    layersApplied.add(JsonUtils.getString(arr.get(i), "layers[" + i + "]"));
                }
            } else {
                layersApplied.addAll(layers);
            }

            float blockLight = JsonUtils.getFloat(json, "block_light", 15F);
            float skyLight = JsonUtils.getFloat(json, "sky_light", 15F);

            return new LightupData(layersApplied, parseCubeFacingValues(JsonUtils.getJsonArray(json, "cubes_lit")), blockLight, skyLight);
        }
    }

    public static List<CubeFacingValues> parseCubeFacingValues(JsonArray array) {
        List<CubeFacingValues> list = Lists.newArrayList();
        for (JsonElement cube : array) {
            if (cube.isJsonPrimitive() && ((JsonPrimitive) cube).isString()) {
                list.add(new CubeFacingValues(cube.getAsString(), Sets.newHashSet(EnumFacing.values())));
            } else if (cube.isJsonObject()) {
                JsonObject cubeJson = cube.getAsJsonObject();
                String name = JsonUtils.getString(cubeJson, "cube_name");
                JsonArray faces = JsonUtils.getJsonArray(cubeJson, "faces");
                String[] astr = new String[faces.size()];
                for (int i = 0; i < faces.size(); i++) {
                    astr[i] = JsonUtils.getString(faces.get(i), "faces[" + i + "]");
                }
                Set<EnumFacing> facings = Sets.newHashSet();
                for (EnumFacing value : EnumFacing.values()) {
                    for (String face : astr) {
                        if (value.getName2().equalsIgnoreCase(face)) {
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
        Set<EnumFacing> facing;
    }
}
