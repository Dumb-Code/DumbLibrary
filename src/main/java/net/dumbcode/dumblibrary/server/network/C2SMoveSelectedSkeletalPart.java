package net.dumbcode.dumblibrary.server.network;

import lombok.AllArgsConstructor;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.taxidermy.BaseTaxidermyBlockEntity;
import net.dumbcode.dumblibrary.server.taxidermy.TaxidermyContainer;
import net.dumbcode.dumblibrary.server.utils.XYZAxis;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

@AllArgsConstructor
public class C2SMoveSelectedSkeletalPart {

    private final String part;
    private final XYZAxis axis;
    private final int type;
    private final float value;


    public static C2SMoveSelectedSkeletalPart fromBytes(FriendlyByteBuf buf) {
        return new C2SMoveSelectedSkeletalPart(
            buf.readUtf(32767),
            XYZAxis.values()[buf.readInt()],
            buf.readByte(),
            buf.readFloat()
        );
    }

    public static void toBytes(C2SMoveSelectedSkeletalPart packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.part);
        buf.writeInt(packet.axis.ordinal());
        buf.writeByte(packet.type);
        buf.writeFloat(packet.value);
    }

    public static void handle(C2SMoveSelectedSkeletalPart message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {

            ServerPlayerEntity sender = context.getSender();
            World world = sender.level;
            if(sender.containerMenu instanceof TaxidermyContainer) {
                BaseTaxidermyBlockEntity builder = ((TaxidermyContainer) sender.containerMenu).getBlockEntity();
                builder.getHistory().liveEdit(message.part, message.type, message.axis, message.value);
                builder.setChanged();
                DumbLibrary.NETWORK.send(NetworkUtils.forPos(world, builder.getBlockPos()), new S2CUpdateSkeletalBuilder(builder.getBlockPos(), message.part, message.axis, message.type, message.value));
            }
        });
        context.setPacketHandled(true);
    }
}
