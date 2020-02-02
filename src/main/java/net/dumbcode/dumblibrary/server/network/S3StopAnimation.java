package net.dumbcode.dumblibrary.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationLayer;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class S3StopAnimation implements IMessage {

    private int entityid;
    private int channel;

    public S3StopAnimation() {
    }

    public S3StopAnimation(Entity entity, int channel) {
        this.entityid = entity.getEntityId();
        this.channel = channel;
    }


    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityid = buf.readInt();
        this.channel = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityid);
        buf.writeInt(this.channel);
    }

    public static class Handler extends WorldModificationsMessageHandler<S3StopAnimation, S3StopAnimation> {

        @Override
        protected void handleMessage(S3StopAnimation message, MessageContext ctx, World world, EntityPlayer player) {
            Entity entity = world.getEntityByID(message.entityid);
            if (entity instanceof ComponentAccess) {
                ((ComponentAccess) entity).get(EntityComponentTypes.ANIMATION).ifPresent(c -> {
                    c.stopAnimation(entity, message.channel);
                });
            }
        }
    }
}
