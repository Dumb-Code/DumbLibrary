package net.dumbcode.dumblibrary.client.animation.objects;

import com.google.gson.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.util.JsonUtils;

import java.lang.reflect.Type;

/**
 * A data class. Used to get the raw information from the json. Stores the location of the pose, and the time it takes
 */
@AllArgsConstructor
@Getter
public class PoseObject {
    private String poseLocation;
    private float ticksTime;

    public enum Deserializer implements JsonDeserializer<PoseObject> {
        INSTANCE;

        @Override
        public PoseObject deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject json = element.getAsJsonObject();
            return new PoseObject(
                    JsonUtils.getString(json ,"pose"),
                    JsonUtils.getFloat(json, "time")
            );
        }
    }
}
