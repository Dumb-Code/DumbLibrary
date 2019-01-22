package net.dumbcode.dumblibrary.client.animation.objects;

import com.google.common.collect.Maps;
import net.dumbcode.dumblibrary.client.animation.PoseHandler;
import net.dumbcode.dumblibrary.client.animation.objects.Animation;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A class to store infomation about a model, and its subsequent poses
 */
public class ModelInfomation<N extends IStringSerializable> {
    /**
     * Map of {@code <Model Name, <Cube Name, Cube Reference>>}
     */
    @SideOnly(Side.CLIENT)
    private Map<String, Map<String, CubeReference>> references;
    /**
     * A map of the list of model datas to use in per animation
     */
    private Map<Animation<N>, List<PoseData>> animations;

    public ModelInfomation() {
        this(null);
    }

    @SideOnly(Side.CLIENT)
    public ModelInfomation(Map<String, Map<String, CubeReference>> cuboids, Map<Animation<N>, List<PoseData>> animations) {
        this(animations);

        if (cuboids == null) {
            cuboids = Maps.newHashMap();
        }

        this.references = cuboids;
    }

    public ModelInfomation(Map<Animation<N>, List<PoseData>> animations) {
        if (animations == null) {
            animations = new LinkedHashMap<>();
        }

        this.animations = animations;
    }

    /**
     * @return the map of references
     * @see ModelInfomation#references
     */
    public Map<String, Map<String, CubeReference>> getReferences() {
        return references;
    }

    /**
     * @return the map of animations to list of pose data
     * @see ModelInfomation#animations
     */
    public Map<Animation<N>, List<PoseData>> getAnimations() {
        return animations;
    }
}