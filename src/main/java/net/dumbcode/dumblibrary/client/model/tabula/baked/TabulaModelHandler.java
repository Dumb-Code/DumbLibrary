package net.dumbcode.dumblibrary.client.model.tabula.baked;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.*;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import lombok.Cleanup;
import lombok.Data;
import lombok.Value;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.dumbcode.studio.model.ModelInfo;
import net.dumbcode.studio.model.ModelLoader;
import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import org.apache.commons.io.IOUtils;

import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The model handler for loading .tbl models
 */
public enum TabulaModelHandler implements ISelectiveResourceReloadListener {
    INSTANCE;

    /**
     * A dummy texture layer representing the missing texture/layer
     */
    public static final TextureLayer MISSING = new TextureLayer("missing", new RenderMaterial(AtlasTexture.LOCATION_BLOCKS, new ResourceLocation("missingno")), Predicates.alwaysTrue(), -1);

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

//    @Override
//    public boolean accepts(ResourceLocation modelLocation) {
//        return this.namespaces.contains(modelLocation.getNamespace()) && modelLocation.getPath().endsWith(".tbl");
//    }

    public IUnbakedModel loadModel(ResourceLocation modelLocation) throws Exception {
        String path = modelLocation.getPath().replaceAll("\\.tbl", ".json");
        IResource resource = this.manager.getResource(new ResourceLocation(modelLocation.getNamespace(), path));
        @Cleanup InputStreamReader reader = new InputStreamReader(resource.getInputStream());
        String string = IOUtils.toString(reader);
        JsonObject json = PARSER.parse(string).getAsJsonObject();
        ModelInfo information = StreamUtils.openStream(new ResourceLocation(JSONUtils.getAsString(json, "tabula")), ModelLoader::loadModel);
        BlockModel modelBlock = BlockModel.fromStream(new StringReader(string));
        List<TextureLayer> allTextures = Lists.newArrayList();
        Set<String> layers = Sets.newHashSet();
        JsonObject object = JSONUtils.getAsJsonObject(json, "texture_data", new JsonObject());
        for (String key : modelBlock.textureMap.keySet()) {
            int layer = -1;
            Matcher matcher = PATTERN.matcher(key);
            if (matcher.matches()) {
                layer = Integer.parseInt(matcher.group(1));
            }
            Predicate<String> cubePredicate = Predicates.alwaysTrue();
            if(object.has("layer" + layer)) {
                JsonObject layerObject = JSONUtils.getAsJsonObject(object, "layer" + layer);
                String type = JSONUtils.getAsString(layerObject, "type");
                List<String> cubeNames = StreamUtils.stream(JSONUtils.getAsJsonArray(layerObject, "cubes")).map(JsonElement::getAsString).collect(Collectors.toList());
                if(!type.equals("whitelist") && !type.equals("blacklist")) {
                    throw new IllegalArgumentException("Don't know how to handle texture info type " + type);
                }
                cubePredicate = cubeNames::contains;
                if(type.equals("blacklist")) {
                    cubePredicate = cubePredicate.negate();
                }
            }
            allTextures.add(new TextureLayer(key, new RenderMaterial(AtlasTexture.LOCATION_BLOCKS, modelBlock.textureMap.get(key).map(RenderMaterial::texture, ResourceLocation::new)), cubePredicate, layer));
            layers.add(key);
        }
        Either<RenderMaterial, String> particle = modelBlock.textureMap.get("particle");
        ResourceLocation part = particle == null ? MissingTextureSprite.getLocation() : particle.map(RenderMaterial::texture, ResourceLocation::new);

        List<LightupData> lightupData = Lists.newArrayList();

        if (json.has("lightup_data")) {
            for (JsonElement data : JSONUtils.getAsJsonArray(json, "lightup_data")) {
                lightupData.add(LightupData.parse(data.getAsJsonObject(), layers));
            }
        }

        Map<Integer, Pair<List<CubeFacingValues>, Integer>> directTints = new HashMap<>();
        if(json.has("direct_cube_tint")) {
            for (JsonElement element : JSONUtils.getAsJsonArray(json, "direct_cube_tint")) {
                JsonObject cubeTint = element.getAsJsonObject();
                Matcher matcher = PATTERN.matcher(JSONUtils.getAsString(cubeTint, "layer"));
                if (matcher.matches()) {
                    directTints.put(Integer.parseInt(matcher.group(1)), Pair.of(parseCubeFacingValues(JSONUtils.getAsJsonArray(cubeTint, "cubes")), JSONUtils.getAsInt(cubeTint, "tint")));
                }
            }
        }

        Map<Integer, String> renderLayerDataMap = new HashMap<>();
        JsonObject renderLayers = JSONUtils.getAsJsonObject(json, "render_layers", new JsonObject());
        for (Map.Entry<String, JsonElement> entry : renderLayers.entrySet()) {
            if(renderLayers.has(entry.getKey())) {
                for (JsonElement element : entry.getValue().getAsJsonArray()) {
                    Matcher matcher = PATTERN.matcher(element.getAsString());
                    if (matcher.matches()) {
                        renderLayerDataMap.put(Integer.parseInt(matcher.group(1)), entry.getKey());
                    }
                }
            }
        }

        return new DCMIModel(
            Collections.unmodifiableList(allTextures), lightupData, directTints, renderLayerDataMap, part,
//            PerspectiveMapWrapper.getTransforms(modelBlock.getTransforms()),
            information, modelBlock.hasAmbientOcclusion, modelBlock.getTransforms());
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
        this.manager = resourceManager;
    }

    @Data
    public static class TextureLayer {
        private final String layerName;
        private final RenderMaterial material;
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
        private List<SmoothFace> smoothFace;
        private float blockLight;
        private float skyLight;

        public static LightupData parse(JsonObject json, Set<String> layers) {
            Set<String> layersApplied = Sets.newHashSet();
            if (json.has("layers_applied")) {
                JsonArray arr = JSONUtils.getAsJsonArray(json, "layers_applied");
                for (int i = 0; i < arr.size(); i++) {
                    layersApplied.add(arr.get(i).getAsString());
                }
            } else {
                layersApplied.addAll(layers);
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
