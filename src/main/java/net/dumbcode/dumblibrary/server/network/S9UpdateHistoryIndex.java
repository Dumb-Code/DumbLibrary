package net.dumbcode.dumblibrary.server.network;

import lombok.AllArgsConstructor;
import net.dumbcode.dumblibrary.server.taxidermy.BaseTaxidermyBlockEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

@AllArgsConstructor
public class S9UpdateHistoryIndex {

    private final BlockPos pos;
    private final boolean direction;

    public static S9UpdateHistoryIndex fromBytes(PacketBuffer buf) {
        return new S9UpdateHistoryIndex(buf.readBlockPos(), buf.readBoolean());
    }

    public static void toBytes(S9UpdateHistoryIndex packet, PacketBuffer buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeBoolean(packet.direction);
    }

    public static void handle(S9UpdateHistoryIndex message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            World world = NetworkUtils.getPlayer(supplier).getCommandSenderWorld();
            TileEntity blockEntity = world.getBlockEntity(message.pos);
            if(blockEntity instanceof BaseTaxidermyBlockEntity) {
                BaseTaxidermyBlockEntity builder = (BaseTaxidermyBlockEntity)blockEntity;
                if(message.direction) {
                    builder.getHistory().redo();
                } else {
                    builder.getHistory().undo();
                }
            }
        });
        context.setPacketHandled(true);
    }
}
