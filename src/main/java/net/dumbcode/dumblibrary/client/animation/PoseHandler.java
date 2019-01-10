package net.dumbcode.dumblibrary.client.animation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.*;
import lombok.*;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.animation.objects.*;
import net.dumbcode.dumblibrary.server.info.AnimationSystemInfo;
import net.dumbcode.dumblibrary.server.info.AnimationSystemInfoRegistry;
import net.dumbcode.dumblibrary.server.utils.DefaultHashMap;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModel;
import net.ilexiconn.llibrary.client.model.tools.AdvancedModelRenderer;
import net.dumbcode.dumblibrary.client.animation.objects.Animation;
import net.dumbcode.dumblibrary.client.animation.objects.AnimatedEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.vecmath.Vector3f;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Handles all the poses for the animals. Stores information about what the pose should look like, along with how long it is.
 */
public class PoseHandler<T extends EntityLiving & AnimatedEntity, N extends IStringSerializable> {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(PoseData.class, PoseData.Deserializer.INSTANCE)
            .create();

    private final Map<N, ModelInfomation> modelInfomationMap;

    @Getter
    private final AnimationSystemInfo<N, T> info;

    PoseHandler(ResourceLocation regname, AnimationSystemInfo<N, T> info) {

        AnimationSystemInfoRegistry.NAMESPACE.put(info.identifier(), info);

        this.info = info;
        this.modelInfomationMap = Maps.newHashMap();
        //The base location of all the models
        String baseLoc = "models/entities/" + regname.getResourcePath() + "/";

        for (N growth : info.allValues()) {
            N reference = growth;
            //If the growth stage isnt supported, default to ModelStage#ADULT
            if (!info.allAcceptedStages().contains(growth)) {
                reference = info.defaultStage();
            }
            ModelInfomation modelInfo;
            //If the model infomation is already collected for this growth stage, don't bother loading it all again, and just use the already created
            if (this.modelInfomationMap.containsKey(reference)) {
                modelInfo = this.modelInfomationMap.get(reference);
            } else {
                try {
                    //Get the growth name, in lowercase
                    String growthName = reference.getName().toLowerCase(Locale.ROOT);
                    //get the directory the of all the files
                    String growthDirectory = baseLoc + growthName + "/";
                    DinosaurAnimationInfomation rawData;
                    try {
                        //Get the input stream of the json file
                        @Cleanup InputStream jsonStream = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(regname.getResourceDomain(), growthDirectory + regname.getResourcePath() + "_" + growthName + ".json")).getInputStream();
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
                        //Create a new DinosaurAnimationInfomation with the PoseData info, and the version
                        rawData = new DinosaurAnimationInfomation(map, JsonUtils.isNumber(json.get("version")) ? JsonUtils.getInt(json, "version") : 0);
                    } catch (IOException e) {
                        throw new IllegalArgumentException("Could not main json load input stream for " + regname, e);
                    }
                    List<PoseData> modelResources = Lists.newArrayList();
                    //Iterate through all the lists poses in the rawData
                    for (List<PoseData> poses : rawData.getPoses().values()) {
                        //Iterate through each individual pose in that list
                        for (PoseData pose : poses) {
                            //Load the model location, and add it to the list
                            modelResources.add(pose.setLocation(new PoseHandler.ModelLocation(pose.getModelName(), growthDirectory + pose.getModelName())));
                        }
                    }

                    Map<Animation, List<PoseData>> animationMap = Maps.newHashMap();
                    //Iterate through the list of poses defined in the json file
                    for (val entry : rawData.getPoses().entrySet()) {
                        //Use the animationGetter to get the Animation from the string
                        Animation animation = info.getAnimation(entry.getKey());
                        List<PoseData> poseData = Lists.newArrayList(entry.getValue());
                        //Populate the list to the animation, and add it to the list
                        animation.populateList(poseData);
                        animationMap.put(animation, poseData);
                    }
                    //If the side is client side, then load the actual pose data
                    if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
                        //Get the main model for this ModelStage
                        String location = Objects.requireNonNull(info.stageToModelMap().get(reference), "Could not find main model location for " + regname + " as it was not defined");
                        //load the client information
                        modelInfo = loadClientInfomation(new ResourceLocation(regname.getResourceDomain(), growthDirectory + location), modelResources, animationMap);
                    } else {
                        modelInfo = new ModelInfomation(animationMap);
                    }
                } catch (Exception e) {
                    DumbLibrary.getLogger().error("Unable to load poses stage " + reference + " for " + regname, e);
                    modelInfo = new ModelInfomation();
                }
            }
            this.modelInfomationMap.put(growth, modelInfo);
        }
    }

    /**
     * Load the clinet infomation for the poses. Loads each pose file, and figures out the diff with the main mdoe
     *
     * @param mainModelLocation   The location of the main model.
     * @param animations          A map of all the model data pertaining to the animation
     * @return The ModelInfomation class, used store data about each pose
     */
    @SideOnly(Side.CLIENT)
    private ModelInfomation loadClientInfomation(ResourceLocation mainModelLocation, Iterable<PoseData> modelResources, Map<Animation, List<PoseData>> animations) {
        //Load the main model, used for comparing the diffrence in cube location/rotation
        TabulaModel mainModel = TabulaUtils.getModel(mainModelLocation);
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
            ResourceLocation location = new ResourceLocation(mainModelLocation.getResourceDomain(), modelResource.getFullLocation());
            //If the pose location is the same as the mainModel, skip it and add all the mainModel data instead. Prevents loading the same model twice
            if (location.equals(mainModelLocation)) {
                for (val cube : mainModel.getCubes().entrySet()) {
                    innerMap.put(cube.getKey(), CubeReference.fromCube(cube.getValue()));
                }
            } else {
                //If the file ends with .tbl (The old way). Currently only the working way which is why its enforced. I need to check the integrity of the python script
                if (modelResource.getFileName().endsWith(".tbl") || true) {
                    TabulaModel model;
                    try {
                        //Try and get the model at the specified location
                        model = TabulaUtils.getModel(location);
                    } catch (Exception e) {
                        DumbLibrary.getLogger().error("Unable to load tabula model " + location, e);
                        continue;
                    }
                    //Iterate through all the cube names in the main model
                    for (String cubeName : mainModel.getCubes().keySet()) {
                        //Get the cube of which the name links to
                        AdvancedModelRenderer cube = model.getCube(cubeName);
                        //If the cube does not exist in pose model (which shouldn't happen), then just default to the main models cube
                        if (cube == null) {
                            cube = mainModel.getCube(cubeName);
                        }
                        //Create a CubeReference (data about the cubes position/rotation) and put it in the innerMap, with the key being the cube name
                        innerMap.put(cubeName, CubeReference.fromCube(cube));
                    }
                } else {
                    try {
                        //Create a new JsonParser
                        JsonParser parser = new JsonParser();
                        //If the location dosen't end with .json, then add it
                        if (!location.getResourcePath().endsWith(".json")) {
                            location = new ResourceLocation(location.getResourceDomain(), location.getResourcePath() + ".json");
                        }
                        //Get the json inputstream
                        @Cleanup InputStream stream = Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream();
                        //Create a Reader for the inputstream
                        @Cleanup InputStreamReader reader = new InputStreamReader(stream);
                        //Read the inputstream as a json object
                        JsonObject json = parser.parse(reader).getAsJsonObject();
                        //Get the version of the json file
                        int version = JsonUtils.getInt(json, "version");
                        //Get a list of all the main model cubes. Used for determining which cubes are not overriden
                        List<String> cubeNames = Lists.newArrayList(mainModel.getCubes().keySet());
                        //Go through the list of overrides in the json.
                        for (JsonElement jsonElement : JsonUtils.getJsonArray(json, "overrides")) {
                            //Get the element as a json object
                            JsonObject obj = jsonElement.getAsJsonObject();
                            //Get the field inside the json object called "cube_name"
                            String cubeName = JsonUtils.getString(obj, "cube_name");
                            //get the cube with the same name that's in the mainModel
                            AdvancedModelRenderer mainCube = mainModel.getCube(cubeName);
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
                                    innerMap.put(cubeName, new CubeReference(
                                            JsonUtils.hasField(obj, "rotation_x") ? JsonUtils.getFloat(obj, "rotation_x") : mainCube.defaultRotationX,
                                            JsonUtils.hasField(obj, "rotation_y") ? JsonUtils.getFloat(obj, "rotation_y") : mainCube.defaultRotationY,
                                            JsonUtils.hasField(obj, "rotation_z") ? JsonUtils.getFloat(obj, "rotation_z") : mainCube.defaultRotationZ,
                                            JsonUtils.hasField(obj, "position_x") ? JsonUtils.getFloat(obj, "position_x") : mainCube.defaultPositionX,
                                            JsonUtils.hasField(obj, "position_y") ? JsonUtils.getFloat(obj, "position_y") : mainCube.defaultPositionY,
                                            JsonUtils.hasField(obj, "position_z") ? JsonUtils.getFloat(obj, "position_z") : mainCube.defaultPositionZ
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
        return new ModelInfomation(map, animations);
    }

    /**
     * Creates the animation wrapper.
     *
     * @param entity              the entity you're creating the pass for
     * @param model               the model the animation pass wrapper would be applied to
     * @param defaultAnimation    the default animation for the entity. Should be a idle animation, or something similar
     * @param inertia             should inertia be used
     * @param factories           a list of {@link AnimationLayerFactory} (Note these should be Object::new)
     * @param <T>                 the entity type being used.
     * @return A new animation wrapper.
     */
    @SuppressWarnings("unchecked")
    public <T extends AnimatedEntity> AnimationRunWrapper<T> createAnimationWrapper(T entity, TabulaModel model, Animation defaultAnimation,
                                                                                    N stage, boolean inertia,
                                                                                    AnimationLayerFactory... factories) {
        ModelInfomation modelInfo = this.modelInfomationMap.get(stage);
        List<AnimationLayer<T>> list = Lists.newArrayList();
        for (val factory : factories) {
            list.add(factory.createWrapper(entity, model, new DefaultHashMap<String, AnimationRunWrapper.CubeWrapper>(AnimationRunWrapper.CubeWrapper::new)::getOrPut, defaultAnimation, inertia));
        }
        return new AnimationRunWrapper(entity, list);
    }


    /**
     * Get the full length of an animation
     *
     * @param animation   the animation
     * @param growthStage the growth stage of the entity
     * @return the length of the animation
     */
    public float getAnimationLength(Animation animation, N growthStage) {
        float duration = 0;
        if (animation != null) {
            //Get the list of poses for the certian animation, or a new list if there is none
            List<PoseData> poses = animation.getPoseData();
            for (PoseData pose : poses) {
                //Add up the time
                duration += pose.getTime();
            }
        }
        return duration;
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
     * Information class to hold the raw data from the json file
     */
    @Getter
    @AllArgsConstructor
    public class DinosaurAnimationInfomation {
        Map<String, List<PoseData>> poses;
        int version;
    }

    /**
     * The factory for creating {@link AnimationLayer}
     */
    public interface AnimationLayerFactory {
        <T extends AnimatedEntity> AnimationLayer<T> createWrapper(T entity, TabulaModel model, Function<String, AnimationRunWrapper.CubeWrapper> cuberef, Animation defaultAnimation, boolean inertia);
    }
}
