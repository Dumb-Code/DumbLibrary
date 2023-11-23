package net.dumbcode.dumblibrary.server.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Value;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SplitNetworkHandler {
    private static final Map<Short, byte[][]> BUFFER_MAP = new HashMap<>();

    private static int ids;
//    private static final BiMap<Byte, Class<?>> DESC_TO_CLASS = HashBiMap.create();
//    private static final BiMap<Class<?>, Byte> CLASS_TO_DESC = DESC_TO_CLASS.inverse();

    private static final Map<Class<?>, Entry<?>> CLASS_TO_ENTRY = new HashMap<>();
    private static final Map<Byte, Entry<?>> DESC_TO_ENTRY = new HashMap<>();

    public static void sendSplitMessage(Object message, PacketDistributor.PacketTarget target) {
        ByteBuf buffer = Unpooled.buffer();
        int startIndex = buffer.writerIndex();
        encode(message, buffer);
        int endIndex = buffer.writerIndex();

        byte[] data = new byte[endIndex - startIndex];
        buffer.readerIndex(startIndex);
        buffer.readBytes(data);

        int total = data.length/30000 + 1;

        Entry<?> packetDesc = CLASS_TO_ENTRY.get(message.getClass());
        DumbLibrary.getLogger().info("Splitting up packet (len={}) of class {} (id={}) into {} chunks", data.length, message.getClass().getSimpleName(), packetDesc.getId(), total);
        if(packetDesc == null) {
            throw new IllegalArgumentException("Tried to split up packet of class " + message.getClass() + ", but it wasn't registered");
        }
        int collectionID = getNextCollection(message.getClass());
        for (int i = 0; i < total; i++) {
            byte[] outData = new byte[i+1 == total ? data.length%30000 : 30000];
            System.arraycopy(data, 30000*i, outData, 0, outData.length);
            DumbLibrary.NETWORK.send(target, new B13SplitNetworkPacket(packetDesc.id, (short) collectionID, (byte) i, (byte) total, outData));
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void encode(T obj, ByteBuf buf) {
        Entry<T> entry = (Entry<T>) CLASS_TO_ENTRY.get(obj.getClass());
        entry.encoder.accept(obj, new FriendlyByteBuf(buf));
    }


    public static void handleSplitMessage(byte descriptor, short collectionID, byte index, byte total, byte[] data, PlayerEntity player, NetworkEvent.Context context) {
        byte[][] abyte = BUFFER_MAP.computeIfAbsent(collectionID, aShort -> new byte[total][]);
        if(abyte.length != total) {
            throw new IllegalArgumentException("Invalid abyte length found for received packet. Expected " + abyte.length + ", found " + total);
        }
        abyte[index] = data;

        boolean gotAll = true;
        for (byte[] aByte : abyte) {
            if (aByte == null) {
                gotAll = false;
                break;
            }
        }

        if(gotAll) {
            byte[] outData = new byte[30000*(abyte.length - 1) + abyte[abyte.length - 1].length];

            for (int i = 0; i < abyte.length; i++) {
                System.arraycopy(abyte[i], 0, outData, i * 30000, abyte[i].length);
            }
            BUFFER_MAP.remove(collectionID);
            if(player instanceof ServerPlayerEntity) {
                DumbLibrary.NETWORK.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new B14ReleaseCollection(collectionID));
            } else {
                DumbLibrary.NETWORK.sendToServer(new B14ReleaseCollection(collectionID));
            }

            ByteBuf buffer = Unpooled.buffer();
            int start = buffer.writerIndex();
            buffer.writeBytes(outData);
            buffer.readerIndex(start);

            handle(DESC_TO_ENTRY.get(descriptor), buffer, () -> context);

        }
    }

    private static <T> void handle(Entry<T> entry, ByteBuf buf, Supplier<NetworkEvent.Context> supplier) {
        T apply = entry.decoder.apply(new FriendlyByteBuf(buf));
        entry.messageConsumer.accept(apply, supplier);
    }

    public static void releaseCollection(short collectionID) {
        BUFFER_MAP.remove(collectionID);
    }

    public static <T> void registerMessage(Class<T> messageType, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> messageConsumer) {
        byte id = (byte) ids++;
        Entry<T> entry = new Entry<>(id, encoder, decoder, messageConsumer);
        CLASS_TO_ENTRY.put(messageType, entry);
        DESC_TO_ENTRY.put(id, entry);
    }



    private static int getNextCollection(Class<?> aClass) {
        if(BUFFER_MAP.isEmpty()) {
            return 0;
        }
        int[] ints = BUFFER_MAP.keySet().stream().mapToInt(Short::intValue).sorted().toArray();
        for (int i = 0; i < ints.length; i++) {
            if(i != ints[0]) {
                return i;
            }
        }
        if(ints.length > 500) {
            DumbLibrary.getLogger().error("Potential packet leak detected for class {}", aClass);
        }
        if(ints.length >= Short.MAX_VALUE) {
            throw new IllegalArgumentException("All 32767 split network collections have been sent out. This should be impossible. Please make sure you are not creating a packet leak");
        }
        return ints.length;
    }

    @Value
    private static class Entry<T> {
        byte id;
        BiConsumer<T, FriendlyByteBuf> encoder;
        Function<FriendlyByteBuf, T> decoder;
        BiConsumer<T, Supplier<NetworkEvent.Context>> messageConsumer;
    }
}
