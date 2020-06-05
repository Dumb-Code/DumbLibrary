package net.dumbcode.dumblibrary.server.tabula;

import com.google.gson.*;
import net.dumbcode.dumblibrary.server.utils.CollectorUtils;
import net.minecraft.util.JsonUtils;

import java.lang.reflect.Type;

import static net.minecraft.util.JsonUtils.*;

public enum TabulaJsonHandler implements JsonDeserializer<TabulaModelInformation>, JsonSerializer<TabulaModelInformation> {
    INSTANCE;

    public static final Gson GSON = new GsonBuilder().registerTypeAdapter(TabulaModelInformation.class, INSTANCE).create();

    @Override
    public TabulaModelInformation deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = JsonUtils.getJsonObject(element, "root");
        TabulaModelInformation info = new TabulaModelInformation(
                getString(json, "modelName"),
                getString(json, "authorName"),
                getInt(json, "projVersion"),
                getArrString(json, "metadata"),
                getInt(json, "textureWidth"),
                getInt(json, "textureHeight"),
                getArr(json, "scale"),
                getInt(json, "cubeCount")
        );

        TabulaModelInformation.CubeGroup rootGroup = info.group("@@ROOT@@", false, false, new String[0], "~~root~~");
        info.getGroups().add(rootGroup.setRoot(true));
        getJsonArray(json, "cubes").forEach(cube -> rootGroup.getCubeList().add(parseCube(cube, info)));
        getJsonArray(json, "cubeGroups").forEach(group -> info.getGroups().add(parseGroup(group, info)));

        return info;
    }


    private TabulaModelInformation.CubeGroup parseGroup(JsonElement element, TabulaModelInformation info) {
        JsonObject json = getJsonObject(element, "cubeGroups");

        TabulaModelInformation.CubeGroup group = info.group(
                getString(json, "name"),
                getBoolean(json, "txMirror"),
                getBoolean(json, "hidden"),
                getArrString(json, "metadata"),
                getString(json, "identifier")
        );
        getJsonArray(json, "cubes").forEach(cube -> group.getCubeList().add(parseCube(cube, info)));

        getJsonArray(json, "cubeGroups").forEach(cubeGroup -> group.getChildGroups().add(parseGroup(cubeGroup, info)));

        return group;
    }

    private TabulaModelInformation.Cube parseCube(JsonElement element, TabulaModelInformation info) {
        JsonObject json = getJsonObject(element, "cubes");
        TabulaModelInformation.Cube cube = info.cube(
                getString(json, "name"),
                getArr(json, "dimensions"),
                getArr(json, "position"),
                getArr(json, "offset"),
                getArrAngles(json, "rotation"),
                getArr(json, "scale"),
                getArr(json, "txOffset"),
                getBoolean(json, "txMirror"),
                getFloat(json, "mcScale"),
                getFloat(json, "opacity"),
                getBoolean(json, "hidden"),
                getArrString(json, "metadata"),
                getString(json, "parentIdentifier", "null"),
                getString(json, "identifier")
        );
        info.getCubes().add(cube);
        getJsonArray(json, "children").forEach(child -> {
            TabulaModelInformation.Cube childcube = parseCube(child, info);
            cube.getChildren().add(childcube);
            childcube.setParent(cube);
        });
        return cube;
    }

    private String[] getArrString(JsonObject json, String str) {
        if (!isJsonArray(json, str)) {
            return new String[0];
        }
        JsonArray jsonArray = getJsonArray(json, str);
        String[] aString = new String[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); i++) {
            aString[i] = getString(jsonArray.get(i), str + "[" + i + "]");
        }
        return aString;
    }

    private float[] getArr(JsonObject json, String str) {
        if (!isJsonArray(json, str)) {
            return new float[0];
        }
        JsonArray jsonArray = getJsonArray(json.get(str), str);
        float[] afloat = new float[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); i++) {
            afloat[i] = getFloat(jsonArray.get(i), str + "[" + i + "]");
        }
        return afloat;
    }

    private float[] getArrAngles(JsonObject json, String str) {
        float[] arr = getArr(json, str);
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (float) Math.toRadians(arr[i]);
        }
        return arr;
    }

    @Override
    public JsonElement serialize(TabulaModelInformation src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();

        obj.addProperty("modelName", src.getModelName());
        obj.addProperty("authorName", src.getAuthorName());
        obj.addProperty("projVersion", src.getProjectVersion());
        obj.add("metadata", toArr(src.getMetadata()));
        obj.addProperty("textureWidth", src.getTexWidth());
        obj.addProperty("textureHeight", src.getTexHeight());
        obj.add("scale", toArr(src.getScale()));
        obj.addProperty("cubeCount", src.getCubeCount());

        JsonArray groups = new JsonArray();
        obj.add("cubeGroups", groups);

        for (TabulaModelInformation.CubeGroup group : src.getGroups()) {
            if(group.getName().equals("@@ROOT@@") && group.getIdentifier().equals("~~root~~")) {
                obj.add("cubes", group.getCubeList().stream().map(this::jsonifyCube).collect(CollectorUtils.toJsonArray()));
            } else {
                groups.add(this.jsonifyGroup(group));
            }
        }

        return obj;
    }

    private JsonObject jsonifyCube(TabulaModelInformation.Cube cube) {
        JsonObject json = new JsonObject();

        json.addProperty("name", cube.getName());
        json.add("dimensions", toArr(cube.getDimension()));
        json.add("position", toArr(cube.getRotationPoint()));
        json.add("offset", toArr(cube.getOffset()));
        json.add("rotation", toArrAngles(cube.getRotation()));
        json.add("scale", toArr(cube.getScale()));
        json.add("txOffset", toArr(cube.getTexOffset()));
        json.addProperty("txMirror", cube.isTextureMirror());
        json.addProperty("mcScale", cube.getMcScale());
        json.addProperty("opacity", cube.getOpacity());
        json.addProperty("hidden", cube.isHidden());
        json.add("metadata", toArr(cube.getMetadata()));
        if(cube.getParent() != null) {
            json.addProperty("parentIdentifier", cube.getParentIdentifier());
        }
        json.addProperty("identifier", cube.getIdentifier());

        json.add("children", cube.getChildren().stream().map(this::jsonifyCube).collect(CollectorUtils.toJsonArray()));

        return json;
    }

    private JsonObject jsonifyGroup(TabulaModelInformation.CubeGroup group) {
        JsonObject json = new JsonObject();

        json.addProperty("name", group.getName());
        json.addProperty("txMirror", group.isTextureMirror());
        json.addProperty("hidden", group.isHidden());
        json.add("metadata", toArr(group.getMetadata()));
        json.addProperty("identifier", group.getIdentifier());

        json.add("cubes", group.getCubeList().stream().map(this::jsonifyCube).collect(CollectorUtils.toJsonArray()));
        json.add("cubeGroups", group.getChildGroups().stream().map(this::jsonifyGroup).collect(CollectorUtils.toJsonArray()));

        return json;
    }

    private JsonArray toArr(float... arr) {
        JsonArray array = new JsonArray();
        for (float v : arr) {
            array.add(v);
        }
        return array;
    }

    private JsonArray toArrAngles(float... arr) {
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (float) Math.toDegrees(arr[i]);
        }
        return toArr(arr);
    }

    private JsonArray toArr(String... arr) {
        JsonArray array = new JsonArray();
        for (String v : arr) {
            array.add(v);
        }
        return array;
    }
}
