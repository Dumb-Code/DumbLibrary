package net.dumbcode.dumblibrary.server.network;

import lombok.AllArgsConstructor;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationEntry;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

@AllArgsConstructor
public class S0SyncAnimation {

    private int entityid;
    private AnimationEntry entry;
    private int channel;

    public static S0SyncAnimation fromBytes(PacketBuffer buf) {
        return new S0SyncAnimation(buf.readInt(), AnimationEntry.deserialize(buf), buf.readInt());
    }

    public static void toBytes(S0SyncAnimation packet, PacketBuffer buf) {
        buf.writeInt(packet.entityid);
        packet.entry.serialize(buf);
        buf.writeInt(packet.channel);
    }

    public static void handle(S0SyncAnimation message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Entity entity = NetworkUtils.getPlayer(supplier).getCommandSenderWorld().getEntity(message.entityid);
            if (entity instanceof ComponentAccess) {
                ((ComponentAccess) entity).get(EntityComponentTypes.ANIMATION).ifPresent(c -> {
                    if(c.isReadyForAnimations()) {
                        c.playAnimation((ComponentAccess) entity, message.entry, message.channel);
                    } else {
                        c.proposeAnimation((ComponentAccess) entity, message.entry, message.channel, 10);
                    }
                });
            }
        });
    }
}
