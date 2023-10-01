package net.dumbcode.dumblibrary.server.network;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.taxidermy.BaseTaxidermyBlockEntity;
import net.dumbcode.dumblibrary.server.taxidermy.TaxidermyHistory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.core.BlockPos;
import org.joml.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@AllArgsConstructor
public class C2SFullPoseChange {

    private final BlockPos pos;
    private final Map<String, Vector3f> pose;

    public static C2SFullPoseChange fromBytes(PacketBuffer buf) {
        int count = buf.readInt();
        Map<String, Vector3f> pose = new HashMap<>();
        for (int i = 0; i < count; i++) {
            String name = buf.readUtf(32767);
            float rx = buf.readFloat();
            float ry = buf.readFloat();
            float rz = buf.readFloat();
            pose.put(name, new Vector3f(rx, ry, rz));
        }
        return new C2SFullPoseChange(buf.readBlockPos(), pose);
    }

    public static void toBytes(C2SFullPoseChange packet, PacketBuffer buf) {
        buf.writeInt(packet.pose.size());
        for (Map.Entry<String, Vector3f> entry : packet.pose.entrySet()) {
            buf.writeUtf(entry.getKey());
            Vector3f v = entry.getValue();
            buf.writeFloat(v.x());
            buf.writeFloat(v.y());
            buf.writeFloat(v.z());
        }
        buf.writeBlockPos(packet.pos);
    }

    public static void handle(C2SFullPoseChange message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            World world = NetworkUtils.getPlayer(supplier).getCommandSenderWorld();
            TileEntity blockEntity = world.getBlockEntity(message.pos);
            if (blockEntity instanceof BaseTaxidermyBlockEntity) {
                BaseTaxidermyBlockEntity builder = (BaseTaxidermyBlockEntity) blockEntity;
                List<TaxidermyHistory.Record> records = Lists.newArrayList(); //TODO: re-add this
//                message.pose.forEach((s, v) -> records.add(new TaxidermyHistory.Record(s, v)));
                builder.getHistory().addGroupedRecord(records);
                builder.setChanged();
                DumbLibrary.NETWORK.send(PacketDistributor.ALL.noArg(), new S2CFullPoseChange(message.pos, message.pose));
            }
        });
        context.setPacketHandled(true);
    }
}