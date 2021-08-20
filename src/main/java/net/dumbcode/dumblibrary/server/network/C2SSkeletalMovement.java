package net.dumbcode.dumblibrary.server.network;

import lombok.AllArgsConstructor;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.taxidermy.BaseTaxidermyBlockEntity;
import net.dumbcode.dumblibrary.server.taxidermy.TaxidermyContainer;
import net.dumbcode.dumblibrary.server.taxidermy.TaxidermyHistory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

@AllArgsConstructor
public class C2SSkeletalMovement {

    private final String part;
    private final Vector3f rotations;
    private final Vector3f position;


    public static C2SSkeletalMovement fromBytes(PacketBuffer buf) {
        return new C2SSkeletalMovement(
            buf.readUtf(32767),
            new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat()),
            new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat())
        );
    }

    public static void toBytes(C2SSkeletalMovement packet, PacketBuffer buf) {
        buf.writeUtf(packet.part);
        buf.writeFloat(packet.rotations.x());
        buf.writeFloat(packet.rotations.y());
        buf.writeFloat(packet.rotations.z());
        buf.writeFloat(packet.position.x());
        buf.writeFloat(packet.position.y());
        buf.writeFloat(packet.position.z());
    }

    public static void handle(C2SSkeletalMovement message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayerEntity sender = context.getSender();
            World world = sender.level;
            if(sender.containerMenu instanceof TaxidermyContainer) {
                BaseTaxidermyBlockEntity builder = ((TaxidermyContainer) sender.containerMenu).getBlockEntity();
                builder.getHistory().add(new TaxidermyHistory.Record(message.part, new TaxidermyHistory.CubeProps(message.rotations, message.position)));
                builder.setChanged();
                DumbLibrary.NETWORK.send(NetworkUtils.forPos(world, builder.getBlockPos()), new S2CHistoryRecord(builder.getBlockPos(), message.part, message.rotations, message.position));
            }
        });
        context.setPacketHandled(true);
    }
}
