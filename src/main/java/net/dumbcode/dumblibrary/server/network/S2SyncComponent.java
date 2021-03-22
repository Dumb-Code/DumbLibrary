package net.dumbcode.dumblibrary.server.network;

import io.netty.buffer.Unpooled;
import lombok.AllArgsConstructor;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentType;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

@AllArgsConstructor
public class S2SyncComponent {

    private final int entityid;
    private final EntityComponentType<?, ?> type;
    private final byte[] data;


    public static S2SyncComponent fromBytes(PacketBuffer buf) {
        return new S2SyncComponent(
            buf.readInt(),
            buf.readRegistryIdSafe(EntityComponentType.class),
            buf.readByteArray()
        );
    }

    public static void toBytes(S2SyncComponent packet, PacketBuffer buf) {
        buf.writeInt(packet.entityid);
        buf.writeRegistryId(packet.type);
        buf.writeByteArray(packet.data);
    }

    public static void handle(S2SyncComponent message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Entity entity = NetworkUtils.getPlayer(supplier).getCommandSenderWorld().getEntity(message.entityid);
            if (entity instanceof ComponentAccess) {
                ((ComponentAccess) entity).get(message.type).ifPresent(c -> c.deserializeSync(Unpooled.wrappedBuffer(message.data)));
            }
        });
    }
}
