package net.dumbcode.dumblibrary.server.json;

import com.google.gson.*;
import lombok.Data;
import net.minecraft.util.JSONUtils;

import java.lang.reflect.Type;

@Data
public class JsonDinosaurModel {

    private final String headCuboid;
    private final JsonAnimator animator;
    private final float shadowSize;

    public static class Deserializer implements JsonDeserializer<JsonDinosaurModel> {
        @Override
        public JsonDinosaurModel deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (!element.isJsonObject()) {
                throw new JsonParseException("Expected Json Object, found " + element);
            }
            JsonObject json = element.getAsJsonObject();
            return new JsonDinosaurModel(
                JSONUtils.getAsString(json, "head_cuboid"),
                context.deserialize(JSONUtils.getAsJsonObject(json, "animator"), JsonAnimator.class),
                JSONUtils.getAsFloat(json, "shadow_size"));
        }
    }

}
