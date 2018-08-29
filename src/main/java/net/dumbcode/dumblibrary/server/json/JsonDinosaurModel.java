package net.dumbcode.dumblibrary.server.json;

import com.google.gson.*;
import lombok.Data;
import net.minecraft.util.JsonUtils;

import java.lang.reflect.Type;

@Data
public class JsonDinosaurModel {

    private final String headCuboid;
    private final JsonAnimator animator;
    private final float shadowSize;

    public static class Deserializer implements JsonDeserializer<JsonDinosaurModel> {
        @Override
        public JsonDinosaurModel deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if(!element.isJsonObject()) {
                throw new JsonParseException("Expected Json Object, found " + JsonUtils.toString(element));
            }
            JsonObject json = element.getAsJsonObject();
            return new JsonDinosaurModel(
                    JsonUtils.getString(json, "head_cuboid"),
                    context.deserialize(JsonUtils.getJsonObject(json, "animator"), JsonAnimator.class),
                    JsonUtils.getFloat(json, "shadow_size"));
        }
    }

}
