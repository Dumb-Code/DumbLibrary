package net.dumbcode.dumblibrary.client.animation.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Information class to hold infomation about the model name, and the time it takes to complete
 */
@Getter
@AllArgsConstructor
public class PoseData {
    String modelName;
    float time;
}
