package net.dumbcode.dumblibrary.server.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class B14ReleaseCollection implements IMessage {

    private short collectionID;

    public B14ReleaseCollection(short collectionID) {
        this.collectionID = collectionID;
    }

    public B14ReleaseCollection() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.collectionID = buf.readShort();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeShort(this.collectionID);
    }

    public static class Handler extends WorldModificationsMessageHandler<B14ReleaseCollection, B14ReleaseCollection> {

        @Override
        protected void handleMessage(B14ReleaseCollection message, MessageContext ctx, World world, EntityPlayer player) {
            SplitNetworkHandler.releaseCollection(message.collectionID);
        }
    }
}
