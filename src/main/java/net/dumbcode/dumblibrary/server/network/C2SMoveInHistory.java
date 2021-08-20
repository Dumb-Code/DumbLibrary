package net.dumbcode.dumblibrary.server.network;

import lombok.AllArgsConstructor;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.taxidermy.BaseTaxidermyBlockEntity;
import net.dumbcode.dumblibrary.server.taxidermy.TaxidermyContainer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

@AllArgsConstructor
public class C2SMoveInHistory {

    private final boolean direction;

    public static C2SMoveInHistory fromBytes(PacketBuffer buf) {
        return new C2SMoveInHistory(buf.readBoolean());
    }

    public static void toBytes(C2SMoveInHistory packet, PacketBuffer buf) {
        buf.writeBoolean(packet.direction);
    }

    public static void handle(C2SMoveInHistory message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayerEntity sender = context.getSender();
            World world = sender.level;
            if(sender.containerMenu instanceof TaxidermyContainer) {
                BaseTaxidermyBlockEntity builder = ((TaxidermyContainer) sender.containerMenu).getBlockEntity();
                if(message.direction) {
                    builder.getHistory().redo();
                } else {
                    builder.getHistory().undo();
                }
                DumbLibrary.NETWORK.send(NetworkUtils.forPos(world, builder.getBlockPos()), new S2CUpdateHistoryIndex(builder.getBlockPos(), message.direction));
            }
        });
        context.setPacketHandled(true);
    }
}
