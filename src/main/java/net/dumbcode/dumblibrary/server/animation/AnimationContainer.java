package net.dumbcode.dumblibrary.server.animation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.util.internal.IntegerHolder;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.animation.data.CubeReference;
import net.dumbcode.dumblibrary.server.animation.data.PoseData;
import net.dumbcode.dumblibrary.server.animation.keyframes.KeyframeCompiler;
import net.dumbcode.dumblibrary.server.registry.DumbRegistries;
import net.dumbcode.dumblibrary.server.tabula.TabulaModelInformation;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.util.Strings;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * The animation container. Used to hold information about the animation container
 */
@Getter
public class AnimationContainer {

    private static final String JSON_EXTENSION = ".json";

    /**
     * A map of the list of model datas to use in per animation
     */
    private final Map<Animation, List<PoseData>> animations = Maps.newHashMap();

    private final TabulaModelInformation mainModel;

    public AnimationContainer(ResourceLocation regname) {

        //The base location of all the models
        String baseLoc = "models/entities/" + regname.getPath().replace("_", "/") + "/";

        this.mainModel = TabulaUtils.getModelInformation(new ResourceLocation(regname.getNamespace(), baseLoc + regname.getPath() + ".tbl"));
        this.loadAnimations(regname, baseLoc);
    }


    /**
     * Iterate through all the all the raw animations and puts the pose data into the animation. Also loads the main model with all the animation data.
     *
     * @param regname      The registry name of where to loadAnimationFromElement the data from
     * @param baseLoc      The base location. Derived from regname
     * @see AnimationContainer#animations
     */
    private void loadAnimations(ResourceLocation regname, String baseLoc) {
        try {
            Map<String, List<PoseData>> rawAnimations = this.loadRawAnimations(regname, baseLoc);


            List<PoseData> modelResources = Lists.newArrayList();
            //Iterate through all the lists poses in the rawData
            for (List<PoseData> poses : rawAnimations.values()) {
                modelResources.addAll(poses);
            }

            //Iterate through the list of poses defined in the json file
            for (Map.Entry<String, List<PoseData>> entry : rawAnimations.entrySet()) {
                //Use the animationGetter to get the Animation from the string
                Animation animation = DumbRegistries.ANIMATION_REGISTRY.getValue(new ResourceLocation(entry.getKey()));
                List<PoseData> poseData = Lists.newLinkedList(entry.getValue());

                this.animations.put(animation, poseData);
            }
            //loadAnimationFromElement the animation info
            if(!this.animations.containsKey(Animation.NONE) || this.animations.get(Animation.NONE).isEmpty()) {
                DumbLibrary.getLogger().error("Default animation (dumblibrary:none) had no models defined for {}", regname);
                return;
            }
            this.loadModelInformation(regname.getNamespace(), baseLoc, modelResources);
        } catch (Exception e) {
            DumbLibrary.getLogger().error("Unable to loadRawAnimations poses for " + regname, e);
        }
    }

    /**
     * Loads the raw map of (animation, List(Posedata))
     *
     * @param regname   The registry name of where to loadAnimationFromElement the animations
     * @param baseLoc   The base folder, derived from regname
     * @return the map of (animation, List(Posedata))
     */
    private Map<String, List<PoseData>> loadRawAnimations(ResourceLocation regname, String baseLoc) {
        Map<String, List<PoseData>> map = Maps.newHashMap();
        try {
            //Iterate through all the animation names
            for (ResourceLocation key : DumbRegistries.ANIMATION_REGISTRY.getKeys()) {
                //If the registry name is the same as the animation, then ignore the namespace of the key
                String animation = regname.getNamespace().equals(key.getNamespace()) ? key.getPath() : key.toString().replace(':', '/');
                //Loads the animation from the directory name
                this.loadAnimationFromDirectory(animation.toLowerCase(), key.toString(), regname, baseLoc, map);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not main json loadRawAnimations input stream for " + regname, e);
        }
        return map;
    }

    /**
     * Loads the animation from a directory
     * @param folder        The inner folder name to use
     * @param animation     The animation name
     * @param regName       The registry name for this
     * @param baseFolder    The base folder for the directory. Derived from regName
     * @param map           The map to add the animations too
     * @throws IOException if an I/O error occurs
     */
    private void loadAnimationFromDirectory(String folder, String animation, ResourceLocation regName, String baseFolder, Map<String, List<PoseData>> map) throws IOException {
        ResourceLocation folderLoc = new ResourceLocation(regName.getNamespace(), baseFolder + folder);
        if(this.tryLoadAnimationFromFile(folderLoc, map.computeIfAbsent(animation, a -> Lists.newArrayList()))) {
            return;
        }
        Path root = StreamUtils.getPath(folderLoc, false);
        if(Files.notExists(root)) {
            map.put(animation, Lists.newArrayList(new PoseData.FileResolvablePoseData(regName.getPath() + ".tbl", 10)));
            return;
        }
        JsonObject parsed = new JsonParser().parse(new InputStreamReader(Files.newInputStream(root.resolve("animation.json")))).getAsJsonObject();

        int time = JsonUtils.getInt(parsed, "base_time");
        Map<Integer, Integer> overrides = Maps.newHashMap();
        for (JsonElement jsonElement : JsonUtils.getJsonArray(parsed, "overrides", new JsonArray())) {
            JsonObject jobj = JsonUtils.getJsonObject(jsonElement, "overrides member");
            overrides.put(JsonUtils.getInt(jobj, "index"), JsonUtils.getInt(jobj, "time"));
        }
        IntegerHolder index = new IntegerHolder();
        try (Stream<Path> stream = Files.walk(root)){
            map.put(animation,
                    stream
                            .filter(path -> path.getFileName().toString().endsWith(".tbl"))
                            .map(path -> path.getParent().getFileName() + "/" + FilenameUtils.getBaseName(path.getFileName().toString()))
                            .filter(Strings::isNotEmpty)
                            .sorted()
                            .map(modelname -> new PoseData.FileResolvablePoseData(modelname, overrides.getOrDefault(index.value++, time)))
                            .collect(Lists::newLinkedList, List::add, LinkedList::addAll)
            );
            List<PoseData> poseData = map.get(animation);
            if(poseData.isEmpty()) {
                poseData.add(new PoseData.FileResolvablePoseData(regName.getPath() + ".tbl", 10));
            }
        } catch (IOException e) {
            DumbLibrary.getLogger().warn(e);
        }
    }

    private boolean tryLoadAnimationFromFile(ResourceLocation folder, List<PoseData> poseData) {
        poseData.clear();

        Path path = StreamUtils.getPath(new ResourceLocation(folder.getNamespace(), folder.getPath() + ".dca"), false);
        if(Files.notExists(path)) {
            return false;
        }
        try(DataInputStream dis = new DataInputStream(Files.newInputStream(path))) {
            float version = dis.readFloat(); //version
            if(version < 1) {
                throw new IOException("Need animation of at least version 1 to load. Please upload and re-download the animation"); //maybe have a method that instead of reading an unsigned short it should read a integer
            }

            KeyframeCompiler compiler = KeyframeCompiler.create(this.mainModel, (int) version);

            float length = dis.readFloat();
            for (int i = 0; i < length; i++) {
                KeyframeCompiler.Keyframe kf = compiler.addKeyframe(dis.readFloat(), dis.readFloat());

                float rotSize = dis.readFloat();
                for (int r = 0; r < rotSize; r++) {
                    kf.getRotationMap().put(dis.readUTF(), new float[]{(float) Math.toRadians(dis.readFloat()), (float) Math.toRadians(dis.readFloat()), (float) Math.toRadians(dis.readFloat())});
                }

                float posSize = dis.readFloat();
                for (int r = 0; r < posSize; r++) {
                    kf.getRotationPointMap().put(dis.readUTF(), new float[]{dis.readFloat(), dis.readFloat(), dis.readFloat()});
                }
            }

            poseData.addAll(compiler.compile());

            return true;
        } catch (IOException io) {
            DumbLibrary.getLogger().error(io);
            return false;
        }
    }

    /**
     * Loads the main model, as well as all the animation info
     * @param namespace            The registry name namespace
     * @param baseLoc    The base folder for the directory. Derived from regName
     * @param modelResources    the list of pose data for the main model
     */
    private void loadModelInformation(String namespace, String baseLoc, Iterable<PoseData> modelResources) {
        Map<String, Map<String, CubeReference>> refCache = Maps.newHashMap();

        //Load the main model, used for comparing the diffrence in cube location/rotation
        //Iterate through all the ModelLocations
        for (PoseData data : modelResources) {
            if(data instanceof PoseData.FileResolvablePoseData) {
                PoseData.FileResolvablePoseData ppd = (PoseData.FileResolvablePoseData) data;
                data.getCubes().clear();
                if (refCache.containsKey(ppd.getModelName())) {
                    data.getCubes().putAll(refCache.get(ppd.getModelName()));
                    continue;
                }
                Map<String, CubeReference> innerMap = Maps.newHashMap();
                refCache.put(ppd.getModelName(), innerMap);
                //Get the location of the model that represents the pose, and that we're going to generate the data for
                ResourceLocation location = new ResourceLocation(namespace, baseLoc + ppd.getModelName());

                if (ppd.getModelName().endsWith(JSON_EXTENSION)) {
                    this.loadJsonPose(location, this.mainModel, innerMap);
                } else {
                    this.loadTabulaPose(location, this.mainModel, innerMap);
                }
                data.getCubes().putAll(innerMap);
            }
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
     * Information class to hold the file name, and the file location of the pose
     */
    @Getter
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class ModelLocation {
        private String fileName;
        private String fullLocation;
    }
}
