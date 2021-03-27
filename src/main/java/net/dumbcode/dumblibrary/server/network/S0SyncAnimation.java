package net.dumbcode.dumblibrary.server.network;

import lombok.AllArgsConstructor;
import net.dumbcode.dumblibrary.server.animation.Animation;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

@AllArgsConstructor
public class S0SyncAnimation {

    private int entityid;
    private Animation entry;
    private int channel;

    public static S0SyncAnimation fromBytes(PacketBuffer buf) {
        return new S0SyncAnimation(buf.readInt(), new Animation(buf.readResourceLocation()), buf.readInt());
    }

    public static void toBytes(S0SyncAnimation packet, PacketBuffer buf) {
        buf.writeInt(packet.entityid);
        buf.writeResourceLocation(packet.entry.getKey());
        buf.writeInt(packet.channel);
    }

    public static void handle(S0SyncAnimation message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Entity entity = NetworkUtils.getPlayer(supplier).getCommandSenderWorld().getEntity(message.entityid);
            if (entity instanceof ComponentAccess) {
                ((ComponentAccess) entity).get(EntityComponentTypes.ANIMATION).ifPresent(c -> {
                    c.playAnimation(message.entry, message.channel);
                });
            }
        });
    }
}
