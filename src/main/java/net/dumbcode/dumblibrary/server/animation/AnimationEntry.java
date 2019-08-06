package net.dumbcode.dumblibrary.server.animation;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Wither;
import net.dumbcode.dumblibrary.server.animation.data.AnimationFactor;
import net.dumbcode.dumblibrary.server.animation.interpolation.Interpolation;
import net.dumbcode.dumblibrary.server.animation.interpolation.LinearInterpolation;
import net.dumbcode.dumblibrary.server.registry.DumbRegistries;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import javax.annotation.Nullable;

import static net.dumbcode.dumblibrary.server.animation.AnimationLayer.LOOP;

@Getter
@Wither
public class AnimationEntry {

    @NonNull
    private final Animation animation;
    private final int time; //-2 = loop, -1 = run until finished, x > 0 run for x amount of ticks
    private final boolean useInertia;
    private final boolean hold;
    private final AnimationFactor speedFactor;
    private final AnimationFactor degreeFactor;
    private final AnimationEntry exitAnimation;
    private final Interpolation interpolation;

    public AnimationEntry(Animation animation) {
        this(animation, -1, animation.inertia(), animation.hold(), AnimationFactor.DEFAULT, AnimationFactor.DEFAULT, null, new LinearInterpolation());
    }

    public AnimationEntry(Animation animation, int time, boolean useInertia, boolean hold, AnimationFactor speedFactor, AnimationFactor degreeFactor, @Nullable AnimationEntry andThen, @Nullable Interpolation interpolation) {
        this.animation = animation;
        this.time = time;
        this.useInertia = useInertia;
        this.hold = hold;
        this.speedFactor = speedFactor;
        this.degreeFactor = degreeFactor;
        this.exitAnimation = andThen;
        if (interpolation != null) {
            this.interpolation = interpolation;
        } else {
            this.interpolation = new LinearInterpolation();
        }
    }

    public AnimationEntry loop() {
        return this.withTime(LOOP);
    }

    public void serialize(ByteBuf buf) {
        ByteBufUtils.writeRegistryEntry(buf, this.animation);
        buf.writeInt(this.time);
        buf.writeBoolean(this.useInertia);
        buf.writeBoolean(this.hold);
        ByteBufUtils.writeRegistryEntry(buf, this.speedFactor);
        ByteBufUtils.writeRegistryEntry(buf, this.degreeFactor);
        buf.writeBoolean(this.exitAnimation != null);
        if (this.exitAnimation != null) {
            this.exitAnimation.serialize(buf);
        }
    }

    public static AnimationEntry deserialize(ByteBuf buf) {
        return new AnimationEntry(
                ByteBufUtils.readRegistryEntry(buf, DumbRegistries.ANIMATION_REGISTRY),
                buf.readInt(),
                buf.readBoolean(),
                buf.readBoolean(),
                ByteBufUtils.readRegistryEntry(buf, DumbRegistries.FLOAT_SUPPLIER_REGISTRY),
                ByteBufUtils.readRegistryEntry(buf, DumbRegistries.FLOAT_SUPPLIER_REGISTRY),
                buf.readBoolean() ? deserialize(buf) : null,
                null // TODO: Serialize
        );
    }
}