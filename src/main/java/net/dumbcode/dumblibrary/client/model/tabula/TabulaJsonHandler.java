package net.dumbcode.dumblibrary.client.model.tabula;

import com.google.gson.*;
import net.minecraft.util.JsonUtils;

import java.lang.reflect.Type;

import static net.minecraft.util.JsonUtils.*;

public enum TabulaJsonHandler implements JsonDeserializer<TabulaModelInformation> {
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
                getArr(json, "rotation"),
                getArr(json, "scale"),
                getArr(json, "txOffset"),
                getBoolean(json, "txMirror"),
                getFloat(json,"mcScale"),
                getFloat(json, "opacity"),
                getBoolean(json, "hidden"),
                getArrString(json, "metadata"),
                getString(json, "parentIdentifier", "null"),
                getString(json, "identifier")
        );

        getJsonArray(json, "children").forEach(child -> {
            TabulaModelInformation.Cube childcube = parseCube(child, info);
            cube.getChildren().add(childcube);
            childcube.setParent(cube);
        });
        return cube;
    }

    private String[] getArrString(JsonObject json, String str) {
        if(!isJsonArray(json, str)) {
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
        if(!isJsonArray(json, str)) {
            return new float[0];
        }
        JsonArray jsonArray = getJsonArray(json.get(str), str);
        float[] afloat = new float[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); i++) {
            afloat[i] = getFloat(jsonArray.get(i), str + "[" + i + "]");
        }
        return afloat;
    }

}
