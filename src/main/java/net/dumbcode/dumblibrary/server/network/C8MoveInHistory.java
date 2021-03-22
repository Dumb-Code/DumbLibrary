package net.dumbcode.dumblibrary.server.network;

import lombok.AllArgsConstructor;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.taxidermy.BaseTaxidermyBlockEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

@AllArgsConstructor
public class C8MoveInHistory {

    private final BlockPos pos;
    private final boolean direction;

    public static C8MoveInHistory fromBytes(PacketBuffer buf) {
        return new C8MoveInHistory(buf.readBlockPos(), buf.readBoolean());
    }

    public static void toBytes(C8MoveInHistory packet, PacketBuffer buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeBoolean(packet.direction);
    }

    public static void handle(C8MoveInHistory message, Supplier<NetworkEvent.Context> supplier) {
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
                DumbLibrary.NETWORK.send(PacketDistributor.DIMENSION.with(world::dimension), new S9UpdateHistoryIndex(message.pos, message.direction));

            }
        });
    }
}
