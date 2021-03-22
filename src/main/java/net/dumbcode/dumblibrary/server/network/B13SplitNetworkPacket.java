package net.dumbcode.dumblibrary.server.network;

import lombok.RequiredArgsConstructor;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class B13SplitNetworkPacket {

    private final byte descriptor;
    private final short collectionID;
    private final byte packetID;
    private final byte total;
    private final byte[] data;

    public static B13SplitNetworkPacket fromBytes(PacketBuffer buf) {
        return new B13SplitNetworkPacket(
            buf.readByte(), buf.readShort(), buf.readByte(),
            buf.readByte(), buf.readByteArray());
    }

    public static void toBytes(B13SplitNetworkPacket packet, PacketBuffer buf) {
        buf.writeByte(packet.descriptor);
        buf.writeShort(packet.collectionID);
        buf.writeByte(packet.packetID);
        buf.writeByte(packet.total);

        buf.writeByteArray(packet.data);
    }

    public static void handle(B13SplitNetworkPacket message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        SplitNetworkHandler.handleSplitMessage(message.descriptor, message.collectionID, message.packetID, message.total, message.data, NetworkUtils.getPlayer(supplier), context);

    }
}
