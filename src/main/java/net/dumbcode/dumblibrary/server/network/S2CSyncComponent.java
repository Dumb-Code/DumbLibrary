package net.dumbcode.dumblibrary.server.network;

import io.netty.buffer.Unpooled;
import lombok.AllArgsConstructor;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentType;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

@AllArgsConstructor
public class S2CSyncComponent {

    private final int entityid;
    private final EntityComponentType<?, ?> type;
    private final byte[] data;


    public static S2CSyncComponent fromBytes(FriendlyByteBuf buf) {
        return new S2CSyncComponent(
            buf.readInt(),
            buf.readRegistryIdSafe(EntityComponentType.class),
            buf.readByteArray()
        );
    }

    public static void toBytes(S2CSyncComponent packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.entityid);
        buf.writeRegistryId(packet.type);
        buf.writeByteArray(packet.data);
    }

    public static void handle(S2CSyncComponent message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Entity entity = NetworkUtils.getPlayer(supplier).getCommandSenderWorld().getEntity(message.entityid);
            if (entity instanceof ComponentAccess) {
                ((ComponentAccess) entity).get(message.type).ifPresent(c -> c.deserializeSync(new FriendlyByteBuf(Unpooled.wrappedBuffer(message.data))));
            }
        });
        context.setPacketHandled(true);
    }
}
