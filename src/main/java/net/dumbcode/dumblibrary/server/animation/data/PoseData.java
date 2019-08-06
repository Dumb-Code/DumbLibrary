package net.dumbcode.dumblibrary.server.animation.data;

import com.google.common.collect.Maps;
import lombok.Data;
import lombok.Getter;

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