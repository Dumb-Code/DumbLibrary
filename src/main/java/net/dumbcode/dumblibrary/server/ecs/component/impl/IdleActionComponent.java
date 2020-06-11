package net.dumbcode.dumblibrary.server.ecs.component.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.animation.objects.Animation;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.util.function.Supplier;

public class IdleActionComponent extends EntityComponent {
    public int soundTicks;
    public int animationTicks;

    public Animation idleAnimation;

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setString("Animation", idleAnimation.getKey().toString());
        return super.serialize(compound);
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        this.idleAnimation = new Animation(new ResourceLocation(compound.getString("Animation")));
        super.deserialize(compound);
    }

    @Setter
    @Getter
    @Accessors(chain = true)
    public static class Storage implements EntityComponentStorage<IdleActionComponent> {

        public Supplier<Animation> idleAnimation;

        @Override
        public void constructTo(IdleActionComponent component) {
            component.idleAnimation = idleAnimation.get();
        }
    }
}
