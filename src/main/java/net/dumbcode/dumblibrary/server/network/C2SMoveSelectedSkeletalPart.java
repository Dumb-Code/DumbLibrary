package net.dumbcode.dumblibrary.server.network;

import lombok.AllArgsConstructor;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.taxidermy.BaseTaxidermyBlockEntity;
import net.dumbcode.dumblibrary.server.utils.XYZAxis;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

@AllArgsConstructor
public class C2SMoveSelectedSkeletalPart {

    private final BlockPos pos;
    private final String part;
    private final XYZAxis axis;
    private final int type;
    private final float value;


    public static C2SMoveSelectedSkeletalPart fromBytes(PacketBuffer buf) {
        return new C2SMoveSelectedSkeletalPart(
            buf.readBlockPos(),
            buf.readUtf(),
            XYZAxis.values()[buf.readInt()],
            buf.readByte(),
            buf.readFloat()
        );
    }

    public static void toBytes(C2SMoveSelectedSkeletalPart packet, PacketBuffer buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeUtf(packet.part);
        buf.writeInt(packet.axis.ordinal());
        buf.writeByte(packet.type);
        buf.writeFloat(packet.value);
    }

    public static void handle(C2SMoveSelectedSkeletalPart message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            BlockPos pos = message.pos;
            TileEntity blockEntity = NetworkUtils.getPlayer(supplier).getCommandSenderWorld().getBlockEntity(pos);
            if(blockEntity instanceof BaseTaxidermyBlockEntity) {
                BaseTaxidermyBlockEntity builder = (BaseTaxidermyBlockEntity)blockEntity;
                builder.getHistory().liveEdit(message.part, message.type, message.axis, message.value);
                builder.setChanged();
                DumbLibrary.NETWORK.send(PacketDistributor.ALL.noArg(), new S2CUpdateSkeletalBuilder(pos.getX(), pos.getY(), pos.getZ(), message.part, message.axis, message.type, message.value));
            }
        });
        context.setPacketHandled(true);
    }
}
