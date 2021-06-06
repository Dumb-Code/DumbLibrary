package net.dumbcode.dumblibrary.server.network;

import lombok.AllArgsConstructor;
import net.dumbcode.dumblibrary.server.taxidermy.BaseTaxidermyBlockEntity;
import net.dumbcode.dumblibrary.server.taxidermy.TaxidermyHistory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

@AllArgsConstructor
public class S2CHistoryRecord {

    private final BlockPos pos;
    private final String part;
    private final Vector3f rotations;
    private final Vector3f position;

    public static S2CHistoryRecord fromBytes(PacketBuffer buf) {
        return new S2CHistoryRecord(
            buf.readBlockPos(),
            buf.readUtf(),
            new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat()),
            new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat())
        );
    }

    public static void toBytes(S2CHistoryRecord packet, PacketBuffer buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeUtf(packet.part);
        buf.writeFloat(packet.rotations.x());
        buf.writeFloat(packet.rotations.y());
        buf.writeFloat(packet.rotations.z());
        buf.writeFloat(packet.position.x());
        buf.writeFloat(packet.position.y());
        buf.writeFloat(packet.position.z());
    }

    public static void handle(S2CHistoryRecord message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            World world = NetworkUtils.getPlayer(supplier).getCommandSenderWorld();
            TileEntity blockEntity = world.getBlockEntity(message.pos);
            if(blockEntity instanceof BaseTaxidermyBlockEntity) {
                BaseTaxidermyBlockEntity builder = (BaseTaxidermyBlockEntity)blockEntity;
                builder.getHistory().add(new TaxidermyHistory.Record(message.part, new TaxidermyHistory.CubeProps(message.rotations, message.position)));
            }
        });
        context.setPacketHandled(true);
    }
}
