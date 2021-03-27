package net.dumbcode.dumblibrary.server.json.objects;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.*;
import net.minecraft.util.JSONUtils;

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
            if (!element.isJsonArray()) {
                throw new JsonSyntaxException("Expected a Json array, found " + element);
            }
            for (JsonElement jsonElement : element.getAsJsonArray()) {
                JsonObject json = JSONUtils.convertToJsonObject(jsonElement, "constants");
                List<String> names = Lists.newArrayList();
                for (JsonElement nameElement : JSONUtils.getAsJsonArray(json, "names")) {
                    names.add(JSONUtils.convertToString(nameElement, "names"));
                }
                constants.map.put(JSONUtils.getAsString(json, "key"), names);
            }
            return constants;
        }
    }

}
