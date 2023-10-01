package net.dumbcode.dumblibrary.server.animation;

import lombok.Value;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.studio.animation.instance.AnimationEntry;
import net.minecraft.resources.ResourceLocation;

/**
 * Simple class to hold the animation data. This will most likely be removed later on when I re-do the animation system
 *
 * @author Wyn Price
 */
@Value
public class Animation {

    public static final Animation NONE = new Animation(new ResourceLocation(DumbLibrary.MODID, "none"));

    private final ResourceLocation key;

    public Animation(ResourceLocation location) {
        this.key = location;
    }

    public Animation(String namespace, String path) {
        this(new ResourceLocation(namespace, path));
    }
}