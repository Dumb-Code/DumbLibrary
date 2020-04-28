package net.dumbcode.dumblibrary.server.animation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.*;
import io.netty.util.internal.IntegerHolder;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.animation.keyframes.KeyframeCompiler;
import net.dumbcode.dumblibrary.server.animation.objects.Animation;
import net.dumbcode.dumblibrary.server.animation.objects.CubeReference;
import net.dumbcode.dumblibrary.server.animation.objects.PoseData;
import net.dumbcode.dumblibrary.server.registry.DumbRegistries;
import net.dumbcode.dumblibrary.server.tabula.TabulaModelInformation;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.util.Strings;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
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

    public Function<Animation, List<PoseData>> createDataGetter() {
        return animation -> this.animations.getOrDefault(animation, Collections.emptyList());
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
            this.loadRawAnimations(regname, baseLoc);

            List<PoseData> modelResources = Lists.newArrayList();
            //Iterate through all the lists poses in the rawData
            for (List<PoseData> poses : this.animations.values()) {
                modelResources.addAll(poses);
            }

            this.loadModelInformation(regname.getNamespace(), baseLoc, modelResources);
        } catch (Exception e) {
            DumbLibrary.getLogger().error("Unable to load animation for " + regname, e);
        }
    }

    /**
     * Loads the raw map of (animation, List(Posedata))
     *
     * @param regname   The registry name of where to loadAnimationFromElement the animations
     * @param baseLoc   The base folder, derived from regname
     * @return the map of (animation, List(Posedata))
     */
    private void loadRawAnimations(ResourceLocation regname, String baseLoc) throws IOException {
        StreamUtils.getPath(new ResourceLocation(regname.getNamespace(), baseLoc), false, path -> {
            for (Path p : StreamUtils.listPaths(path)) {
                if (Files.isDirectory(p)) {
                    boolean tblFiles = false;
                    for (Path innerP : StreamUtils.listPaths(p)) {
                        String name = this.lastFolder(p);
                        if(Files.isDirectory(innerP)) {
                            //Folder > Folder, therefore is a list of .tbl files from a foreign domain
                            String pathName = this.lastFolder(innerP);
                            this.animations.put(new Animation(name, pathName), this.loadAnimationFromDirectory(innerP));
                        } else {
                            //Folder > Files, therefore CAN be a list of dca files from a foriegn domain.
                            if(FilenameUtils.getExtension(innerP.toString()).equals("dca")) {
                                String pathName = FilenameUtils.getBaseName(innerP.toString());
                                this.animations.put(new Animation(name, pathName), this.loadAnimationFromDCAFile(innerP));
                            }
                            if(FilenameUtils.getExtension(innerP.toString()).equals("tbl")) {
                                tblFiles = true;
                            }
                        }
                    }
                    //If tbl files are present then load the .tbl files from the active domain
                    if(tblFiles) {
                        this.animations.put(new Animation(regname.getNamespace(), this.lastFolder(p)), this.loadAnimationFromDirectory(p));
                    }
                } else if(FilenameUtils.getExtension(p.toString()).equals("dca")) {
                    //Load the .dca file with the active domain
                    String pathName = FilenameUtils.getBaseName(p.toString());
                    this.animations.put(new Animation(regname.getNamespace(), pathName), this.loadAnimationFromDCAFile(p));
                }
            }
            return null;
        });
    }

    private String lastFolder(Path path) {
        String[] split = path.toString().split("/");
        return split[split.length - 1];
    }

    /**
     * Loads the animation from a directory
     * @param root the path folder root to load from
     * @throws IOException if an I/O error occurs
     */
    private List<PoseData> loadAnimationFromDirectory(Path root) throws IOException {
        int time;
        Map<Integer, Integer> overrides = Maps.newHashMap();

        Path animationFile = root.resolve("animation.json");
        if(Files.exists(animationFile)) {
            JsonObject parsed = new JsonParser().parse(new InputStreamReader(Files.newInputStream(animationFile))).getAsJsonObject();
            time = JsonUtils.getInt(parsed, "base_time", 5);
            for (JsonElement jsonElement : JsonUtils.getJsonArray(parsed, "overrides", new JsonArray())) {
                JsonObject jobj = JsonUtils.getJsonObject(jsonElement, "overrides member");
                overrides.put(JsonUtils.getInt(jobj, "index"), JsonUtils.getInt(jobj, "time"));
            }
        } else {
            time = 5;
        }

        IntegerHolder index = new IntegerHolder();
        try (Stream<Path> stream = Files.walk(root)) {
            return stream
                .filter(path -> path.getFileName().toString().endsWith(".tbl"))
                .map(path -> path.getParent().getFileName() + "/" + FilenameUtils.getBaseName(path.getFileName().toString()))
                .filter(Strings::isNotEmpty)
                .sorted()
                .map(modelname -> new PoseData.FileResolvablePoseData(modelname, overrides.getOrDefault(index.value++, time)))
                .collect(Lists::newLinkedList, List::add, LinkedList::addAll);
        }
    }

    private List<PoseData> loadAnimationFromDCAFile(Path path) throws IOException {
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

                if(version >= 2) {
                    //TODO: progression points
                    float ppSize = dis.readFloat(); //Progression points size
                    for (int p = 0; p < ppSize; p++) {
                        dis.readFloat();//x
                        dis.readFloat();//y
                    }
                }
            }

            return compiler.compile();
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

                this.loadTabulaPose(location, this.mainModel, innerMap);

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
