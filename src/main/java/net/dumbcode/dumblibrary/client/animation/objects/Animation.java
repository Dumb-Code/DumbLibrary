package net.dumbcode.dumblibrary.client.animation.objects;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.experimental.Accessors;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Map;

@Data
public class Animation<T> {
    @Accessors(fluent = true) private final boolean hold;
    @Accessors(fluent = true) private final boolean inertia;
    private final String identifier;

    private final Map<T, List<PoseData>> poseData = Maps.newHashMap();

    public Animation(boolean hold, boolean inertia, String identifier) {
        this.hold = hold;
        this.inertia = inertia;
        this.identifier = identifier;
    }

    public void populateList(T type, List<PoseData> dataList) {
        this.poseData.put(type, Lists.newArrayList(dataList));
    }

    public float getTotalLength(T type) {
        float length = 0;
        for (PoseData datum : this.poseData.get(type)) {
            length += datum.getTime();
        }
        return length; //cache ?
    }

}
