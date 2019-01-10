package net.dumbcode.dumblibrary.client.animation.objects;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
public class Animation {
    @Accessors(fluent = true) private final boolean hold;
    @Accessors(fluent = true) private final boolean inertia;
    private final String identifier;

    private final List<PoseData> poseData = Lists.newArrayList();

    public Animation(boolean hold, boolean inertia, String identifier) {
        this.hold = hold;
        this.inertia = inertia;
        this.identifier = identifier;
    }

    public void populateList(List<PoseData> dataList) {
        this.poseData.clear();
        this.poseData.addAll(dataList);
    }

    public float getTotalLength() {
        float length = 0;
        for (PoseData datum : this.poseData) {
            length += datum.getTime();
        }
        return length; //cache ?
    }

}
