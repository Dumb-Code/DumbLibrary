package net.dumbcode.dumblibrary.server.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentType;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.registry.DumbRegistries;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class S2SyncComponent implements IMessage {

    private int entityid;
    private EntityComponentType<?, ?> type;
    private byte[] data;

    public S2SyncComponent() {
    }

    public S2SyncComponent(int entityid, EntityComponentType type, EntityComponent component) {
        ByteBuf buffer = Unpooled.buffer();
        component.serialize(buffer);
        this.entityid = entityid;
        this.type = type;
        this.data = buffer.array();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityid = buf.readInt();
        this.type = ByteBufUtils.readRegistryEntry(buf, DumbRegistries.COMPONENT_REGISTRY);
        this.data = new byte[buf.readInt()];
        buf.readBytes(this.data);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityid);
        ByteBufUtils.writeRegistryEntry(buf, this.type);
        buf.writeInt(this.data.length);
        buf.writeBytes(this.data);
    }

    public static class Handler extends WorldModificationsMessageHandler<S2SyncComponent, S2SyncComponent> {

        @Override
        protected void handleMessage(S2SyncComponent message, MessageContext ctx, World world, EntityPlayer player) {
            Entity entity = world.getEntityByID(message.entityid);
            if (entity instanceof ComponentAccess) {
                ((ComponentAccess) entity).get(message.type).ifPresent(c -> c.deserialize(Unpooled.wrappedBuffer(message.data)));
            }
        }
    }
}
