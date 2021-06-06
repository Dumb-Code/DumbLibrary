package net.dumbcode.dumblibrary.server.network;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import net.dumbcode.dumblibrary.server.taxidermy.BaseTaxidermyBlockEntity;
import net.dumbcode.dumblibrary.server.taxidermy.TaxidermyHistory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@AllArgsConstructor
public class S2CFullPoseChange {

    private final BlockPos pos;
    private final Map<String, Vector3f> pose;

    public static S2CFullPoseChange fromBytes(PacketBuffer buf) {
        int count = buf.readInt();
        Map<String, Vector3f> pose = new HashMap<>();
        for (int i = 0; i < count; i++) {
            String name = buf.readUtf();
            float rx = buf.readFloat();
            float ry = buf.readFloat();
            float rz = buf.readFloat();
            pose.put(name, new Vector3f(rx, ry, rz));
        }
        return new S2CFullPoseChange(buf.readBlockPos(), pose);
    }

    public static void toBytes(S2CFullPoseChange packet, PacketBuffer buf) {
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

    public static void handle(S2CFullPoseChange message, Supplier<NetworkEvent.Context> supplier) {
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
            }
        });
        context.setPacketHandled(true);
    }
}