package net.dumbcode.dumblibrary.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.dumblibrary.server.animation.objects.Animation;
import net.dumbcode.dumblibrary.server.info.AnimationSystemInfo;
import net.dumbcode.dumblibrary.server.info.AnimationSystemInfoRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class S0SyncAnimation implements IMessage {

    private int entityid;
    private ResourceLocation ais;
    private String animation;


    public S0SyncAnimation() {

    }

    public <E extends Entity> S0SyncAnimation(E entity, AnimationSystemInfo<?> info, Animation animation) {
        this.entityid = entity.getEntityId();
        this.ais = info.identifier();
        this.animation = animation.getIdentifier();
    }


    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityid = buf.readInt();
        this.ais = new ResourceLocation(ByteBufUtils.readUTF8String(buf));
        this.animation = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityid);
        ByteBufUtils.writeUTF8String(buf, this.ais.toString());
        ByteBufUtils.writeUTF8String(buf, this.animation);
    }

    public static class Handler extends WorldModificationsMessageHandler<S0SyncAnimation, S0SyncAnimation> {

        @Override
        protected void handleMessage(S0SyncAnimation message, MessageContext ctx, World world, EntityPlayer player) {
            Entity entity = world.getEntityByID(message.entityid);
            if (entity != null) {
                AnimationSystemInfoRegistry.setAnimationToEntity(entity, message.ais, message.animation);
            }
        }
    }
}
