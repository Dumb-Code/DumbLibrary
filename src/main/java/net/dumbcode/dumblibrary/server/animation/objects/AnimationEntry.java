package net.dumbcode.dumblibrary.server.animation.objects;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NonNull;
import lombok.With;
import net.dumbcode.dumblibrary.server.animation.interpolation.Interpolation;
import net.dumbcode.dumblibrary.server.animation.interpolation.LinearInterpolation;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import javax.annotation.Nullable;

@Getter
@With
public class AnimationEntry {

    @NonNull
    private final Animation animation;
    private final int time; //-2 = loop, -1 = run until finished, x > 0 run for x amount of ticks
    private final boolean hold;
    private final AnimationFactor<?> speedFactor;
    private final AnimationFactor<?> degreeFactor;
    private final AnimationEntry exitAnimation;
    private final Interpolation interpolation;

    public AnimationEntry(Animation animation) {
        this(animation, -1, false, AnimationFactor.getDefault(), AnimationFactor.getDefault(), null, new LinearInterpolation());
    }

    public AnimationEntry(Animation animation, int time, boolean hold, AnimationFactor<?> speedFactor, AnimationFactor<?> degreeFactor, @Nullable AnimationEntry andThen, @Nullable Interpolation interpolation) {
        this.animation = animation;
        this.time = time;
        this.hold = hold;
        this.speedFactor = speedFactor;
        this.degreeFactor = degreeFactor;
        this.exitAnimation = andThen;
        if(interpolation != null) {
            this.interpolation = interpolation;
        } else {
            this.interpolation = new LinearInterpolation();
        }
    }

    public AnimationEntry loop() {
        return this.withTime(AnimationLayer.LOOP);
    }

    public void serialize(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.animation.getKey().getNamespace());
        ByteBufUtils.writeUTF8String(buf, this.animation.getKey().getPath());
        buf.writeInt(this.time);
        buf.writeBoolean(this.hold);
        ByteBufUtils.writeUTF8String(buf, this.speedFactor.getName());
        ByteBufUtils.writeUTF8String(buf, this.degreeFactor.getName());
        buf.writeBoolean(this.exitAnimation != null);
        if(this.exitAnimation != null) {
            this.exitAnimation.serialize(buf);
        }
    }

    public static AnimationEntry deserialize(ByteBuf buf) {
        return new AnimationEntry(
            new Animation(ByteBufUtils.readUTF8String(buf), ByteBufUtils.readUTF8String(buf)),
            buf.readInt(),
            buf.readBoolean(),
            AnimationFactor.getFactor(ByteBufUtils.readUTF8String(buf)),
            AnimationFactor.getFactor(ByteBufUtils.readUTF8String(buf)),
            buf.readBoolean() ? deserialize(buf) : null,
            null // TODO: Serialize
        );
    }
}
