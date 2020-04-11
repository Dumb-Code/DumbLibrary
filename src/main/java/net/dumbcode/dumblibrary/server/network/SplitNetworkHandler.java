package net.dumbcode.dumblibrary.server.network;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class SplitNetworkHandler {
    private static final Map<Short, byte[][]> BUFFER_MAP = new HashMap<>();

    private static int ids;
    private static final BiMap<Byte, Class<? extends IMessage>> DESC_TO_CLASS = HashBiMap.create();
    private static final BiMap<Class<? extends IMessage>, Byte> CLASS_TO_DESC = DESC_TO_CLASS.inverse();

    private static final Map<Byte, IMessageHandler> DESC_TO_HANDLER = new HashMap<>();

    public static void sendSplitMessage(IMessage message, BiConsumer<SimpleNetworkWrapper, IMessage> messageSender) {
        ByteBuf buffer = Unpooled.buffer();
        int startIndex = buffer.writerIndex();
        message.toBytes(buffer);
        int endIndex = buffer.writerIndex();

        byte[] data = new byte[endIndex - startIndex];
        buffer.readerIndex(startIndex);
        buffer.readBytes(data);

        int total = data.length/30000 + 1;

        Byte packetDesc = CLASS_TO_DESC.get(message.getClass());
        DumbLibrary.getLogger().info("Splitting up packet (len={}) of class {} (id={}) into {} chunks", data.length, message.getClass().getSimpleName(), packetDesc, total);
        if(packetDesc == null) {
            throw new IllegalArgumentException("Tried to split up packet of class " + message.getClass() + ", but it wasn't registered");
        }
        int collectionID = getNextCollection();
        for (int i = 0; i < total; i++) {
            byte[] outData = new byte[i+1 == total ? data.length%30000 : 30000];
            System.arraycopy(data, 30000*i, outData, 0, outData.length);
            messageSender.accept(DumbLibrary.NETWORK, new B13SplitNetworkPacket(packetDesc, (short) collectionID, (byte) i, (byte) total, outData));
        }
    }

    public static void handleSplitMessage(byte descriptor, short collectionID, byte index, byte total, byte[] data, EntityPlayer player, MessageContext context) {
        byte[][] abyte = BUFFER_MAP.computeIfAbsent(collectionID, aShort -> new byte[total][]);
        if(abyte.length != total) {
            throw new IllegalArgumentException("Invalid abyte length found for received packet. Expected " + abyte.length + ", found " + total);
        }
        abyte[index] = data;

        boolean gotAll = true;
        for (byte[] aByte : abyte) {
            if(aByte == null) {
                gotAll = false;
            }
        }

        if(gotAll) {
            byte[] outData = new byte[30000*(abyte.length - 1) + abyte[abyte.length - 1].length];

            for (int i = 0; i < abyte.length; i++) {
                System.arraycopy(abyte[i], 0, outData, i * 30000, abyte[i].length);
            }
            BUFFER_MAP.remove(collectionID);
            if(player instanceof EntityPlayerMP) {
                DumbLibrary.NETWORK.sendTo(new B14ReleaseCollection(collectionID), (EntityPlayerMP) player);
            } else {
                DumbLibrary.NETWORK.sendToServer(new B14ReleaseCollection(collectionID));
            }

            ByteBuf buffer = Unpooled.buffer();
            int start = buffer.writerIndex();
            buffer.writeBytes(outData);
            buffer.readerIndex(start);

            try {
                IMessage iMessage = DESC_TO_CLASS.get(descriptor).newInstance();
                iMessage.fromBytes(buffer);
                DESC_TO_HANDLER.get(descriptor).onMessage(iMessage, context);
            } catch (InstantiationException | IllegalAccessException e) {
                DumbLibrary.getLogger().warn("Unable to create constructor for split network", e);
            }
        }
    }

    public static void releaseCollection(short collectionID) {
        BUFFER_MAP.remove(collectionID);

    }

    public static <T extends IMessage> void registerPacket(Class<T> clazz, IMessageHandler<T, ?> handler) {
        byte id = (byte) ids++;
        DESC_TO_CLASS.put(id, clazz);
        DESC_TO_HANDLER.put(id, handler);
    }

    private static int getNextCollection() {
        if(BUFFER_MAP.isEmpty()) {
            return 0;
        }
        int[] ints = BUFFER_MAP.keySet().stream().mapToInt(value -> value).sorted().toArray();
        if(ints[0] > 0) {
            return 0;
        }
        int previous = 0;
        for (int i : ints) {
            if (i - previous > 1) {
                return previous + 1;
            }
        }
        throw new IllegalArgumentException("All 32767 split network collections have been sent out. This should be impossible. Please make sure you are not creating a packet leak");
    }
}
