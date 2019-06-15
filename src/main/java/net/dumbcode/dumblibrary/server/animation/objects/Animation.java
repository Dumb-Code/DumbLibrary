package net.dumbcode.dumblibrary.server.animation.objects;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.utils.InjectedUtils;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Simple class to hold the animation data. This will most likely be removed later on when I re-do the animation system
 *
 * @author Wyn Price
 */
@Getter
@Accessors(fluent = true)
public class Animation extends IForgeRegistryEntry.Impl<Animation> {

    @GameRegistry.ObjectHolder(DumbLibrary.MODID + ":none")
    public static final Animation NONE = InjectedUtils.injected();

    private final boolean hold;
    private final boolean inertia;

    public Animation() {
        this(false, false);
    }

    public Animation(boolean hold) {
        this(hold, true);
    }

    public Animation(boolean hold, boolean useInertia) {
        this.hold = hold;
        this.inertia = useInertia;
    }
}
