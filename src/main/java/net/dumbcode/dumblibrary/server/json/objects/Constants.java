package net.dumbcode.dumblibrary.server.json.objects;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.*;
import net.minecraft.util.JsonUtils;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class Constants {

    Map<String, List<String>> map = Maps.newHashMap();

    public List<String> getStringParts(String constant) {
        return map.getOrDefault(constant, Lists.newArrayList());
    }

    public static class Deserializer implements JsonDeserializer<Constants> {
        @Override
        public Constants deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Constants constants = new Constants();
            if(!element.isJsonArray()) {
                throw new JsonSyntaxException("Expected a Json array, found " + JsonUtils.toString(element));
            }
            for (JsonElement jsonElement : element.getAsJsonArray()) {
                JsonObject json = JsonUtils.getJsonObject(jsonElement, "constants");
                List<String> names = Lists.newArrayList();
                for (JsonElement nameElement : JsonUtils.getJsonArray(json, "names")) {
                    names.add(JsonUtils.getString(nameElement, "names"));
                }
                constants.map.put(JsonUtils.getString(json, "key"), names);
            }
            return constants;
        }
    }

}
