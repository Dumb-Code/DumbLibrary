package net.dumbcode.dumblibrary.server.animation.objects;

import lombok.Value;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraft.util.ResourceLocation;

/**
 * Simple class to hold the animation data. This will most likely be removed later on when I re-do the animation system
 *
 * @author Wyn Price
 */
@Value
public class Animation  {

    public static final Animation NONE = new Animation(new ResourceLocation(DumbLibrary.MODID, "none"));

    private final ResourceLocation key;

    public Animation(ResourceLocation location) {
        this.key = location;
    }

    public Animation(String namespace, String path) {
        this(new ResourceLocation(namespace, path));
    }

    public AnimationEntry createEntry() {
        return new AnimationEntry(this);
    }
}
