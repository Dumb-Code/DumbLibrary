package net.dumbcode.dumblibrary.server.network;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import net.dumbcode.dumblibrary.server.taxidermy.BaseTaxidermyBlockEntity;
import net.dumbcode.dumblibrary.server.taxidermy.TaxidermyHistory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.vecmath.Vector3f;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class S11FullPoseChange implements IMessage {

    private Map<String, Vector3f> pose;
    private int x;
    private int y;
    private int z;

    public S11FullPoseChange() { }

    public S11FullPoseChange(BlockPos pos, Map<String, Vector3f> newPose) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.pose = newPose;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        int count = buf.readInt();
        pose = new HashMap<>();
        for (int i = 0; i < count; i++) {
            String name = ByteBufUtils.readUTF8String(buf);
            float rx = buf.readFloat();
            float ry = buf.readFloat();
            float rz = buf.readFloat();
            pose.put(name, new Vector3f(rx, ry, rz));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);

        buf.writeInt(pose.size());
        for(Map.Entry<String, Vector3f> entry : pose.entrySet()) {
            ByteBufUtils.writeUTF8String(buf, entry.getKey());
            Vector3f v = entry.getValue();
            buf.writeFloat(v.x);
            buf.writeFloat(v.y);
            buf.writeFloat(v.z);
        }
    }

    public static class Handler extends WorldModificationsMessageHandler<S11FullPoseChange, IMessage> {
        @Override
        protected void handleMessage(S11FullPoseChange message, MessageContext ctx, World world, EntityPlayer player) {
            BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain(message.x, message.y, message.z);
            TileEntity te = world.getTileEntity(pos);
            if(te instanceof BaseTaxidermyBlockEntity) {
                BaseTaxidermyBlockEntity builder = (BaseTaxidermyBlockEntity)te;
                List<TaxidermyHistory.Record> records = Lists.newArrayList();//TODO: re-add
//                message.pose.forEach((s, v) -> records.add(new TaxidermyHistory.Record(s, v)));
                builder.getHistory().addGroupedRecord(records);
            }
            pos.release();
        }
    }
}
