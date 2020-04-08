package net.dumbcode.dumblibrary.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class B13SplitNetworkPacket implements IMessage {

    private byte descriptor;
    private short collectionID;
    private byte packetID;
    private byte total;
    private byte[] data;

    public B13SplitNetworkPacket(byte descriptor, short collectionID, byte packetID, byte total, byte[] data) {
        this.descriptor = descriptor;
        this.collectionID = collectionID;
        this.packetID = packetID;
        this.total = total;
        this.data = data;
    }

    public B13SplitNetworkPacket() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.descriptor = buf.readByte();
        this.collectionID = buf.readShort();
        this.packetID = buf.readByte();
        this.total = buf.readByte();

        this.data = new byte[buf.readInt()];
        buf.readBytes(this.data);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(this.descriptor);
        buf.writeShort(this.collectionID);
        buf.writeByte(this.packetID);
        buf.writeByte(this.total);

        buf.writeInt(this.data.length);
        buf.writeBytes(this.data);
    }

    public static class Handler extends WorldModificationsMessageHandler<B13SplitNetworkPacket, B13SplitNetworkPacket> {

        @Override
        protected void handleMessage(B13SplitNetworkPacket message, MessageContext ctx, World world, EntityPlayer player) {
            SplitNetworkHandler.handleSplitMessage(message.descriptor, message.collectionID, message.packetID, message.total, message.data, player, ctx);
        }
    }
}
