package net.dumbcode.dumblibrary.server.animation.objects;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.animation.AnimationContainer;
import net.minecraft.util.JsonUtils;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Information class to hold infomation about the model name, and the time it takes to complete
 */
@Getter
@Data
public class PoseData {
    private final float time;
    private final Map<String, CubeReference> cubes = Maps.newHashMap();

    /**
     * This class should be used if the pose data should be resolved with our file system. <br>
     * This can be the the json file system, or the folder with poses in system
     */
    @Getter
    public static class FileResolvablePoseData extends PoseData {
        private final String modelName;

        public FileResolvablePoseData(String modelName, float time) {
            super(time);
            this.modelName = modelName;
        }
    }

}