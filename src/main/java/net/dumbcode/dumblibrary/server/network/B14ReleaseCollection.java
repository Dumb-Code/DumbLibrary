package net.dumbcode.dumblibrary.server.network;

import lombok.AllArgsConstructor;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

@AllArgsConstructor
public class B14ReleaseCollection {

    private final short collectionID;

    public static B14ReleaseCollection fromBytes(PacketBuffer buf) {
        return new B14ReleaseCollection(buf.readShort());
    }

    public static void toBytes(B14ReleaseCollection packet, PacketBuffer buf) {
        buf.writeShort(packet.collectionID);
    }

    public static void handle(B14ReleaseCollection message, Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> SplitNetworkHandler.releaseCollection(message.collectionID));
        supplier.get().setPacketHandled(true);
    }
}
