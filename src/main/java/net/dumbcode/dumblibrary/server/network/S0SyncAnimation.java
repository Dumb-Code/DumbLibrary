package net.dumbcode.dumblibrary.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationLayer;
import net.dumbcode.dumblibrary.server.entity.ComponentAccess;
import net.dumbcode.dumblibrary.server.entity.component.EntityComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class S0SyncAnimation implements IMessage {

    private int entityid;
    private AnimationLayer.AnimationEntry entry;
    private int channel;

    public S0SyncAnimation() {
    }

    public <E extends Entity> S0SyncAnimation(int operation, E entity, AnimationLayer.AnimationEntry entry, int channel) {
        this.entityid = entity.getEntityId();
        this.entry = entry;
        this.channel = channel;
    }


    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityid = buf.readInt();
        this.entry = AnimationLayer.AnimationEntry.deserialize(buf);
        this.channel = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityid);
        this.entry.serialize(buf);
        buf.writeInt(this.channel);
    }

    public static class Handler extends WorldModificationsMessageHandler<S0SyncAnimation, S0SyncAnimation> {

        @Override
        protected void handleMessage(S0SyncAnimation message, MessageContext ctx, World world, EntityPlayer player) {
            Entity entity = world.getEntityByID(message.entityid);
            if (entity instanceof ComponentAccess) {
                ((ComponentAccess) entity).get(EntityComponentTypes.ANIMATION).ifPresent(c -> {
                    if(c.isReadyForAnimations()) {
                        c.playAnimation((ComponentAccess) entity, message.entry, message.channel);
                    } else {
                        c.proposeAnimation((ComponentAccess) entity, message.entry, message.channel, 10);
                    }
                });
            }
        }
    }
}
