package net.dumbcode.dumblibrary.client.animation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.*;
import lombok.*;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.server.animation.TabulaUtils;
import net.dumbcode.dumblibrary.server.animation.objects.*;
import net.dumbcode.dumblibrary.server.info.AnimationSystemInfo;
import net.dumbcode.dumblibrary.server.info.AnimationSystemInfoRegistry;
import net.dumbcode.dumblibrary.server.tabula.TabulaModelInformation;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.function.Function;

/**
 * The model container. Used to hold information about the tabula model and the poses. 
 */
@Getter
public class ModelContainer<E extends Entity> {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(PoseData.class, PoseData.Deserializer.INSTANCE)
            .create();

    private final AnimationSystemInfo<E> info;


    @SideOnly(Side.CLIENT)
    private final TabulaModel mainModel;

    /**
     * Map of {@code <Model Name, <Cube Name, Cube Reference>>}
     */
    @SideOnly(Side.CLIENT)
    private Map<String, Map<String, CubeReference>> references;
    /**
     * A map of the list of model datas to use in per animation
     */
    private Map<Animation, List<PoseData>> animations;



    public ModelContainer(ResourceLocation regname, AnimationSystemInfo<E> info) {
        AnimationSystemInfoRegistry.NAMESPACE.put(info.identifier(), info);
        this.info = info;
        //The base location of all the models
        String baseLoc = "models/entities/" + regname.getPath().replace("_", "/") + "/";
        String baseFileName = baseLoc + regname.getPath();
        Animation defaultAnimation = info.defaultAnimation();


        Map<String, Map<String, CubeReference>> references;
        Map<Animation, List<PoseData>> animations;
        try {
            Map<String, List<PoseData>> rawPoseData;
            try {
                //Get the input stream of the json file
                @Cleanup InputStream jsonStream = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(regname.getNamespace(), baseFileName + ".json")).getInputStream();
                //Create a reader of the input stream
                @Cleanup Reader reader = new InputStreamReader(jsonStream);
                //Parse the reader into a JsonObject, so i can deserialize it
                JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
                //Get the Poses object within the json file
                JsonObject poses = JsonUtils.getJsonObject(json, "poses");
                Map<String, List<PoseData>> map = Maps.newHashMap();
                //Iterate through all the supplied animation names
                for (String animation : info.allAnimationNames()) {
                    //If the poses object has a object called the "animation". Can be fully upper case or fully lowercase
                    for (String ani : Lists.newArrayList(animation.toLowerCase(), animation.toUpperCase())) {
                        if (JsonUtils.hasField(poses, ani)) {
                            //Get the Json Array thats referenced by the "animation"
                            for (JsonElement pose : JsonUtils.getJsonArray(poses, ani)) {
                                //For the every object within the Json Array, deserialize it to a PoseData, and add it to the list
                                map.computeIfAbsent(animation, a -> Lists.newArrayList()).add(GSON.fromJson(pose, PoseData.class));
                            }
                            break;
                        }
                    }
                }
                rawPoseData = map;
            } catch (IOException e) {
                throw new IllegalArgumentException("Could not main json load input stream for " + regname, e);
            }
            List<PoseData> modelResources = Lists.newArrayList();
            //Iterate through all the lists poses in the rawData
            for (List<PoseData> poses : rawPoseData.values()) {
                //Iterate through each individual pose in that list
                for (PoseData pose : poses) {
                    //Load the model location, and add it to the list
                    modelResources.add(pose.setLocation(new ModelLocation(pose.getModelName(), baseLoc + pose.getModelName())));
                }
            }

            Map<Animation, List<PoseData>> animationMap = Maps.newHashMap();
            //Iterate through the list of poses defined in the json file
            for (Map.Entry<String, List<PoseData>> entry : rawPoseData.entrySet()) {
                //Use the animationGetter to get the Animation from the string
                Animation animation = info.getAnimation(entry.getKey());
                List<PoseData> poseData = Lists.newArrayList(entry.getValue());

                info.setPoseData(animation, poseData);
                animationMap.put(animation, poseData);
            }
            //Get the main model for this ModelStage
            String location = Objects.requireNonNull(defaultAnimation.getIdentifier(), "Could not find main model location for " + regname + " as it was not defined");
            //load the client information
            references = loadAnimationInformation(new ResourceLocation(regname.getNamespace(), baseFileName + "_" + location), modelResources);
            animations = animationMap;
        } catch (Exception e) {
            DumbLibrary.getLogger().error("Unable to load poses for " + regname, e);
            references = Maps.newHashMap();
            animations = Maps.newHashMap();
        }
        this.references = references;
        this.animations = animations;


        //Load the main models

        TabulaModel model;
        //Make sure the model name isnt null
        if(defaultAnimation == null) {
            DumbLibrary.getLogger().error("Unable to load model for regname {} as main animation was not defined", regname);
            //If the name is null, set the model to null and continue
            model = null;
        } else {
            //Get the resource location of where the model is,
            ResourceLocation modelName = new ResourceLocation(regname.getNamespace(), baseFileName + "_" + defaultAnimation.getIdentifier());
            try {
                //Try and load the model, and also try to load the EntityAnimator (factory.createAnimator)
                model = TabulaUtils.getModel(modelName, info.createAnimator(this));
            } catch (Exception e) {
                //If for whatever reason theres an error while loading the tabula model, log the error and set the model to null
                DumbLibrary.getLogger().error("Unable to load model: " + modelName.toString(), e);
                model = null;
            }
        }
        this.mainModel = model;

    }


    private Map<String, Map<String, CubeReference>> loadAnimationInformation(ResourceLocation mainModelLocation, Iterable<PoseData> modelResources) {
        //Load the main model, used for comparing the diffrence in cube location/rotation
        TabulaModelInformation mainModel = TabulaUtils.getModelInformation(mainModelLocation);
        Map<String, Map<String, CubeReference>> map = Maps.newHashMap(); //Map of <Model location, <Cube Name, Cube Reference>>
        //Iterate through all the ModelLocations
        for (PoseData data : modelResources) {
            data.getCubes().clear();
            //Get the model location. If its in the map then just initialize the pose data, otherwise generate the cubes
            ModelLocation modelResource = data.getLocation();
            if (map.containsKey(modelResource.getFileName())) {
                data.getCubes().putAll(map.get(modelResource.getFileName()));
                continue;
            }
            Map<String, CubeReference> innerMap = Maps.newHashMap();
            map.put(modelResource.getFileName(), innerMap);
            //Get the location of the model that represents the pose, and that we're going to generate the data for
            ResourceLocation location = new ResourceLocation(mainModelLocation.getNamespace(), modelResource.getFullLocation());
            //If the pose location is the same as the mainModel, skip it and add all the mainModel data instead. Prevents loading the same model twice
            if (location.equals(mainModelLocation)) {
                for (TabulaModelInformation.Cube cube : mainModel.getAllCubes()) {
                    innerMap.put(cube.getName(), CubeReference.fromCube(cube));
                }
            } else {
                //If the file ends with .tbl (The old way). Currently only the working way which is why its enforced. I need to check the integrity of the python script
                if (modelResource.getFileName().endsWith(".tbl") || true) {
                    TabulaModelInformation model;
                    try {
                        //Try and get the model at the specified location
                        model = TabulaUtils.getModelInformation(location);
                    } catch (Exception e) {
                        DumbLibrary.getLogger().error("Unable to load tabula model " + location, e);
                        continue;
                    }
                    //Iterate through all the cube names in the main model
                    for (String cubeName : mainModel.getAllCubeNames()) {
                        //Get the cube of which the name links to
                        TabulaModelInformation.Cube cube = model.getCube(cubeName);
                        //If the cube does not exist in pose model (which shouldn't happen), then just default to the main models cube
                        if (cube == null) {
                            cube = Objects.requireNonNull(mainModel.getCube(cubeName));
                        }
                        //Create a CubeReference (data about the cubes position/rotation) and put it in the innerMap, with the key being the cube name
                        innerMap.put(cubeName, CubeReference.fromCube(cube));
                    }
                } else {
                    try {
                        //Create a new JsonParser
                        JsonParser parser = new JsonParser();
                        //If the location dosen't end with .json, then add it
                        if (!location.getPath().endsWith(".json")) {
                            location = new ResourceLocation(location.getNamespace(), location.getPath() + ".json");
                        }
                        //Get the json inputstream
                        @Cleanup InputStream stream = StreamUtils.openStream(location);
                        //Create a Reader for the inputstream
                        @Cleanup InputStreamReader reader = new InputStreamReader(stream);
                        //Read the inputstream as a json object
                        JsonObject json = parser.parse(reader).getAsJsonObject();
                        //Get the version of the json file
                        int version = JsonUtils.getInt(json, "version");
                        //Get a list of all the main model cubes. Used for determining which cubes are not overriden
                        List<String> cubeNames = Lists.newArrayList(mainModel.getAllCubeNames());
                        //Go through the list of overrides in the json.
                        for (JsonElement jsonElement : JsonUtils.getJsonArray(json, "overrides")) {
                            //Get the element as a json object
                            JsonObject obj = jsonElement.getAsJsonObject();
                            //Get the field inside the json object called "cube_name"
                            String cubeName = JsonUtils.getString(obj, "cube_name");
                            //get the cube with the same name that's in the mainModel
                            TabulaModelInformation.Cube mainCube = mainModel.getCube(cubeName);
                            //If the cube is already processed, or it dosen't continue on the main model, continue
                            if (!cubeNames.contains(cubeName) || mainCube == null) {
                                continue;
                            } else {
                                cubeNames.remove(cubeName);
                            }
                            //switch the version defined in the json
                            switch (version) {
                                case 0:
                                    //place the new CubeReference in the innerMap. Values are dematerialized from the json object

                                    float[] rotation = mainCube.getRotation();
                                    float[] rotationPoint = mainCube.getRotationPoint();

                                    innerMap.put(cubeName, new CubeReference(
                                            JsonUtils.hasField(obj, "rotation_x") ? JsonUtils.getFloat(obj, "rotation_x") : rotation[0],
                                            JsonUtils.hasField(obj, "rotation_y") ? JsonUtils.getFloat(obj, "rotation_y") : rotation[1],
                                            JsonUtils.hasField(obj, "rotation_z") ? JsonUtils.getFloat(obj, "rotation_z") : rotation[2],
                                            JsonUtils.hasField(obj, "position_x") ? JsonUtils.getFloat(obj, "position_x") : rotationPoint[0],
                                            JsonUtils.hasField(obj, "position_y") ? JsonUtils.getFloat(obj, "position_y") : rotationPoint[1],
                                            JsonUtils.hasField(obj, "position_z") ? JsonUtils.getFloat(obj, "position_z") : rotationPoint[2]
                                    ));
                                    break;

                                default: //We dont know how to handle versions other than 0
                                    throw new IllegalArgumentException("Dont know how to handle version " + version);
                            }
                        }
                        //Go through all the unedited cubes, and add a cube reference from the main model
                        for (String cubeName : cubeNames) {
                            innerMap.put(cubeName, CubeReference.fromCube(mainModel.getCube(cubeName)));

                        }
                    } catch (IOException e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            }
            data.getCubes().putAll(innerMap);
        }
        return map;
    }

    /**
     * Creates the animation wrapper.
     *
     * @param entity              the entity you're creating the pass for
     * @param model               the model the animation pass wrapper would be applied to
     * @param inertia             should inertia be used
     * @param factories           a list of {@link AnimationLayerFactory} (Note these should be Object::new)
     * @return A new animation wrapper.
     */
    @SideOnly(Side.CLIENT)
    public AnimationRunWrapper<E> createAnimationWrapper(E entity, TabulaModel model, boolean inertia,
                                                         List<AnimationLayerFactory<E>> factories) {
        List<AnimationLayer<E>> list = Lists.newArrayList();
        for (val factory : factories) {
            Map<String, AnimationRunWrapper.CubeWrapper> cacheMap = new HashMap<>();
            list.add(factory.createWrapper(entity, model.getAllCubesNames(), model::getCube, s -> cacheMap.computeIfAbsent(s, o -> new AnimationRunWrapper.CubeWrapper(model.getCube(o))), this.info, inertia));
        }
        return new AnimationRunWrapper<>(entity, list);
    }


    /**
     * Information class to hold the file name, and the file location of the pose
     */
    @Getter
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class ModelLocation {
        private String fileName;
        private String fullLocation;
    }


    /**
     * The factory for creating {@link AnimationLayer}
     */
    public interface AnimationLayerFactory<E extends Entity> {
        AnimationLayer<E> createWrapper(E entity, Collection<String> cubeNames, Function<String, AnimationLayer.AnimatableCube> anicubeRef, Function<String, AnimationRunWrapper.CubeWrapper> cuberef, AnimationSystemInfo<E> info, boolean inertia);
    }
}
