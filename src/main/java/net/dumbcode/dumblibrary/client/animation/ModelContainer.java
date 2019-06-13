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
import net.minecraft.entity.Entity;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.util.Strings;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private static final String JSON_EXTENSION = ".json";

    private final AnimationSystemInfo<E> info;


    @SideOnly(Side.CLIENT)
    private final TabulaModel mainModel;

    /**
     * Map of {@code <Model Name, <Cube Name, Cube Reference>>}
     */
    @SideOnly(Side.CLIENT)
    private final Map<String, Map<String, CubeReference>> references = Maps.newHashMap();
    /**
     * A map of the list of model datas to use in per animation
     */
    private final Map<Animation, List<PoseData>> animations = Maps.newHashMap();


    public ModelContainer(ResourceLocation regname, AnimationSystemInfo<E> info) {
        AnimationSystemInfoRegistry.INSTANCE.namespace.put(info.identifier(), info);
        this.info = info;
        //The base location of all the models
        String baseLoc = "models/entities/" + regname.getPath().replace("_", "/") + "/";
        String baseFileName = baseLoc + regname.getPath();
        Animation defaultAnimation = info.defaultAnimation();


        this.loadAnimations(this.references, this.animations, regname, baseLoc, baseFileName);


        //Load the main models

        TabulaModel model;
        //Make sure the model name isnt null
        if (defaultAnimation == null) {
            DumbLibrary.getLogger().error("Unable to loadRawAnimations model for regname {} as main animation was not defined", regname);
            //If the name is null, set the model to null and continue
            model = null;
        } else {
            //Get the resource location of where the model is,
            ResourceLocation modelName = new ResourceLocation(regname.getNamespace(), baseFileName + "_" + defaultAnimation.getIdentifier());
            try {
                //Try and loadRawAnimations the model, and also try to loadRawAnimations the EntityAnimator (factory.createAnimator)
                model = TabulaUtils.getModel(modelName, info.createAnimator(this));
            } catch (Exception e) {
                //If for whatever reason theres an error while loading the tabula model, log the error and set the model to null
                DumbLibrary.getLogger().error("Unable to loadRawAnimations model: " + modelName.toString(), e);
                model = null;
            }
        }
        this.mainModel = model;

    }

    /**
     * Iterate through all the all the raw animations and puts the pose data into the animation. Also loads the main model with all the animation data.
     *
     * @param references   The map of which the model name will be added with the list of cube names to cube references
     * @param animations   The map of which all the animations will be loaded to
     * @param regname      The registry name of where to load the data from
     * @param baseLoc      The base location. Derived from regname
     * @param baseFileName The base file name. Derived from regname
     * @see ModelContainer#references
     * @see ModelContainer#animations
     */
    private void loadAnimations(Map<String, Map<String, CubeReference>> references, Map<Animation, List<PoseData>> animations, ResourceLocation regname, String baseLoc, String baseFileName) {
        try {
            Map<String, List<PoseData>> rawAnimations = Maps.newHashMap();
            this.loadRawAnimations(regname, baseLoc, baseFileName, rawAnimations);

            List<PoseData> modelResources = Lists.newArrayList();
            //Iterate through all the lists poses in the rawData
            for (List<PoseData> poses : rawAnimations.values()) {
                //Iterate through each individual pose in that list
                for (PoseData pose : poses) {
                    //Load the model location, and add it to the list
                    modelResources.add(pose.setLocation(new ModelLocation(pose.getModelName(), baseLoc + pose.getModelName())));
                }
            }

            //Iterate through the list of poses defined in the json file
            for (Map.Entry<String, List<PoseData>> entry : rawAnimations.entrySet()) {
                //Use the animationGetter to get the Animation from the string
                Animation animation = this.info.getAnimation(entry.getKey());
                List<PoseData> poseData = Lists.newLinkedList(entry.getValue());

                this.info.setPoseData(animation, poseData);
                animations.put(animation, poseData);
            }
            //Get the main model for this ModelStage
            String location = Objects.requireNonNull(this.info.defaultAnimation().getIdentifier(), "Could not find main model location for " + regname + " as it was not defined");
            //load the animation info
            this.loadMainModelInformation(references, new ResourceLocation(regname.getNamespace(), baseFileName + "_" + location), modelResources);
        } catch (Exception e) {
            DumbLibrary.getLogger().error("Unable to loadRawAnimations poses for " + regname, e);
        }
    }

    /**
     * Loads the raw map of (animation, List(Posedata))
     *
     * @param regname      The registry name of where to load the animations
     * @param baseFolder The base folder, derived from regname
     * @param baseFileName The base file name, derived from the regname
     * @param map a map of animation-name to a list of the pose data for that animation
     */
    private void loadRawAnimations(ResourceLocation regname, String baseFolder, String baseFileName, Map<String, List<PoseData>> map) {
        try {
            //Get the input stream of the json file
            @Cleanup InputStream jsonStream = StreamUtils.openStream(new ResourceLocation(regname.getNamespace(), baseFileName + JSON_EXTENSION));
            //Create a reader of the input stream
            @Cleanup Reader reader = new InputStreamReader(jsonStream);
            //Parse the reader into a JsonObject, so i can deserialize it
            JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
            //Get the Poses object within the json file
            JsonObject poses = JsonUtils.getJsonObject(json, "poses");
            //Iterate through all the supplied animation names
            for (String animation : this.info.allAnimationNames()) {
                //If the poses object has a object called the "animation". Can be fully upper case or fully lowercase
                for (String ani : Lists.newArrayList(animation.toLowerCase(), animation.toUpperCase())) {
                    if (JsonUtils.hasField(poses, ani)) {
                        if(JsonUtils.isJsonArray(poses, ani)) {
                            //Get the Json Array thats referenced by the "animation"
                            for (JsonElement pose : JsonUtils.getJsonArray(poses, ani)) {
                                //For the every object within the Json Array, deserialize it to a PoseData, and add it to the list
                                map.computeIfAbsent(animation, a -> Lists.newLinkedList()).add(GSON.fromJson(pose, PoseData.class));
                            }
                        } else {
                            this.loadJsonPose(JsonUtils.getJsonObject(poses, ani), animation, regname, baseFolder, map);
                        }
                        break;
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not main json loadRawAnimations input stream for " + regname, e);
        }
    }

    private void loadJsonPose(JsonObject poseObject, String animation, ResourceLocation regName, String baseFolder, Map<String, List<PoseData>> map) {
        String directory = JsonUtils.getString(poseObject, "directory");
        float time = JsonUtils.getFloat(poseObject, "time");
        Path root = StreamUtils.getPath(new ResourceLocation(regName.getNamespace(), baseFolder + "/" + directory));
        try {
            map.put(animation,
            Files.walk(root)
                    .filter(path -> path != root)
                    .map(path -> path.getParent().getFileName() + "/" + FilenameUtils.getBaseName(path.getFileName().toString()))
                    .filter(Strings::isNotEmpty)
                    .sorted()
                    .map(modelname -> new PoseData(modelname, time))
                    .collect(Lists::newLinkedList, List::add, LinkedList::addAll)
        );
        } catch (IOException e) {
            DumbLibrary.getLogger().warn(e);
        }
    }


    /**
     * Loads the main model
     *
     * @param references        A map of which to add the model information too.
     * @param mainModelLocation The main models location
     * @param modelResources    the list of pose data for the main model
     * @see ModelContainer#references
     */
    private void loadMainModelInformation(Map<String, Map<String, CubeReference>> references, ResourceLocation mainModelLocation, Iterable<PoseData> modelResources) {
        //Load the main model, used for comparing the diffrence in cube location/rotation
        TabulaModelInformation mainInfo = TabulaUtils.getModelInformation(mainModelLocation);
        //Iterate through all the ModelLocations
        for (PoseData data : modelResources) {
            data.getCubes().clear();
            //Get the model location. If its in the map then just initialize the pose data, otherwise generate the cubes
            ModelLocation modelResource = data.getLocation();
            if (references.containsKey(modelResource.getFileName())) {
                data.getCubes().putAll(references.get(modelResource.getFileName()));
                continue;
            }
            Map<String, CubeReference> innerMap = Maps.newHashMap();
            references.put(modelResource.getFileName(), innerMap);
            //Get the location of the model that represents the pose, and that we're going to generate the data for
            ResourceLocation location = new ResourceLocation(mainModelLocation.getNamespace(), modelResource.getFullLocation());
            //If the pose location is the same as the mainInfo, skip it and add all the mainInfo data instead. Prevents loading the same model twice
            if (location.equals(mainModelLocation)) {
                for (TabulaModelInformation.Cube cube : mainInfo.getAllCubes()) {
                    innerMap.put(cube.getName(), CubeReference.fromCube(cube));
                }
            } else {
                //If the file ends with .tbl (The old way). Currently only the working way which is why its enforced. I need to check the integrity of the python script
                if (modelResource.getFileName().endsWith(JSON_EXTENSION)) {
                    this.loadJsonPose(location, mainInfo, innerMap);
                } else {
                    this.loadTabulaPose(location, mainInfo, innerMap);
                }
            }
            data.getCubes().putAll(innerMap);
        }
    }

    /**
     * Loads a pose from a tabula model
     *
     * @param location  The location of the tabula model
     * @param mainModel The main model of which to compare too and take cubes from when they don't exist in the given model
     * @param innerMap  The inner map of which to add the data too.
     */
    private void loadTabulaPose(ResourceLocation location, TabulaModelInformation mainModel, Map<String, CubeReference> innerMap) {
        TabulaModelInformation model;
        try {
            //Try and get the model at the specified location
            model = TabulaUtils.getModelInformation(location);
        } catch (Exception e) {
            DumbLibrary.getLogger().error("Unable to loadRawAnimations tabula model " + location, e);
            return;
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
    }

    /**
     * Loads a pose from a json file
     *
     * @param location  The location of the json model
     * @param mainModel The main model, of which to compare and take cubes from when they don't exist in the given model
     * @param innerMap  The inner map of which to add the data too.
     */
    private void loadJsonPose(ResourceLocation location, TabulaModelInformation mainModel, Map<String, CubeReference> innerMap) {
        try {
            //Create a new JsonParser
            JsonParser parser = new JsonParser();
            //If the location dosen't end with .json, then add it
            if (!location.getPath().endsWith(JSON_EXTENSION)) {
                location = new ResourceLocation(location.getNamespace(), location.getPath() + JSON_EXTENSION);
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
                //If the cube is already processed continue
                if (!cubeNames.contains(cubeName) || mainCube == null) {
                    continue;
                } else {
                    cubeNames.remove(cubeName);
                }
                //switch the version defined in the json
                if (version == 0) {
                    innerMap.put(cubeName, this.loadVersion0JsonCube(obj, mainCube));
                } else {
                    throw new IllegalArgumentException("Dont know how to handle version " + version);
                }
            }
            //Go through all the unedited cubes, and add a cube reference from the main model
            for (String cubeName : cubeNames) {
                innerMap.put(cubeName, CubeReference.fromCube(Objects.requireNonNull(mainModel.getCube(cubeName))));
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Loads a cube reference from a json object. Version 0
     *
     * @param obj      the json object
     * @param mainCube the main cube
     * @return The cube reference loaded from the given json object
     */
    private CubeReference loadVersion0JsonCube(JsonObject obj, TabulaModelInformation.Cube mainCube) {
        //place the new CubeReference in the innerMap. Values are dematerialized from the json object

        float[] rotation = mainCube.getRotation();
        float[] rotationPoint = mainCube.getRotationPoint();

        return new CubeReference(
                JsonUtils.hasField(obj, "rotation_x") ? JsonUtils.getFloat(obj, "rotation_x") : rotation[0],
                JsonUtils.hasField(obj, "rotation_y") ? JsonUtils.getFloat(obj, "rotation_y") : rotation[1],
                JsonUtils.hasField(obj, "rotation_z") ? JsonUtils.getFloat(obj, "rotation_z") : rotation[2],
                JsonUtils.hasField(obj, "position_x") ? JsonUtils.getFloat(obj, "position_x") : rotationPoint[0],
                JsonUtils.hasField(obj, "position_y") ? JsonUtils.getFloat(obj, "position_y") : rotationPoint[1],
                JsonUtils.hasField(obj, "position_z") ? JsonUtils.getFloat(obj, "position_z") : rotationPoint[2]
        );
    }

    /**
     * Creates the animation wrapper.
     *
     * @param entity    the entity you're creating the pass for
     * @param model     the model the animation pass wrapper would be applied to
     * @param inertia   should inertia be used
     * @param factories a list of {@link AnimationLayerFactory} (Note these should be Object::new)
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
