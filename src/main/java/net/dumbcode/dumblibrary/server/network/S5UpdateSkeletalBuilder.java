package net.dumbcode.dumblibrary.server.network;

import lombok.AllArgsConstructor;
import net.dumbcode.dumblibrary.server.taxidermy.BaseTaxidermyBlockEntity;
import net.dumbcode.dumblibrary.server.utils.XYZAxis;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

@AllArgsConstructor
public class S5UpdateSkeletalBuilder {

    private final int x;
    private final int y;
    private final int z;
    private final String part;
    private final XYZAxis axis;
    private final int type;
    private final float value;


    public static S5UpdateSkeletalBuilder fromBytes(PacketBuffer buf) {
        return new S5UpdateSkeletalBuilder(
            buf.readInt(),
            buf.readInt(),
            buf.readInt(),
            buf.readUtf(),
            XYZAxis.values()[buf.readInt()],
            buf.readByte(),
            buf.readFloat()
        );
    }

    public static void toBytes(S5UpdateSkeletalBuilder packet, PacketBuffer buf) {
        buf.writeInt(packet.x);
        buf.writeInt(packet.y);
        buf.writeInt(packet.z);
        buf.writeUtf(packet.part);
        buf.writeInt(packet.axis.ordinal());
        buf.writeByte(packet.type);
        buf.writeFloat(packet.value);
    }

    public static void handle(S5UpdateSkeletalBuilder message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            BlockPos pos = new BlockPos(message.x, message.y, message.z);
            TileEntity blockEntity = NetworkUtils.getPlayer(supplier).getCommandSenderWorld().getBlockEntity(pos);
            if(blockEntity instanceof BaseTaxidermyBlockEntity) {
                BaseTaxidermyBlockEntity builder = (BaseTaxidermyBlockEntity)blockEntity;
                builder.getHistory().liveEdit(message.part, message.type, message.axis, message.value);
                builder.setChanged();
            }
        });
    }
}
