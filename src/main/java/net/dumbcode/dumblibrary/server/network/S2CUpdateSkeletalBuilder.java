package net.dumbcode.dumblibrary.server.network;

import lombok.AllArgsConstructor;
import net.dumbcode.dumblibrary.server.taxidermy.BaseTaxidermyBlockEntity;
import net.dumbcode.dumblibrary.server.utils.XYZAxis;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import sun.jvm.hotspot.opto.Block;

import java.util.function.Supplier;

@AllArgsConstructor
public class S2CUpdateSkeletalBuilder {

    private final BlockPos pos;
    private final String part;
    private final XYZAxis axis;
    private final int type;
    private final float value;


    public static S2CUpdateSkeletalBuilder fromBytes(PacketBuffer buf) {
        return new S2CUpdateSkeletalBuilder(
            buf.readBlockPos(),
            buf.readUtf(),
            XYZAxis.values()[buf.readInt()],
            buf.readByte(),
            buf.readFloat()
        );
    }

    public static void toBytes(S2CUpdateSkeletalBuilder packet, PacketBuffer buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeUtf(packet.part);
        buf.writeInt(packet.axis.ordinal());
        buf.writeByte(packet.type);
        buf.writeFloat(packet.value);
    }

    public static void handle(S2CUpdateSkeletalBuilder message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            BlockPos pos = message.pos;
            TileEntity blockEntity = NetworkUtils.getPlayer(supplier).getCommandSenderWorld().getBlockEntity(pos);
            if(blockEntity instanceof BaseTaxidermyBlockEntity) {
                BaseTaxidermyBlockEntity builder = (BaseTaxidermyBlockEntity)blockEntity;
                builder.getHistory().liveEdit(message.part, message.type, message.axis, message.value);
                builder.setChanged();
            }
        });
        context.setPacketHandled(true);
    }
}
