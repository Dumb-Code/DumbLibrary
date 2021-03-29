package net.dumbcode.dumblibrary.server.network;

import lombok.AllArgsConstructor;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

@AllArgsConstructor
public class S3StopAnimation {

    private final int entityid;
    private final int channel;


    public static S3StopAnimation fromBytes(PacketBuffer buf) {
        return new S3StopAnimation(buf.readInt(), buf.readInt());
    }

    public static void toBytes(S3StopAnimation packet, PacketBuffer buf) {
        buf.writeInt(packet.entityid);
        buf.writeInt(packet.channel);
    }

    public static void handle(S3StopAnimation message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            World world = NetworkUtils.getPlayer(supplier).getCommandSenderWorld();
            Entity entity = world.getEntity(message.entityid);
            if (entity instanceof ComponentAccess) {
                ((ComponentAccess) entity).get(EntityComponentTypes.ANIMATION).ifPresent(c -> {
                    c.stopAnimation(message.channel);
                });
            }
        });
        context.setPacketHandled(true);
    }
}
